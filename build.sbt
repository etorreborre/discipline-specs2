/*
 * Copyright 2018-2021 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / baseVersion := "1.2"

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / developers := List(
  Developer("larsrh", "Lars Hupel", "", url("https://github.com/larsrh")),
  Developer("travisbrown", "Travis Brown", "", url("https://github.com/travisbrown")),
  Developer(
    "ChristopherDavenport",
    "Christopher Davenport",
    "",
    url("https://github.com/ChristopherDavenport")
  ),
  Developer("djspiewak", "Daniel Spiewak", "", url("https://github.com/djspiewak")),
  Developer("vasilmkd", "Vasil Vasilev", "", url("https://github.com/vasilmkd"))
)

val Scala213 = "2.13.6"

ThisBuild / crossScalaVersions := Seq("3.0.2", "2.12.14", Scala213)

ThisBuild / githubWorkflowJavaVersions := Seq("adoptium@8")
ThisBuild / githubWorkflowEnv += ("JABBA_INDEX" -> "https://github.com/vasilmkd/jdk-index/raw/main/index.json")

ThisBuild / githubWorkflowUseSbtThinClient := false
ThisBuild / githubWorkflowTargetBranches := Seq("main")

ThisBuild / homepage := Some(url("https://github.com/typelevel/discipline-specs2"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/typelevel/discipline-specs2"),
    "git@github.com:typelevel/discipline-specs2.git"
  )
)

ThisBuild / startYear := Some(2018)
ThisBuild / endYear := Some(2021)

val MicrositesCond = s"matrix.scala == '$Scala213'"

ThisBuild / githubWorkflowBuildPreamble ++= Seq(
  WorkflowStep.Use(
    UseRef.Public("ruby", "setup-ruby", "v1"),
    params = Map("ruby-version" -> "2.6"),
    cond = Some(MicrositesCond)
  ),
  WorkflowStep.Run(List("gem install sass"), cond = Some(MicrositesCond)),
  WorkflowStep.Run(List("gem install jekyll -v 3.2.1"), cond = Some(MicrositesCond))
)

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(
    List("test", "mimaReportBinaryIssues"),
    name = Some("Validate unit tests and binary compatibility")
  ),
  WorkflowStep.Sbt(List("docs/makeMicrosite"), cond = Some(MicrositesCond))
)

lazy val `discipline-specs2` = project
  .in(file("."))
  .aggregate(coreJVM, coreJS)
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "discipline-specs2"
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MicrositesPlugin, NoPublishPlugin)
  .settings(commonSettings, micrositeSettings)
  .dependsOn(coreJVM)

val disciplineV = "1.1.5"
val specs2V = "4.12.10"
val specs2DottyV = "5.0.0-RC-07"

// General Settings
lazy val commonSettings = Seq(
  Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-sourcepath",
    (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-doc-source-url",
    "https://github.com/typelevel/discipline-specs2/blob/v" + version.value + "€{FILE_PATH}.scala"
  ),
  libraryDependencies += "org.typelevel" %%% "discipline-core" % disciplineV,
  libraryDependencies += {
    if (isDotty.value)
      "org.specs2" %%% "specs2-scalacheck" % specs2DottyV
    else
      "org.specs2" %%% "specs2-scalacheck" % specs2V
  },
  Compile / doc / sources := {
    val old = (Compile / doc / sources).value
    if (isDotty.value)
      Seq()
    else
      old
  }
)

lazy val micrositeSettings = {
  import microsites._
  Seq(
    micrositeName := "discipline-specs2",
    micrositeDescription := "Specs2 Integration for Discipline",
    micrositeAuthor := "typelevel",
    micrositeGithubOwner := "typelevel",
    micrositeGithubRepo := "discipline-specs2",
    micrositeBaseUrl := "/discipline-specs2",
    micrositeDocumentationUrl := "https://www.javadoc.io/doc/org.typelevel/discipline-specs2_2.13",
    micrositeGitterChannelUrl := "typelevel/cats",
    micrositeFooterText := None,
    micrositeHighlightTheme := "atom-one-light",
    micrositePalette := Map(
      "brand-primary" -> "#3e5b95",
      "brand-secondary" -> "#294066",
      "brand-tertiary" -> "#2d5799",
      "gray-dark" -> "#49494B",
      "gray" -> "#7B7B7E",
      "gray-light" -> "#E5E5E6",
      "gray-lighter" -> "#F4F3F4",
      "white-color" -> "#FFFFFF"
    ),
    micrositeExtraMdFiles := Map(
      file("CHANGELOG.md") -> ExtraMdFileConfig(
        "changelog.md",
        "page",
        Map("title" -> "changelog", "section" -> "changelog", "position" -> "100")
      ),
      file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig(
        "code-of-conduct.md",
        "page",
        Map("title" -> "code of conduct", "section" -> "code of conduct", "position" -> "101")
      ),
      file("LICENSE") -> ExtraMdFileConfig(
        "license.md",
        "page",
        Map("title" -> "license", "section" -> "license", "position" -> "102")
      )
    )
  )
}
