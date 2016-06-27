package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorSession
import org.vaadin.addons.vaactor.demo.ChatServer._

import akka.actor.Actor

/** contains session actor class and messages */
object ChatSession {

  /** session state
    *
    * @param name name of user logged in
    */
  case class State(name: String = "") {
    def isLoggedIn = !name.isEmpty
  }

  /** login message, stores name in state
    *
    * @param name name of user
    */
  case class Login(name: String = "")

  /** logout message, clears name in state */
  case object Logout

  /** chat message, text and user name will be sent to chatroom
    *
    * @param msg text of message
    */
  case class Message(msg: String)

  /** actor handling session state */
  class SessionActor extends Actor with VaactorSession[State] {

    /** initialm value of session state */
    override val initialSession = State()

    /** behaviour of session, processes received messages */
    override val sessionBehaviour: Receive = {
      // chat statement from ui, send send text and user name to chatroom
      case Message(msg) =>
        chatServer ! Statement(session.name, msg)
      // chat statement from chatroom, send to all registerd uis
      case msg: Statement =>
        broadcast(msg)
      // user entered chatroom, send to all registered uis
      case msg: Enter =>
        broadcast(msg)
      // user left chatroom, send to all registered uis
      case msg: Leave =>
        broadcast(msg)
      // list of users in chatroom, send to all registered uis
      case msg: Members =>
        broadcast(msg)
      // user login, set state und subscribe in chatroom, send state to all rgistered uis
      case Login(name) =>
        if (!name.isEmpty) {
          // perform login
          session = session.copy(name = name)
          chatServer ! Subscribe(Client(session.name, self))
          broadcast(session)
        }
        if (session.isLoggedIn) // is/was logged in
          chatServer ! RequestMembers
      // user logout, set state und unsubscribe from chatroom, send state to all rgistered uis
      case Logout =>
        chatServer ! Unsubscribe(Client(session.name, self))
        session = session.copy(name = "")
        broadcast(session)
        broadcast(Members(Nil))
    }

  }

}
