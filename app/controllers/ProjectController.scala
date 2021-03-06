package controllers

import services.file.FileService
import services.project.ProjectService
import utils.Application

import scala.concurrent.Future

@javax.inject.Singleton
class ProjectController @javax.inject.Inject() (override val app: Application, parseController: ParseController) extends BaseController {
  def update(key: String) = act(s"update.$key") { implicit request =>
    val created = ProjectService(key).update()
    Future.successful(Ok(views.html.project.update(key, created, app.config.debug)))
  }

  def updateAll(q: Option[String]) = act("project.all") { implicit request =>
    val outDirs = FileService.getDir("out").list.filter(_.isDirectory).filter(_.name.contains(q.getOrElse(""))).toSeq
    val results = outDirs.par.map(outDir => outDir.name -> ProjectService(outDir.name).update()).seq
    Future.successful(Ok(views.html.project.updateAll(results, app.config.debug)))
  }
}
