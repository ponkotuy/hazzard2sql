import java.io.PrintWriter

import utils.ShapefileWrapper

object Main {
  def main(args: Array[String]): Unit = {
    val generator = new PolygonSQLGenerator("flood_polygon", "survey_id", "depth", "polygon")
    val Fname = "A31-12_13.shp"
    val shapes = ShapefileWrapper.fromFname(Fname)
    val polygons = shapes.features.flatMap(FloodPolygon.fromFeature)
    val writer = new PrintWriter("polygon.sql")
    writer.write(generator.genInit)
    writer.write("\n")
    polygons.grouped(100).map(generator.gen).foreach { str => writer.write(str); writer.write("\n") }
  }
}
