object ScalaConfig {

  val scalaBinVersion = "2.12"

  val version = s"${ scalaBinVersion }.4"

  val crossVersions = Seq(version)

  val compileOptions = Seq("-feature", "-unchecked", "-deprecation")

  val docCompileOptions = Seq("-groups", "-implicits", "-diagrams")

}
