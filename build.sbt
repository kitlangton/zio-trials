ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val zioVersion = "2.0.2"

lazy val root = (project in file("."))
  .settings(
    name := "zio-trials",
    libraryDependencies ++= Seq(
      "dev.zio"              %% "zio"          % zioVersion,
      "dev.zio"              %% "zio-test"     % zioVersion % Test,
      "dev.zio"              %% "zio-process"  % "0.7.1",
      "io.github.kitlangton" %% "zio-tui"      % "0.2.0",
      "dev.zio"              %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
