# Vaactor

Use [Vaadin Framework](https://vaadin.com/framework) 
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

val vaadinVersion = "8.1.5"
val akkaVersion = "2.5.6"
libraryDependencies ++= Seq(
  "org.vaadin.addons" % "vaactor" % "1.0.0",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.vaadin" % "vaadin-server" % vaadinVersion,
  "com.vaadin" % "vaadin-client-compiled" % vaadinVersion,
  "com.vaadin" % "vaadin-themes" % vaadinVersion,
  "com.vaadin" % "vaadin-push" % vaadinVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)
```
Note the `"org.vaadin.addons" % "vaactor" % "1.0.0"` line -
 Vaadin directory will deliver the library compiled with scala binary version 2.12 !

### Development

Implement a Servlet, an UI, a session Actor and Compnents in your Scala code,
 and extend them with traits from the vaactor library:

```scala
import javax.servlet.annotation.WebServlet

import ExampleObject.globalCnt
import org.vaadin.addons.vaactor._
import com.vaadin.annotations.{ Push, VaadinServletConfiguration }
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

import akka.actor.Actor.Receive
import akka.actor.{ Actor, Props }

object ExampleObject {
  // global counter
  private[this] var _globalCnt = 0

  def globalCnt: Int = this.synchronized { _globalCnt }

  def globalCnt_=(value: Int): Unit = this.synchronized { _globalCnt = value }

}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[ExampleUI]
)
class ExampleServlet extends VaactorServlet {

  override val sessionProps: Option[Props] = Some(Props(classOf[ExampleSessionActor]))

}

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class ExampleUI extends VaactorUI with Vaactor.UIVaactor {
  ui =>

  // counter local to this UI
  var uiCnt = 0

  val stateDisplay = new Label()
  val layout: VerticalLayout = new VerticalLayout {
    setMargin(true)
    setSpacing(true)
    addComponent(new Label("Vaactor Example") {
      addStyleName(ValoTheme.LABEL_H1)
    })
    addComponent(new Button("Click Me", { _ =>
      uiCnt += 1
      send2SessionActor(s"Thanks for clicking! (uiCnt:$uiCnt)")
    })
    )
    addComponent(stateDisplay)
    addComponent(new ExampleStateButton(ui))
  }

  override def init(request: VaadinRequest): Unit = { setContent(layout) }

  override def receive: Actor.Receive = {
    case hello: String =>
      globalCnt += 1
      stateDisplay.setValue(s"$hello (globalCnt:$globalCnt)")
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

class ExampleStateButton(val vaactorUI: VaactorUI) extends Button with Vaactor {

  setCaption("SessionState")
  addClickListener { _ => vaactorUI.sessionActor ! VaactorSession.RequestSessionState }

  override def receive: Receive = {
    case state: Int => setCaption(s"SessionState is $state")
  }

}
```

### Deployment

Vaactor applications are deployed as servlets.
During development you could use [xsb-web-plugin](http://earldouglas.com/projects/xsbt-web-plugin/).

```sbt
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.0")
```

The actual default version of jetty in the plugin has problems with Vaadin 8.1 and websockets,
so you should use the specific jetty version configured below.

```sbt
containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.3.21.v20170918" intransitive())

enablePlugins(JettyPlugin)
```

If you use the xsbt-web-plugin, start a web server `sbt jetty:start`
and your Vaactor application should be available at http://localhost:8080:

## License

Vaactor is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
