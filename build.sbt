sbtPlugin := true

name := "sbt-sticker"

organization := "com.krrrr38"

version := "0.0.3"

isSnapshot := false

publishMavenStyle := true

publishTo := {
  val ghpageMavenDir: Option[String] = {
    val searchResult = (Process(Seq("ghq", "list", "--full-path")) #| Process(Seq("grep", "krrrr38/maven")) !!).trim
    if (searchResult.isEmpty) None else Some(searchResult)
  }
  ghpageMavenDir.map { dirPath =>
    Resolver.file(
      organization.value,
      file(dirPath)
    )(Patterns(true, Resolver.mavenStyleBasePattern))
  }
}

publishArtifact in Test := false

pomExtra :=
  <url>http://github.com/krrrr38/sbt-sticker</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://www.opensource.org/licenses/mit-license.php</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:krrrr38/sbt-sticker.git</url>
      <connection>scm:git:git@github.com:krrrr38/sbt-sticker.git</connection>
    </scm>
    <developers>
      <developer>
        <id>krrrr38</id>
        <name>Ken Kaizu</name>
        <url>http://www.krrrr38.com</url>
      </developer>
    </developers>
