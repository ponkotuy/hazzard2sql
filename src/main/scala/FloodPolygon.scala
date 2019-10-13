import org.locationtech.jts.geom.{Coordinate, MultiPolygon}
import utils.FeatureWrapper

case class FloodPolygon(surveyId: Int, depth: Depth, polygon: MultiPolygon)

object FloodPolygon {
  def fromFeature(feature: FeatureWrapper): Option[FloodPolygon] = {
    val a = feature.attrs
    for {
      depthCode <- a.get(1).map(_.asInstanceOf[Int])
      depth <- Depth.fromCode(depthCode)
      surveyId <- a.get(6).flatMap(_.asInstanceOf[String].toIntOption)
    } yield {
      val geom = feature.geom.getValue.asInstanceOf[MultiPolygon]
      FloodPolygon(surveyId, depth, geom)
    }
  }
}

case class Depth(min: Double, max: Option[Double])

object Depth {
  def fromCode(code: Int): Option[Depth] = code match {
    case 11 => Some(Depth(0, Some(0.5)))
    case 12 => Some(Depth(0.5, Some(1.0)))
    case 13 => Some(Depth(1.0, Some(2.0)))
    case 14 => Some(Depth(2.0, Some(5.0)))
    case 15 => Some(Depth(5.0, None))
    case 21 => Some(Depth(0, Some(0.5)))
    case 22 => Some(Depth(0.5, Some(1.0)))
    case 23 => Some(Depth(1.0, Some(2.0)))
    case 24 => Some(Depth(2.0, Some(3.0)))
    case 25 => Some(Depth(3.0, Some(4.0)))
    case 26 => Some(Depth(4.0, Some(5.0)))
    case 27 => Some(Depth(5.0, None))
  }
}
