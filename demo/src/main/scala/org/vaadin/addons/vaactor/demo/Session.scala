package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorSession
import org.vaadin.addons.vaactor.chat.ChatServer

import akka.actor.Actor
import akka.event.LoggingReceive

/** Contains session actor class and messages
  *
  * @author Otto Ringhofer
  */
object Session {

  /** Session state
    *
    * @param name name of user logged in
    */
  case class State(name: String = "")

  /** Login message, stores name in state
    *
    * @param name name of user
    */
  case class Login(name: String = "")

  /** Logout message, clears name in state */
  case object Logout

  /** Component of sender is attached to UI */
  case object Attached

  /** Component of sender is detached from UI */
  case object Detached

  /** Chat message, text and user name will be sent to chatroom
    *
    * @param msg text of message
    */
  case class Message(msg: String)

  /** Actor handling session state */
  class SessionActor extends Actor with VaactorSession[State] {

    /** Initial value of session state */
    override val initialSessionState = State()

    /** Behaviour of session, processes received messages */
    override val sessionBehaviour: Receive = LoggingReceive {
      // Chat statement from ui, send send text and user name to chatServer
      case Session.Message(msg) =>
        ChatServer.chatServer ! ChatServer.Statement(sessionState.name, msg)
      // Chat statement from chatroom, broadcast
      case msg: ChatServer.Statement =>
        broadcast(msg)
      // User entered chatroom, broadcast
      case msg: ChatServer.Enter =>
        broadcast(msg)
      // User left chatroom, broadcast
      case msg: ChatServer.Leave =>
        broadcast(msg)
      // List of users in chatroom, broadcast
      case msg: ChatServer.Members =>
        broadcast(msg)
      // User login, send Subscribe to chatServer
      case Session.Login(name) =>
        ChatServer.chatServer ! ChatServer.Subscribe(ChatServer.Client(name, self))
      // User logout, send Unsubscribe to chatServer
      case Session.Logout =>
        ChatServer.chatServer ! ChatServer.Unsubscribe(ChatServer.Client(sessionState.name, self))
      // Subscription successful, set state, broadcast
      case msg: ChatServer.SubscriptionSuccess =>
        sessionState = State(msg.name)
        broadcast(msg)
      // Subscription failure, broadcast
      case msg: ChatServer.SubscriptionFailure =>
        broadcast(msg)
      // Subscription cancelled, set state, broadcast
      case msg: ChatServer.SubscriptionCancelled =>
        sessionState = State()
        broadcast(msg)
      case Attached =>
        self ! VaactorSession.Subscribe(sender)
        if (sessionState.name.isEmpty)
          sender ! ChatServer.SubscriptionCancelled("")
        else
          sender ! ChatServer.SubscriptionSuccess(sessionState.name)
      case Detached =>
        self ! VaactorSession.Unsubscribe(sender)
        sender ! ChatServer.SubscriptionCancelled("")
    }

  }

}
