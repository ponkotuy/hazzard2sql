import java.io.StringWriter

import org.geotools.geojson.geom.GeometryJSON
import org.locationtech.jts.geom.MultiPolygon

class PolygonSQLGenerator(table: String, surveyIdColumn: String, depthColumn: String, polygonColumn: String) {
  def genInit: String =
    s"""create table if not exists ${table} (
       |  id bigint not null auto_increment primary key,
       |  ${surveyIdColumn} int not null,
       |  ${depthColumn} double not null,
       |  ${polygonColumn} geometry not null,
       |  index (${surveyIdColumn}),
       |  spatial index (${polygonColumn})
       |) engine=InnoDB default charset=utf8mb4;""".stripMargin

  def gen(polygons: Seq[FloodPolygon]): String = {
    val csv = polygons.map { p =>
      s"(${p.surveyId}, ${p.depth.min}, ${toSQL(p.polygon)})"
    }.mkString(",")
    s"insert into ${table} (${surveyIdColumn}, ${depthColumn}, ${polygonColumn}) values ${csv};"
  }

  def toSQL(x: MultiPolygon): String = s"ST_GeomFromGeoJSON('${toGeoJson(x)}')"

  def toGeoJson(x: MultiPolygon): String = {
    val gjson = new GeometryJSON()
    val writer = new StringWriter()
    gjson.write(x, writer)
    writer.toString
  }
}
