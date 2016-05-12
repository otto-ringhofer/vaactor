package org.vaadin.addons.vaactors.demo

import org.vaadin.addons.vaactors.VaactorsSession
import org.vaadin.addons.vaactors.demo.ChatServer._

import akka.actor.Actor

object ChatSession {

  case class State(name: String = "") {
    def isLoggedIn = !name.isEmpty
  }

  case class Login(name: String = "")

  case object Logout

  case class Message(msg: String)

  class SessionActor extends Actor with VaactorsSession[State] {

    private var _session = State()

    def session = _session

    def chatBehaviour: Receive = {
      case m @ Message(msg) =>
        chatServer ! Statement(session.name, msg)
      case msg: Statement =>
        broadcast(msg)
      case msg: Enter =>
        broadcast(msg)
      case msg: Leave =>
        broadcast(msg)
      case msg: Members =>
        broadcast(msg)
      case m @ Login(name) =>
        if (!name.isEmpty) {
          // perform login
          _session = session.copy(name = name)
          chatServer ! Subscribe(Client(session.name, self))
          broadcast(session)
        }
        if (session.isLoggedIn) // is/was logged in
          chatServer ! RequestMembers
      case Logout =>
        chatServer ! Unsubscribe(Client(session.name, self))
        _session = session.copy(name = "")
        broadcast(session)
        broadcast(Members(Nil))
    }

    def receive = chatBehaviour orElse sessionBehaviour

  }

}
