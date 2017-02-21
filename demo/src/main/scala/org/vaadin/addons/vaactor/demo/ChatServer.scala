package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.{ Actor, ActorRef, Props }

import scala.collection.mutable

/** contains ChatServer actor and messages
  *
  * @author Otto Ringhofer
  */
object ChatServer {

  /** clients handled by chat room
    *
    * @param name  name of user
    * @param actor actorref for communication
    */
  case class Client(name: String, actor: ActorRef)

  /** subscribe client to chatroom, sent to chatroom
    *
    * @param client enters chatroom
    */
  case class Subscribe(client: Client)

  /** unsubscribe client from chatroom, sent to chatroom
    *
    * @param client leaves chatroom
    */
  case class Unsubscribe(client: Client)

  /** statement in chatroom, sent to chatroom, sent to clients
    *
    * @param name name of user
    * @param msg  text of statement
    */
  case class Statement(name: String, msg: String)

  /** request memberlist from chatroom, sent to chatroom */
  case object RequestMembers

  /** client entered chatroom, sent to clients
    *
    * @param name name of user
    */
  case class Enter(name: String)

  /** client left chatroom, sent to clients
    *
    * @param name name of user
    */
  case class Leave(name: String)

  /** memberlist, sent to clients
    *
    * @param names list of user names of clients
    */
  case class Members(names: Seq[String])

  /** actorref of chatroom actor */
  val chatServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "chatServer")

  /** actor handling chatroom */
  class ServerActor extends Actor {

    // list of clients in chatroom
    private val chatRoom = mutable.ListBuffer.empty[Client]

    /** process received messages */
    def receive: PartialFunction[Any, Unit] = {
      // subscribe from client, add client to chatroom, send enter to all clients
      case Subscribe(client) =>
        chatRoom += client
        broadcast(Enter(client.name))
      // unsubscribe from client, send leave to all clients, remove client from chatroom
      case Unsubscribe(client) =>
        broadcast(Leave(client.name))
        chatRoom -= client
      // statement from client, send to all clients
      case msg: Statement =>
        broadcast(msg)
      // request from client, send member list to this client
      case RequestMembers =>
        sender ! Members(chatRoom map { _.name })
    }

    /** send message to every client in chatroom
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = for (client <- chatRoom) client.actor ! msg

  }

}
