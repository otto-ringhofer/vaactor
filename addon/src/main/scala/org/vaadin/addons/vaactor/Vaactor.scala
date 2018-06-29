package org.vaadin.addons.vaactor

import Vaactor._
import com.typesafe.config.Config
import com.vaadin.flow.component.{ AttachEvent, Component, DetachEvent, UI }

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration._

/** Contains the Vaactor traits and the [[ProxyActor]] class.
  *
  * @author Otto Ringhofer
  */
object Vaactor {

  /** Guardian, creates and supervises all [[ProxyActor]] */
  class ProxyGuardian extends Actor {

    private var proxies: Int = 0

    def receive: Receive = {
      case props: Props =>
        proxies += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$proxies"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  /** `vaactor`-subtree of Vaactor configuration */
  val vaactorConfig: Config = config.getConfig("vaactor")

  /** [[ProxyGuardian]] Actor, creates all [[ProxyActor]] */
  val guardian: ActorRef = VaactorServlet.system.actorOf(
    Props[ProxyGuardian], vaactorConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(vaactorConfig.getInt("ask-timeout").seconds)

  /** Create an Actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created Actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)


  /** Proxy Actor for [[HasActor]],
    * calls [[HasActor.receiveMessage]] of dedicated [[HasActor]] with every message received.
    *
    * [[HasActor.receiveMessage]] calls [[HasActor.receive]] function
    * synchronized by `access` method of dedicated [[HasActor.ui]].
    *
    * @param hasActor dedicated [[HasActor]]
    */
  class ProxyActor(hasActor: HasActor) extends Actor {

    def receive: Receive = {
      // catch all messages and forward to UI
      case msg: Any => hasActor.receiveMessage(msg, sender)
    }

  }

  /** Vaadin Component with Actor.
    *
    * Makes the Component "feel" like an Actor, with `receive` method synchronized with UI.
    *
    * Creates the Actor, assigns it to implicit `self` value,
    * `receive` is called in context of UI.
    *
    * @author Otto Ringhofer
    */
  trait HasActor extends Component {

    private var _sender: ActorRef = Actor.noSender
    private var _ui: UI = _

    lazy val ui: UI = _ui

    abstract override def onAttach(attachEvent: AttachEvent): Unit = {
      super.onAttach(attachEvent)
      _ui = UI.getCurrent
    }

    abstract override def onDetach(detachEvent: DetachEvent): Unit = {
      self ! PoisonPill
      super.onDetach(detachEvent)
    }

    /** Actor dedicated to this HasActor */
    // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
    implicit lazy val self: ActorRef = actorOf(Props(classOf[ProxyActor], this))

    /** The reference sender Actor of the last received message.
      *
      * WARNING: Only valid within [[receive]] of the Vaactor itself,
      * so do not close over it and publish it to other threads!
      */
    def sender: ActorRef = _sender

    /** Handles all messages not handled by [[receive]].
      *
      * Overriding is encouraged.
      * Default implementation forwards message to deadLetters Actor of ActorSystem.
      *
      * @param msg    message
      * @param sender sender of message
      */
    def orElse(msg: Any, sender: ActorRef): Unit =
      VaactorServlet.system.deadLetters.tell(msg, sender) // forward not possible, no ActorContext available

    /** Call [[receive]] of this trait synchronized by UI.access.
      * Is used by [[ProxyActor]] to forward received messages to this trait.
      *
      * @param msg    message
      * @param sender sender of message
      */
    def receiveMessage(msg: Any, sender: ActorRef): Unit = {
      _sender = sender
      ui.access(() => receive.applyOrElse(msg, (mp: Any) => orElse(mp, sender)))
      _sender = Actor.noSender
    }

    /** Receive function, is called in context of UI (via UI.access) */
    def receive: Actor.Receive

  }

  /** Vaadin Component with session Actor. */
  trait HasSession extends Component {

    private var _session: ActorRef = _

    lazy val session: ActorRef = _session

    abstract override def onAttach(attachEvent: AttachEvent): Unit = {
      super.onAttach(attachEvent)
      _session = VaactorVaadinSession.lookupSessionActor(attachEvent.getSession)
    }

  }

  /** Vaadin Component with session Actor and automatic attach/detach message to session Actor. */
  trait AttachSession extends HasSession {

    /** Message sent to session Actor on attach of Component */
    val attachMessage: Any

    /** Message sent to session Actor on detach of Component */
    val detachMessage: Any

    abstract override def onAttach(attachEvent: AttachEvent): Unit = {
      super.onAttach(attachEvent)
      session ! attachMessage
    }

    abstract override def onDetach(detachEvent: DetachEvent): Unit = {
      session ! detachMessage
      super.onDetach(detachEvent)
    }

  }

  /** Vaadin Component with session Actor and automatic subscription to session Actor. */
  trait SubscribeSession extends AttachSession {

    /** Subscribe message sent to session Actor on attach of Component */
    override val attachMessage: Any = VaactorSession.Subscribe

    /** Unsubscribe message sent to session Actor on detach of Component */
    override val detachMessage: Any = VaactorSession.Unsubscribe

  }

}
