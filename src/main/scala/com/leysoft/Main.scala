package com.leysoft

import akka.actor.{ActorSystem, Props}
import akka.pattern.{BackoffOpts, BackoffSupervisor}
import com.leysoft.persistence._
import com.typesafe.config.ConfigFactory
import java.util.Date

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object Main extends App {
  // Estrategia exponencial para supervision de actores persistentes.
  // No detener inmediatamente.
  val backoffSupervisorProps: (Props, String) => Props = (props, name) => {
    BackoffSupervisor.props(BackoffOpts.onStop(
      childProps = props, childName = name,
      minBackoff = 5 seconds, maxBackoff = 30 seconds,
      randomFactor = 0.3))
  }

  //val config = ConfigFactory.load().getConfig("local")
  val config = ConfigFactory.load().getConfig("local-event-adapter")
  val system = ActorSystem("event-sourcing-system", config)

  /*val salesActor = system.actorOf(SalesActor
    .props("sales-id", system.actorOf(SimpleActor.props, "simple-actor")),
    "sales-actor")

  for(i <- 1 to 10) salesActor ! Product(i.toString, i * 100, new Date())
  val products = for(i <- 11 to 20) yield Product(i.toString, i * 100, new Date())
  salesActor ! products*/

  val chatActor = system.actorOf(backoffSupervisorProps(ChatActor
    .props("java", "scala"), "chat-actor"), "chat-actor")
  for (i <- 1 to 105) chatActor ! Message(s"message $i", new Date(),
    (Random.nextInt() % 2) == 0)
  chatActor ! "print"
}
