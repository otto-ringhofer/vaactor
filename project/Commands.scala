import sbt._

object Commands {

  val testAll: Command = Command.command("testAll") { state =>
    "project addon" ::
      "test" ::
      "project test" ::
      "jetty:start" ::
      "testOnly org.vaadin.addons.vaactor.VaactorServletSpec" ::
      "testOnly org.vaadin.addons.vaactor.VaactorSessionSpec" ::
      "testOnly org.vaadin.addons.vaactor.VaactorUISpec" ::
      "testOnly org.vaadin.addons.vaactor.VaactorSpec" ::
      "jetty:stop" ::
      "project root" ::
      state
  }

  val allCommands = Seq(testAll)

}
