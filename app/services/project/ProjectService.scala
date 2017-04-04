package services.project

import models.parse.ProjectDefinition
import services.file.FileService

object ProjectService {
  def outDirFor(key: String) = {
    val out = FileService.getDir("out")
    out.children.find(_.name.replaceAllLiterally("-", "").replaceAllLiterally(".", "") == key).getOrElse(out / key)
  }

  private[this] val dir = FileService.getDir("projects")

  def list(q: Option[String]) = dir.list.filter(_.isDirectory).filter(_.name.contains(q.getOrElse(""))).toSeq

  def projectDir(key: String) = {
    val keyNormalized = key.replaceAllLiterally("-", "").replaceAllLiterally(".", "")
    dir / ("scala-js-" + keyNormalized)
  }
}

case class ProjectService(key: String) {
  val outDir = FileService.getDir("out") / key

  if (!outDir.exists) {
    throw new IllegalStateException(s"Missing out dir [$outDir].")
  }

  private[this] def updateProject(project: ProjectDefinition) = {
    val proj = ProjectOutput(project, ProjectService.projectDir(project.key))
    val created = !proj.exists()
    val projectSrcDir = proj.create(rebuild = created)

    projectSrcDir.delete()
    projectSrcDir.createDirectory()

    outDir.copyTo(projectSrcDir)

    (projectSrcDir / "project.json").delete()
    created
  }

  def update() = updateProject(ProjectDefinition.fromJson(outDir))
}
