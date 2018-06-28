package org.vaadin.addons.vaactor

import Vaactor._
import com.typesafe.config.Config
import com.vaadin.flow.component.{ AttachEvent, Component, DetachEvent, UI }

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration._

/** Contains some utility traits and the [[VaactorProxyActor]] class.
  *
  * @author Otto Ringhofer
  */
object Vaactor {

  /** UiGuardian, creates and supervises all vaactor actors */
  class VaactorGuardian extends Actor {

    private var uis: Int = 0

    def receive: Receive = {
      case props: Props =>
        uis += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$uis"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  /** `vaactor`-subtree of Vaactor configuration */
  val vaactorConfig: Config = config.getConfig("vaactor")


  /** [[VaactorGuardian]] actor, creates all vaactor-actors */
  val guardian: ActorRef = VaactorServlet.system.actorOf(
    Props[VaactorGuardian], vaactorConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(vaactorConfig.getInt("ask-timeout").seconds)

  /** Create an actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)


  /** Proxy actor for [[Vaactor]],
    * calls [[HasActor.receiveMessage]] of dedicated [[Vaactor]] with every message received.
    *
    * [[HasActor.receiveMessage]] calls [[HasActor.receive]] function
    * synchronized by `access` method of dedicated [[HasActor.ui]].
    *
    * @param hasActor dedicated [[Vaactor]]
    */
  class VaactorProxyActor(hasActor: HasActor) extends Actor {

    def receive: Receive = {
      // catch all messages and forward to UI
      case msg: Any => hasActor.receiveMessage(msg, sender)
    }

  }

  /** Makes a class "feel" like an actor, with `receive` method synchronized with VaadinUI
    *
    * Creates actor, assigns it to implicit `self` value,
    * `receive` is called in context of VaadinUI
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

    /** Actor dedicated to this Vaactor */
    // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
    implicit lazy val self: ActorRef = actorOf(Props(classOf[VaactorProxyActor], this))

    /** The reference sender Actor of the last received message.
      *
      * WARNING: Only valid within [[receive]] of the Vaactor itself,
      * so do not close over it and publish it to other threads!
      */
    def sender: ActorRef = _sender

    /** Handles all messages not handled by [[receive]].
      *
      * Overriding is encouraged.
      * Default implementation forwards message to deadLetters actor of actor system.
      *
      * @param msg    message
      * @param sender sender of message
      */
    def orElse(msg: Any, sender: ActorRef): Unit =
      VaactorServlet.system.deadLetters.tell(msg, sender) // forward not possible, no ActorContext available

    /** Call [[receive]] of this trait synchronized by VaactorUI.access.
      * Is used by [[Vaactor.VaactorProxyActor]] to forward received messages to this trait.
      *
      * @param msg    message
      * @param sender sender of message
      */
    def receiveMessage(msg: Any, sender: ActorRef): Unit = {
      _sender = sender
      ui.access(() => receive.applyOrElse(msg, (mp: Any) => orElse(mp, sender)))
      _sender = Actor.noSender
    }

    /** Receive function, is called in context of VaadinUI (via ui.access) */
    def receive: Actor.Receive

  }

  trait HasSession extends Component {

    private var _session: ActorRef = _

    lazy val session: ActorRef = _session

    abstract override def onAttach(attachEvent: AttachEvent): Unit = {
      super.onAttach(attachEvent)
      _session = VaactorVaadinSession.lookupSessionActor(attachEvent.getSession)
    }

  }

  /** Vaadin Component with dedicated actor and automatic attach/detach message to session actor.
    */
  trait AttachSession extends HasSession {

    /** Message sent to session actor on attach of component */
    val attachMessage: Any

    /** Message sent to session actor on detach of component */
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

  /** Vaadin Component with dedicated actor and automatic subscription to session actor.
    */
  trait SubscribeSession extends AttachSession {
    hasSession: HasSession =>

    /** Subscribe message sent to session actor on attach of component */
    override val attachMessage: Any = VaactorSession.Subscribe

    /** Unsubscribe message sent to session actor on detach of component */
    override val detachMessage: Any = VaactorSession.Unsubscribe

  }

}
