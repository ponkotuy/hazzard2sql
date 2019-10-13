import org.locationtech.jts.geom.MultiPolygon

class PolygonSQLGenerator(table: String, surveyIdColumn: String, depthColumn: String, polygonColumn: String) {
  def genInit: String =
    s"""create table ${table} (
       |  id bigint not null primary auto_increment,
       |  ${surveyIdColumn} integer not null,
       |  ${depthColumn} double not null,
       |  ${polygonColumn} geometry not null,
       |  index (${surveyIdColumn}),
       |  spatial index (${polygonColumn})
       |);""".stripMargin

  def gen(polygons: Seq[FloodPolygon]): String = {
    val csv = polygons.map { p =>
      s"(${p.surveyId}, ${p.depth.min}, ${toSQL(p.polygon)})"
    }.mkString(",")
    s"insert into ${table} (${surveyIdColumn}, ${depthColumn}, ${polygonColumn}) values ${csv};"
  }

  def toSQL(x: MultiPolygon): String = {
    val polygons = (0 until x.getNumGeometries).map(x.getGeometryN)
    val csv = polygons.map { p =>
      p.getCoordinates.map { c => s"${c.x} ${c.y}" }.mkString("(", ",", ")")
    }.mkString("(", ",", ")")
    s"GeomFromText('MultiPolygon($csv)')"
  }
}
