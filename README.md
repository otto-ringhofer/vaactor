# Vaactor

Use [Vaadin Framework](https://vaadin.com) with [Scala](http://www.scala-lang.org/)
and [Akka](http://akka.io/) [actors](http://doc.akka.io/docs/akka/2.4.7/scala/index-actors.html)
(uses [Scaladin addon](https://vaadin.com/directory#!addon/scaladin)).

## How to use it?

Vaactor requires Scaladin 3.2, Scaladin 3.2 requires Vaadin 7.5 and Scala 2.11.

1\. Add dependencies to Vaactor, Scaladin, Vaadin and Akka to your Scala project
(using [sbt](http://www.scala-sbt.org/) here) (Use Akka version 2.3.15 if you need Java 6 compatibility):

```sbt
resolvers += "Scaladin Snapshots" at "http://henrikerola.github.io/repository/snapshots/"

libraryDependencies ++= Seq(
  "org.vaadin.addons" %% "vaactor" % "0.1.0",
  "org.vaadin.addons" %% "scaladin" % "3.2-SNAPSHOT",
  "com.vaadin" % "vaadin-server" % "7.5.10",
  "com.vaadin" % "vaadin-client-compiled" % "7.5.10",
  "com.vaadin" % "vaadin-themes" % "7.5.10",
  "com.vaadin" % "vaadin-push" % "7.5.10",
  "com.typesafe.akka" %% "akka-actor" % "2.4.7"
)
```

2\. Vaactor applications are deployed as servlets, during the development time you could use [xsb-web-plugin](http://earldouglas.com/projects/xsbt-web-plugin/).

3\. Define a VaactorServlet, a VaactorUI and a session Actor:

```scala
package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.{ VaactorServlet, VaactorSession, VaactorUI }

import akka.actor.{ Actor, Props }
import vaadin.scala._
import vaadin.scala.server.ScaladinRequest

@WebServlet(urlPatterns = Array("/*"))
class ExampleServlet extends VaactorServlet(classOf[ExampleUI]) {

  override val sessionProps: Props = Props(classOf[ExampleSessionActor])

}

class ExampleUI extends VaactorUI {

  val layout = new VerticalLayout {
    margin = true
    spacing = true
    addComponent(new Label {
      value = "Vaactor Example"
      styleNames += ValoTheme.LabelH1
    })
    addComponent(Button("Click Me", { e =>
      vaactorUI.sessionActor ! "Thanks for clicking!"
    }))
  }

  override def initVaactorUI(request: ScaladinRequest): Unit = { content = layout }

  def receive = {
    case hello: String => layout.addComponent(Label(hello))
  }

}

class ExampleSessionActor extends Actor with VaactorSession[String] {

  override val initialSession = ""

  override val sessionBehaviour: Receive = {
    case name: String =>
      session = name
      sender ! s"Session received: $session"
  }

}
```

4\. If you use xsbt-web-plugin, start a web server by saying `sbt ~jetty:start`
and your Vaactor application should be available at http://localhost:8080:

## License

Vaactor is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
