import java.time.LocalDate
import java.time.chrono.JapaneseChronology
import java.time.format.DateTimeFormatter
import java.util.Locale

import scala.util.Try

case class Survey(
    id: Int,
    contents: Seq[String],
    code: Int,
    author: String,
    date: Option[LocalDate],
    number: String,
    target: String,
    rainfall: String,
    city: String,
    others: Seq[String]
)

object Survey {
  val DateFormatter = DateTimeFormatter.ofPattern("GGGGy年M月d日", Locale.JAPAN)
      .withChronology(JapaneseChronology.INSTANCE)

  def fromCsv(id: Int, csv: Seq[Seq[String]]): Option[Survey] = {
    import SurveyType._
    val texts = csv.flatMap(SurveyText.fromLine)
    for {
      codeRaw <- texts.find(_.surveyType == TypeCode).map(_.text)
      code <- codeRaw.toIntOption
      author <- texts.find(_.surveyType == Author).map(_.text)
      dateRaw <- texts.find(_.surveyType == SurveyDate).map(_.text)
      number <- texts.find(_.surveyType == Number).map(_.text)
      target <- texts.find(_.surveyType == TargetRiver).map(_.text)
      rainfall <- texts.find(_.surveyType == Rainfall).map(_.text)
      city <- texts.find(_.surveyType == City).map(_.text)
      contents = texts.filter(_.surveyType == Explanation).map(_.text)
      others = texts.filter(_.surveyType == Other).map(_.text)
    } yield {
      val date = Try { LocalDate.parse(dateRaw, DateFormatter) }.toOption
      Survey(id, contents, code, author, date, number, target, rainfall, city, others)
    }
  }
}

case class SurveyText(surveyType: SurveyType, no: String, text: String)

object SurveyText {
  def fromLine(line: Seq[String]): Option[SurveyText] = for {
    first <- line.headOption
    typ <- SurveyType.find(first)
    second <- line.lift(1)
    third <- line.lift(2)
  } yield {
    SurveyText(typ, second, third)
  }
}

sealed abstract class SurveyType(val text: String)

object SurveyType {
  case object Explanation extends SurveyType("説明文")
  case object TypeCode extends SurveyType("作成種別コード")
  case object Author extends SurveyType("作成主体")
  case object SurveyDate extends SurveyType("指定年月日")
  case object Number extends SurveyType("告示番号")
  case object TargetRiver extends SurveyType("対象となる洪水予報河川")
  case object Rainfall extends SurveyType("指定の前提となる計画降雨")
  case object City extends SurveyType("関係市町村")
  case object Other extends SurveyType("その他計算条件等")

  val values = Explanation :: TypeCode :: Author :: SurveyDate :: Number :: TargetRiver :: Rainfall :: City :: Other :: Nil

  def find(text: String): Option[SurveyType] = values.find(_.text == text)
}
