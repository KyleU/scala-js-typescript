package models.parse.sc.transform

object Replacements {
  sealed trait Rule {
    def replace(lines: Seq[String]): Seq[String]
  }

  private[this] def unquote(txt: String) = if (txt.startsWith("\"") && txt.endsWith("\"")) { txt.substring(1, txt.length - 1) } else { txt }

  object Rule {
    case class AddLine(src: String, tgt: String) extends Rule {
      override def replace(lines: Seq[String]) = lines.flatMap(l => if (l == src) { Seq(l, tgt) } else { Seq(l) })
    }
    case class RemoveLine(tgt: String) extends Rule {
      override def replace(lines: Seq[String]) = lines.filterNot(_ == tgt)
    }
    case class ReplaceLine(src: String, tgt: String) extends Rule {
      override def replace(lines: Seq[String]) = lines.map(l => if (l == src) { tgt } else { l })
    }
    case class ReplaceText(src: String, tgt: String) extends Rule {
      override def replace(lines: Seq[String]) = lines.map(_.replaceAllLiterally(unquote(src), unquote(tgt)))
    }
  }

  def toRules(lines: Seq[String]) = {
    lines.zipWithIndex.foldLeft(Seq.empty[Rule]) { (rules, line) =>
      line._1 match {
        case "+" => rules :+ Rule.AddLine(lines(line._2 + 1), lines(line._2 + 2))
        case "-" => rules :+ Rule.RemoveLine(lines(line._2 + 1))
        case "=" => rules :+ Rule.ReplaceLine(lines(line._2 + 1), lines(line._2 + 2))
        case "*" => rules :+ Rule.ReplaceText(lines(line._2 + 1), lines(line._2 + 2))
        case _ => rules
      }
    }
  }
}

case class Replacements(key: String, rules: Seq[Replacements.Rule]) {
  def replace(lines: Seq[String]) = rules.foldLeft(lines) { (l, r) => r.replace(l) }
}
