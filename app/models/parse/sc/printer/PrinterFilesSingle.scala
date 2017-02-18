package models.parse.sc.printer

import better.files._
import models.parse.sc.tree.Name

class PrinterFilesSingle(key: String) extends PrinterFiles {
  private[this] val dir = "data" / "projects" / "_megaproject" / "src" / "main" / "scala" / "org" / "scalajs"

  if (!dir.exists) {
    throw new IllegalStateException(s"Missing output directory [${dir.path}].")
  }

  val file = dir / s"$key.scala"
  if (file.exists) {
    file.delete()
  }

  file.append(s"package org.scalajs.$key\n")

  file.append("\n")
  file.append("import scala.scalajs.js\n")
  file.append("import scala.scalajs.js.|\n")
  file.append("import org.scalajs.dom.raw._\n")
  file.append("\n")

  override def pushPackage(pkg: Name) = {
    val p = pkg.name.replaceAllLiterally("-", "")
    file.append(s"package $p {\n")
  }
  override def popPackage(pkg: Name) = file.append(s"}\n")

  override def print(s: String) = file.append(s)

  override def onComplete() = {

  }
}
