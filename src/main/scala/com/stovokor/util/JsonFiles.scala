package com.stovokor.util

import java.io.FileWriter
import java.io.BufferedWriter
import scala.io.Source
import java.io.File

object JsonFiles {
  import org.json4s._
  import org.json4s.jackson.Serialization
  import org.json4s.jackson.Serialization.{ read, write }

  implicit val formats = Serialization.formats(NoTypeHints)

  def save[T <: AnyRef](path: String, obj: T): T = {
    val json = write(obj)
    val file = new File(path)
    println(s"Saving ${file.getAbsolutePath}")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(json)
    bw.close()
    obj
  }

  def load[T <: AnyRef](path: String)(implicit mf: Manifest[T]): T = {
    val file = new File(path)
    val json = Source.fromFile(file).mkString
    read(json)
  }
}
