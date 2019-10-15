import java.io.{File, FileWriter, PrintWriter}
import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths}

import com.github.tototoshi.csv.CSVReader
import utils.{ShapefileWrapper, ZipWrapper}

import scala.jdk.CollectionConverters._

object Main {
  val DefaultCharset = Charset.forName("MS932")
  val SurveySQL = "outputs/survey.sql"

  val surveyGen = new SurveySQLGenerator("survey")
  val polygonGen = new PolygonSQLGenerator("flood_polygon", "survey_id", "depth", "polygon")

  def main(args: Array[String]): Unit = {
    createSurveySQL()
    zipList("sources").foreach { file =>
      println(s"Start: ${file.getName}")
      val path = Files.createTempDirectory(Paths.get("/tmp"), "hazzard2sql")
      val zip = new ZipWrapper(file, DefaultCharset)
      zip.fileList.filterNot(_.isDirectory).foreach { entry =>
        val is = zip.getInputStream(entry)
        val newPath = path.resolve(entry.getName)
        Files.createDirectories(newPath.getParent)
        Files.copy(is, newPath)
      }
      walkDir(path).filter { f => Files.isRegularFile(f) }.foreach { file =>
        val Array(_, ext) = file.getFileName.toString.split('.')
        ext match {
          case "shp" => shp2sql(file.toFile)
          case "txt" => txt2sql(file.toFile)
          case _ =>
        }
      }
      removeRecursive(path)
    }
  }

  private def zipList(dir: String): Seq[File] =
    new File("sources").listFiles((_, name) => name.endsWith(".zip"))

  private def removeRecursive(path: Path): Unit =
    walkDir(path).toSeq.reverseIterator
        .map(_.toFile).foreach(_.delete())

  private def walkDir(path: Path): Iterator[Path] =
    Files.walk(path).iterator().asScala

  def shp2sql(shp: File): Unit = {
    val shapes = ShapefileWrapper.fromFile(shp)
    val polygons = shapes.features.flatMap(FloodPolygon.fromFeature)
    val outputName = shp.getName.replace(".shp", ".sql")
    val writer = new PrintWriter(s"outputs/${outputName}")
    writer.write(polygonGen.genInit)
    writer.write("\n")
    polygons.grouped(100).map(polygonGen.gen).foreach { str =>
      writer.write(str)
      writer.write("\n")
    }
    writer.flush()
  }

  def createSurveySQL(): Unit = {
    val writer = new PrintWriter(SurveySQL)
    writer.write(surveyGen.genInit)
    writer.write("\n")
    writer.flush()
  }

  def txt2sql(file: File): Unit = {
    val reader = CSVReader.open(file, DefaultCharset.name())
    val Array(id, _) = file.getName.split('.')
    val survey = Survey.fromCsv(id.toInt, reader.all())
    survey.foreach { s =>
      val writer = new PrintWriter(new FileWriter(SurveySQL, true))
      writer.write(surveyGen.gen(s))
      writer.write("\n")
      writer.flush()
    }
    reader.close()
  }
}
