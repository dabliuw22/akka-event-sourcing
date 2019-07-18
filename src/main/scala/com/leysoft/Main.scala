package com.leysoft

import akka.actor.ActorSystem
import com.leysoft.persistence._
import java.util.Date

import com.typesafe.config.ConfigFactory

import scala.util.Random

object Main extends App {
  //val config = ConfigFactory.load().getConfig("local")
  val config = ConfigFactory.load().getConfig("postgresql")
  val system = ActorSystem("event-sourcing-system", config)

  /*val salesActor = system.actorOf(LocalSalesActor
    .props("sales-id", system.actorOf(SimpleActor.props, "simple-actor")),
    "sales-actor")
  var products: List[Product] = Nil

  for(i <- 1 to 10) salesActor ! Product(i.toString, i * 100, new Date())
  for(i <- 11 to 20) products =
    products.appended(Product(i.toString, i * 100, new Date()))
  salesActor ! products*/

  val chatActor = system.actorOf(LocalChatActor.props("java", "scala"),
    "chat-actor")
  for (i <- 1 to 105) chatActor ! Message(s"message $i", new Date(),
    (Random.nextInt() % 2) == 0)
  chatActor ! "print"
}
