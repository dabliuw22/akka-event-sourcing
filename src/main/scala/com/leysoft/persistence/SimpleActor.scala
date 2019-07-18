package com.leysoft.persistence

import akka.actor.{Actor, ActorLogging, Props}

class SimpleActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case message => log.info(s"Message: $message")
  }
}

object SimpleActor {

  def apply: SimpleActor = new SimpleActor()

  def props: Props = Props[SimpleActor]
}
