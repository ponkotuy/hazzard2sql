package utils

import java.io.{File, InputStream}
import java.nio.charset.{Charset, StandardCharsets}
import java.util.zip.{ZipEntry, ZipFile}

import scala.jdk.CollectionConverters._

class ZipWrapper(file: File) {
  val zip = new ZipFile(file, Charset.forName("MS932"))
  def fileList: Iterator[ZipEntry] = {
    zip.entries().asScala
  }

  def fileList(extension: String): Iterator[ZipEntry] = {
    fileList.filter(_.getName.endsWith(extension))
  }

  def getInputStream(entry: ZipEntry): InputStream = zip.getInputStream(entry)
}
