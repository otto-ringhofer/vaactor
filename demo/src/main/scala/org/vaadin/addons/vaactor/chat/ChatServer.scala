package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.{ Actor, ActorRef, Props }
import akka.event.LoggingReceive

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

  /** Subscription was successful, sent to client as response to Subscribe
    *
    * @param name subscribed name
    */
  case class SubscriptionSuccess(name: String)

  /** Subscription failed, sent to client as response to Subscribe
    *
    * @param error Error message
    */
  case class SubscriptionFailure(error: String)

  /** Subscription cancelled, sent to client as response to Unsubscribe
    *
    * @param name unsubscripted name
    */
  case class SubscriptionCancelled(name: String)

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
    def receive: Receive = LoggingReceive {
      // Subscribe from client
      case Subscribe(client) =>
        // no name, reply with failure
        if (client.name.isEmpty)
          sender ! SubscriptionFailure("Empty name not valid")
        // duplicate name, reply with failure
        else if (chatRoom.contains(client.name))
          sender ! SubscriptionFailure(s"Name '${ client.name }' already subscribed")
        // add client to chatroom, reply with success, brodcast Enter to clients
        else {
          chatRoom += client.name -> client
          sender ! SubscriptionSuccess(client.name)
          broadcast(Enter(client.name))
        }
      // Unsubscribe from client, reply with cancelled, broadcast Leave to clients, remove client from chatroom
      case Unsubscribe(client) =>
        if (chatRoom.contains(client.name)) {
          sender ! SubscriptionCancelled(client.name)
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
