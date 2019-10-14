import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths}

import utils.{ShapefileWrapper, ZipWrapper}

import scala.jdk.CollectionConverters._

object Main {
  def main(args: Array[String]): Unit = {
    zipList("sources").foreach { file =>
      val path = Files.createTempDirectory(Paths.get("/tmp"), "hazzard2sql")
      val zip = new ZipWrapper(file)
      zip.fileList.foreach { entry =>
        val is = zip.getInputStream(entry)
        val newPath = path.resolve(entry.getName)
        Files.createDirectories(newPath.getParent)
        Files.copy(is, newPath)
      }
      fileList(path).filter { f => Files.isRegularFile(f) }.foreach { file =>
        val Array(_, ext) = file.getFileName.toString.split('.')
        println(ext)
        ext match {
          case "shp" => shp2sql(file.toFile)
          case _ =>
        }
      }
      removeRecursive(path)
    }
  }

  private def zipList(dir: String): Seq[File] =
    new File("sources").listFiles((_, name) => name.endsWith(".zip"))

  private def removeRecursive(path: Path) =
    Files.walk(path).iterator().asScala.toSeq.reverseIterator
        .map(_.toFile).foreach(_.delete())

  private def fileList(path: Path): Iterator[Path] =
    Files.list(path).iterator().asScala

  def shp2sql(shp: File) = {
    val generator = new PolygonSQLGenerator("flood_polygon", "survey_id", "depth", "polygon")
    val shapes = ShapefileWrapper.fromFile(shp)
    val polygons = shapes.features.flatMap(FloodPolygon.fromFeature)
    val outputName = shp.getName.replace(".shp", ".sql")
    val writer = new PrintWriter(s"outputs/${outputName}")
    writer.write(generator.genInit)
    writer.write("\n")
    polygons.grouped(100).map(generator.gen).foreach { str =>
      writer.write(str)
      writer.write("\n")
    }
    writer.flush()
  }
}
