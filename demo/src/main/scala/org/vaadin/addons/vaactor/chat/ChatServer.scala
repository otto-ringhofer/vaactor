package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.{ Actor, ActorRef, Props }

/** Contains ChatServer actor and messages
  *
  * @author Otto Ringhofer
  */
object ChatServer {

  /** Clients handled by chat room
    *
    * @param name  name of user
    * @param actor actorref for communication
    */
  case class Client(name: String, actor: ActorRef)

  /** Subscribe client to chatroom, processed by chatroom
    *
    * @param client enters chatroom
    */
  case class Subscribe(client: Client)

  /** Unsubscribe client from chatroom, processed by chatroom
    *
    * @param client leaves chatroom
    */
  case class Unsubscribe(client: Client)

  /** Subscription was successful, sent to client
    *
    * @param welcome Welcome text
    */
  case class SubscriptionSuccess(welcome: String)

  /** Subscription failed, sent to client
    *
    * @param error Error message
    */
  case class SubscriptionFailure(error: String)

  /** Statement in chatroom, processed by chatroom, sent to clients
    *
    * @param name name of user
    * @param msg  text of statement
    */
  case class Statement(name: String, msg: String)

  /** Request memberlist from chatroom, processed by chatroom */
  case object RequestMembers

  /** Client entered chatroom, sent to clients
    *
    * @param name name of user
    */
  case class Enter(name: String)

  /** Client left chatroom, sent to clients
    *
    * @param name name of user
    */
  case class Leave(name: String)

  /** Memberlist, sent to clients
    *
    * @param names list of user names of clients
    */
  case class Members(names: Seq[String])

  /** ActoRef of chatroom actor */
  val chatServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "chatServer")

  /** Actor handling chatroom */
  class ServerActor extends Actor {

    // List of clients in chatroom
    private var chatRoom = Map.empty[String, Client]

    /** Process received messages */
    def receive: PartialFunction[Any, Unit] = {
      // Subscribe from client
      case Subscribe(client) =>
        // duplicate name, reply with failure
        if (chatRoom.contains(client.name)) {
          sender ! SubscriptionFailure(s"Name '${ client.name }' already subscripted")
        }
        // add client to chatroom, reply with welcome, brodcast Enter to clients
        else {
          chatRoom += client.name -> client
          sender ! SubscriptionSuccess(s"Welcome ${ client.name }")
          broadcast(Enter(client.name))
        }
      // Unsubscribe from client, broadcast Leave to clients, remove client from chatroom
      case Unsubscribe(client) =>
        if (chatRoom.contains(client.name)) {
          broadcast(Leave(client.name))
          chatRoom -= client.name
        }
      // Statement from client, broadcast to clients
      case msg: Statement =>
        broadcast(msg)
      // RequestMembers from client, send member list to sending client
      case RequestMembers =>
        sender ! Members(chatRoom.keySet.toList)
    }

    /** Send message to every client in chatroom
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = chatRoom foreach { _._2.actor ! msg }

  }

}
