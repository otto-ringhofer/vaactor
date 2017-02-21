package org.vaadin.addons.vaactor

import akka.actor.{ Actor, ActorRef, Props }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

/** contains guardian actor for all ui-actors
  *
  * @author Otto Ringhofer
  */
object Vaactor {

  val vaactorConfig = config.getConfig("vaactor")

  class Guardian extends Actor {

    private var uiGuardians: Int = 0

    def receive = {
      case props: Props =>
        uiGuardians += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$uiGuardians"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  /** guardian actor, creates all ui-actors */
  val guardian = VaactorServlet.system.actorOf(
    Props[Guardian], vaactorConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(vaactorConfig.getInt("ask-timeout").seconds)

  /** create an actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}

/** makes a class "feel" like an actor, but synchronized with vaadin ui
  *
  * creates actor, assigns it to implicit `self` value
  * `receive` is called in context of vaadin ui
  *
  * @author Otto Ringhofer
  */
trait Vaactor {
  vaactor =>

  /** VaactorUI of this component, used for access of sessionActor and access method */
  val vaactorUI: VaactorUI

  /** actor for this Vaactor */
  // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
  implicit lazy val self = vaactorUI.actorOf(Props(classOf[VaactorActor], vaactor))

  private def logUnprocessed: Actor.Receive = {
    case msg: Any =>
  }

  // lazy because receive is not yet initialized
  private lazy val receiveWorker = receive orElse logUnprocessed

  // forward message to receive function of ui, undefined messages are forwarded to logUnprocessed
  private[vaactor] def receiveMessage(msg: Any): Unit = vaactorUI.access(() => receiveWorker(msg))

  /** receive function, is called in context of vaadin ui (via ui.access) */
  def receive: Actor.Receive

}

private class VaactorActor(vaactor: Vaactor) extends Actor {

  def receive = {
    // catch all messages and forward to UI
    case msg: Any =>
      vaactor.receiveMessage(msg)
  }

}
