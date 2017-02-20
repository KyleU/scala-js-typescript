package models.parse.sc.tree

import scala.collection.mutable

class MethodSymbol(prot: Boolean, nme: Name) extends Symbol(nme) with JSNameable {
  val tparams = new mutable.ListBuffer[TypeParamSymbol]
  val params = new mutable.ListBuffer[ParamSymbol]
  var resultType: TypeRef = TypeRef.Dynamic
  var isBracketAccess: Boolean = false
  val p = if (prot) { "protected " } else { "" }

  override def toString = {
    val bracketAccessStr = if (isBracketAccess) { "@JSBracketAccess " } else { "" }

    val tparamsStr = if (tparams.isEmpty) {
      ""
    } else {
      tparams.mkString("[", ", ", "]")
    }
    s"$jsNameStr$bracketAccessStr${p}def $name$tparamsStr(${params.mkString(", ")}): $resultType"
  }

  def paramTypes = params.map(_.tpe)

  override def equals(that: Any): Boolean = that match {
    case that: MethodSymbol =>
      this.name == that.name && this.tparams == that.tparams && this.paramTypes == that.paramTypes && this.resultType == that.resultType
    case _ => false
  }

  override def hashCode() = prot.hashCode * nme.hashCode
}
