package utils

import java.io.File
import java.net.URL

import org.geotools.data.DataStoreFinder
import org.geotools.feature.FeatureIterator
import org.opengis.feature.{Feature, GeometryAttribute}

import scala.jdk.CollectionConverters._

case class ShapefileWrapper(features: Seq[FeatureWrapper])

case class FeatureWrapper(id: String, geom: GeometryAttribute, attrs: Map[Int, Any])

object ShapefileWrapper {
  def fromFname(fname: String) = fromFile(new File(fname))
  def fromFile(file: File) = fromUrl(file.toURI.toURL)
  def fromUrl(file: URL) = {
    val params = Map("url" -> file).asJava
    val data = DataStoreFinder.getDataStore(params)
    val res = for {
      name: String <- data.getTypeNames
      source = data.getFeatureSource(name)
      collection = source.getFeatures
      feature <- new FeatureIteratorWrapper(collection.features())
    } yield {
      val attrs = feature.getAttributes.asScala.zipWithIndex.map { case (attr, idx) => idx -> attr }.toMap
      val id = feature.getID
      val geom = feature.getDefaultGeometryProperty
      FeatureWrapper(id, geom, attrs)
    }
    new ShapefileWrapper(res.toSeq)
  }
}

class FeatureIteratorWrapper[A <: Feature](fi: FeatureIterator[A]) extends Iterator[A] {
  override def hasNext: Boolean = fi.hasNext
  override def next(): A = fi.next()
}
