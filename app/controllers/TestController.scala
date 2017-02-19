package controllers

import better.files._
import models.parse.parser.tree.DeclTree
import services.file.FileService
import services.parse.{ExportService, TypeScriptImport}
import utils.Application

import scala.concurrent.Future

object TestController {
  case class Result(key: String, src: File, tree: Option[List[DeclTree]], text: Seq[String])
}

@javax.inject.Singleton
class TestController @javax.inject.Inject() (override val app: Application) extends BaseController {
  def index() = act("index") { implicit request =>
    val scripts = FileService.getDir("DefinitelyTyped").list.filter(_.isDirectory).toSeq
    Future.successful(Ok(views.html.parse.index(scripts, app.config.debug)))
  }

  def script(key: String) = act(s"detail.$key") { implicit request =>
    val result = process(key)
    Future.successful(Ok(views.html.parse.script(key, result.src, result.tree, result.text, app.config.debug)))
  }

  def allScripts() = act("script.all") { implicit request =>
    val scripts = FileService.getDir("DefinitelyTyped").list.filter(_.isDirectory).toSeq
    val results = scripts.map(script => process(script.name))
    Future.successful(Ok(views.html.parse.allScripts(results, app.config.debug)))
  }

  private[this] def process(key: String) = {
    val dir = FileService.getDir("DefinitelyTyped") / key
    val ts = dir / "index.d.ts"
    val content = ts.contentAsString
    val tree = TypeScriptImport.parse(content)
    val res = tree match {
      case Right(t) => Some(t) -> ExportService(key, t).export()
      case Left(err) => None -> Seq("Error: " + err)
    }
    TestController.Result(key, ts, res._1, res._2)
  }
}
