# Vaactor

Use [Vaadin Flow](https://vaadin.com/flow) 
with [Scala](http://www.scala-lang.org/)
and [Akka](http://akka.io/) [actors](http://doc.akka.io/docs/akka/current/scala/actors.html).

## Documentation
Detailed documentation can be found in the ScalaDoc of the library.

The project on [Github](https://github.com/otto-ringhofer/vaactor/tree/develop)
 also contains two subprojects with example code.

The [example subproject](https://github.com/otto-ringhofer/vaactor/tree/develop/example)
 is the example used here in this description.

The [demo subproject](https://github.com/otto-ringhofer/vaactor/tree/develop/demo)
 is a complete chat application with two interfaces - 
 one using session state and one without session state.

## How to use it?

Vaactor is implemented in Scala 2.12.
You can use it in every Scala project that uses Vaadin and Akka Actors.

### Dependencies

Add all needed dependencies (Vaactor, Vaadin and Akka) to your Scala project
(using [sbt](http://www.scala-sbt.org/)):

```sbt
resolvers ++= Seq(
  "vaadin-addons" at "http://maven.vaadin.com/vaadin-addons"
)

val vaadinVersion = "10.0.1"
val akkaVersion = "2.5.13"
libraryDependencies ++= Seq(
  "org.vaadin.addons" % "vaactor" % "2.0.0",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.vaadin" % "vaadin-core" % vaadinVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)
```
Note the `"org.vaadin.addons" % "vaactor" % "2.0.0"` line -
 Vaadin directory will deliver the library compiled with scala binary version 2.12 !

### Development

Implement a Servlet, an UI Component and a session Actor in your Scala code,
 and extend them with traits from the vaactor library:

```scala
import javax.servlet.annotation.WebServlet

import ExampleObject.globalCnt
import org.vaadin.addons.vaactor._
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinServletConfiguration
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

import akka.actor.Actor.Receive
import akka.actor.{ Actor, Props }


object ExampleObject {
  // global counter
  private[this] var _globalCnt = 0

  def globalCnt: Int = this.synchronized { _globalCnt }

  def globalCnt_=(value: Int): Unit = this.synchronized { _globalCnt = value }

}

@WebServlet(urlPatterns = Array("/*"), asyncSupported = true)
@VaadinServletConfiguration(productionMode = false)
class ExampleServlet extends VaactorSessionServlet {

  override val sessionProps: Props = Props(classOf[ExampleSessionActor])

}

@Route("")
@Theme(value = classOf[Lumo], variant = Lumo.DARK)
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
class ExampleUI extends VerticalLayout with Vaactor.HasActor with Vaactor.HasSession {

  // counter local to this UI
  var uiCnt = 0

  val stateDisplay = new Label()

  setMargin(true)
  setSpacing(true)
  add(new Label("Vaactor Example"))
  add(
    new Button("Click Me", { _ =>
      uiCnt += 1
      session ! s"Thanks for clicking! (uiCnt:$uiCnt)"
    })
  )
  add(stateDisplay)
  add(
    new Button("Show Counts") with Vaactor.HasActor {
      addClickListener(_ => session ! VaactorSession.RequestSessionState)

      override def receive: Receive = {
        case state: Int => setText(s"Show Counts - uiCnt is $uiCnt, sessionCnt is $state, globalCnt is $globalCnt")
      }
    }
  )

  override def receive: Receive = {
    case hello: String =>
      globalCnt += 1
      stateDisplay.setText(s"$hello (globalCnt:$globalCnt)")
  }

}

class ExampleSessionActor extends Actor with VaactorSession[Int] {
  // state is session counter
  override val initialSessionState = 0

  override val sessionBehaviour: Receive = {
    case msg: String =>
      sessionState += 1
      sender ! s"$msg (sessionCnt:$sessionState)"
  }

}
```

### Deployment

Vaactor applications are deployed as servlets.
During development you could use [xsb-web-plugin](http://earldouglas.com/projects/xsbt-web-plugin/).

```sbt
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.2")
```

The actual default version of jetty in the plugin has problems with Vaadin 10.0 and websockets,
so you should use the specific jetty version configured below.

```sbt
containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.3.21.v20170918" intransitive())

enablePlugins(JettyPlugin)
```

If you use the xsbt-web-plugin, start a web server `sbt jetty:start`
and your Vaactor application should be available at http://localhost:8080:

## License

Vaactor is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
