// See README.md for license details.

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.github.ucbbar"

val chiselVersion = "3.5.6"




val chiselFirrtlMergeStrategy = CustomMergeStrategy("cfmergestrategy") { deps =>
  import sbtassembly.Assembly.{Project, Library}
  val keepDeps = deps.filter { dep =>
    val nm = dep match {
      case p: Project => p.name
      case l: Library => l.moduleCoord.name
    }
    Seq("firrtl", "chisel3").contains(nm.split("_")(0)) // split by _ to avoid checking on major/minor version
  }
  if (keepDeps.size <= 1) {
    Right(keepDeps.map(dep => JarEntry(dep.target, dep.stream)))
  } else {
    Left(s"Unable to resolve conflict (${keepDeps.size}>1 conflicts):\n${keepDeps.mkString("\n")}")
  }
}

lazy val root = (project in file("."))
  .settings(
    name := "whatever",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "rocketchip" % "1.6.0-c49644ecd-SNAPSHOT",
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.5.4" % "test"
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
    autoAPIMappings  := true,
    exportJars := true,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.mavenLocal),
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("chisel3", "stage", xs @ _*) => chiselFirrtlMergeStrategy
      case PathList("firrtl", "stage", xs @ _*) => chiselFirrtlMergeStrategy
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
    }
  )

