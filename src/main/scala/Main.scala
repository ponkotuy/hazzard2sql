import utils.ShapefileWrapper

object Main {
  def main(args: Array[String]): Unit = {
    val Fname = "A31-12_13.shp"
    val shapes = ShapefileWrapper.fromFname(Fname)
    shapes.features.map(_.id).foreach(println)
  }
}
