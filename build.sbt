
name := "Hazzard2SQL"

scalaVersion := "2.13.1"

resolvers ++= Seq(
  "OSGeo Repository" at "https://download.osgeo.org/webdav/geotools/"
)

val GeotoolsVersion = "22.0"

libraryDependencies ++= Seq(
  "org.geotools" % "gt-main" % GeotoolsVersion,
  "org.geotools" % "gt-shapefile" % GeotoolsVersion
)