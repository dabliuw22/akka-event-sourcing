package com.leysoft.local

import java.util.Date

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

import scala.collection.mutable.Queue

class LocalChatActor(val from: String, val to: String) extends PersistentActor with ActorLogging {

  var id: Long = 0L

  var countMessages = 0

  val lastMessages = new Queue[(String, String)]()

  val f: (String, String) => Unit = (owner, message) => {
    if(lastMessages.size >= LocalChatActor.MAX_MESSAGE_IN_QUEUE)
      lastMessages.dequeue()
    lastMessages.enqueue(Tuple2[String, String](owner, message))
    id += 1
  }

  val snapshot: () => Unit = () => {
    countMessages += 1
    if(countMessages >= LocalChatActor.MAX_MESSAGE_IN_QUEUE) {
      log.info("Saving snapshop...")
      saveSnapshot(lastMessages)
      countMessages = 0
    }
  }

  override def persistenceId: String = s"$from-$to-id"

  override def receiveCommand: Receive = {
    case message: Message => message.sent match {
      case true =>
        persist(SentMessage(id, message)) {event =>
          log.info(s"Sent id: $id, message: $message")
          f(from, message.content)
          snapshot()
        }
      case _ =>
        persist(ReceiveMessage(id, message)) {event =>
          log.info(s"Receive id: $id, message: $message")
          f(to, message.content)
          snapshot()
        }
    }
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Saving snapshot succeeded: $metadata")
    case SaveSnapshotFailure(metadata, cause) =>
      log.error(s"saving snapshot $metadata failed because of $cause")
    case "print" => log.info(s"Last messages: $lastMessages")
  }

  override def receiveRecover: Receive = {
    case SentMessage(id, message) =>
      log.info(s"Recovered sent message $id: $message")
      f(from, message.content)
    case ReceiveMessage(id, message) =>
      log.info(s"Recovered received message $id: $message")
      f(to, message.content)
    case SnapshotOffer(metadata, s) =>
      log.info(s"Recovered snapshot: $metadata")
      s.asInstanceOf[Queue[(String, String)]] foreach {lastMessages.enqueue(_)}
  }
}

object LocalChatActor {

  val MAX_MESSAGE_IN_QUEUE = 10

  def apply(from: String, to: String): LocalChatActor = new LocalChatActor(from, to)

  def props(from: String, to: String) = Props(LocalChatActor(from, to))
}

case class Message(content: String, date: Date, sent: Boolean = false)

case class SentMessage(id: Long, message: Message)

case class ReceiveMessage(id: Long, message: Message)