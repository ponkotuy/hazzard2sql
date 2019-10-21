import java.io.StringWriter

import org.geotools.geojson.geom.GeometryJSON
import org.geotools.geometry.jts.JTSFactoryFinder
import org.locationtech.jts.geom.{MultiPolygon, Polygon}

class PolygonSQLGenerator(table: String, surveyIdColumn: String, depthColumn: String, polygonColumn: String) {
  import PolygonSQLGenerator._

  def genInit: String =
    s"""create table if not exists ${table} (
       |  id bigint not null auto_increment primary key,
       |  ${surveyIdColumn} int not null,
       |  ${depthColumn}_min double not null,
       |  ${depthColumn}_max double,
       |  ${polygonColumn} geometry not null,
       |  index (${surveyIdColumn}),
       |  spatial index (${polygonColumn})
       |) engine=InnoDB default charset=utf8mb4;""".stripMargin

  def gen(polygons: Seq[FloodPolygon]): String = {
    val csv = polygons.map { p =>
      s"(${p.surveyId}, ${p.depth.min}, ${p.depth.max.getOrElse("null")}, ${toSQL(p.polygon)})"
    }.mkString(",")
    s"insert into ${table} (${surveyIdColumn}, ${depthColumn}_min, ${depthColumn}_max, ${polygonColumn}) values ${csv};"
  }
}

object PolygonSQLGenerator {
  def toSQL(x: MultiPolygon): String = s"ST_GeomFromGeoJSON('${toGeoJson(x)}')"

  def toGeoJson(x: MultiPolygon): String = {
    val gjson = new GeometryJSON()
    val writer = new StringWriter()
    gjson.write(closePolygon(x), writer)
    writer.toString
  }

  def closePolygon(x: MultiPolygon): MultiPolygon = {
    val newPolygons: Array[Polygon] = (0 until x.getNumGeometries).map(x.getGeometryN).map { polygon =>
      val coors = polygon.getCoordinates
      if(coors.head == coors.last) polygon.asInstanceOf[Polygon]
      else {
        val factory = JTSFactoryFinder.getGeometryFactory
        factory.createPolygon(coors :+ coors.head)
      }
    }.toArray
    val factory = JTSFactoryFinder.getGeometryFactory
    factory.createMultiPolygon(newPolygons)
  }
}
