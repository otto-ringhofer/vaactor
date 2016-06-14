package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorSession
import org.vaadin.addons.vaactor.demo.ChatServer._

import akka.actor.Actor

object ChatSession {

  case class State(name: String = "") {
    def isLoggedIn = !name.isEmpty
  }

  case class Login(name: String = "")

  case object Logout

  case class Message(msg: String)

  class SessionActor extends Actor with VaactorSession[State] {

    override val initialSession = State()

    override val sessionBehaviour: Receive = {
      case Message(msg) =>
        chatServer ! Statement(session.name, msg)
      case msg: Statement =>
        broadcast(msg)
      case msg: Enter =>
        broadcast(msg)
      case msg: Leave =>
        broadcast(msg)
      case msg: Members =>
        broadcast(msg)
      case Login(name) =>
        if (!name.isEmpty) {
          // perform login
          session = session.copy(name = name)
          chatServer ! Subscribe(Client(session.name, self))
          broadcast(session)
        }
        if (session.isLoggedIn) // is/was logged in
          chatServer ! RequestMembers
      case Logout =>
        chatServer ! Unsubscribe(Client(session.name, self))
        session = session.copy(name = "")
        broadcast(session)
        broadcast(Members(Nil))
    }

  }

}
