package org.vaadin.addons.vaactors.demo

import ChatServer._
import org.vaadin.addons.vaactors.VaactorsServlet

import akka.actor.{ Actor, ActorRef, Props }

import scala.collection.mutable

object ChatServer {

  case class Client(name: String, actor: ActorRef)

  case class Subscribe(client: Client)

  case class Unsubscribe(client: Client)

  case class Statement(name: String, msg: String)

  case object RequestMembers

  case class Enter(name: String)

  case class Leave(name: String)

  case class Members(names: Seq[String])

  val chatServer = VaactorsServlet.system.actorOf(Props[ServerActor], "chatServer")

}

private class ServerActor extends Actor {

  private val chatRoom = mutable.ListBuffer.empty[Client]

  def receive = {
    case m @ Subscribe(client) =>
      chatRoom += client
      broadcast(Enter(client.name))
    case m @ Unsubscribe(client) =>
      broadcast(Leave(client.name))
      chatRoom -= client
    case msg: Statement =>
      broadcast(msg)
    case RequestMembers =>
      sender ! Members(chatRoom map { _.name })
  }

  def broadcast(msg: Any): Unit = for (client <- chatRoom) client.actor ! msg

}
