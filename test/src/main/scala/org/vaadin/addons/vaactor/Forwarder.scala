package org.vaadin.addons.vaactor

import akka.actor.{ Actor, ActorRef, Props }

import scala.collection.mutable

/** Helper for Tests with TestServlet
  *
  */
object Forwarder {

  val ForwarderName = "forwarder-actor"
  val SessionActorName = "session-actor"
  val UIActorName = "ui-actor"
  val VaactorActorName = "vaactor-actor"
  val TestActorName = "test-actor"

  val forwarder: ActorRef = VaactorServlet.system.actorOf(Props[ForwardActor], "forwarder")

  case class Register(name: String)

  case class Forward[T](name: String, msg: T)

  case class Lookup(name: String)

  case class Registered(name: String, actor: ActorRef)

  class ForwardActor extends Actor {
    val actors = mutable.Map.empty[String, ActorRef]

    def receive: Receive = {
      case Forward(name, msg) => actors.get(name) foreach { _.forward(msg) }
      case Lookup(name) => actors.get(name) foreach { sender ! Registered(name, _) }
      case Register(name) => actors += (name -> sender)
    }
  }

}
