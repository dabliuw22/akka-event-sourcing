package com.leysoft

import akka.actor.ActorSystem
import com.leysoft.local.{LocalSalesActor, Product}
import java.util.{Date, UUID}

object Main extends App {
  val system = ActorSystem("event-sourcing-system")

  val salesActor = system.actorOf(LocalSalesActor
    .props(UUID.randomUUID().toString), "sales-actor")
  var products: List[Product] = Nil

  for(i <- 1 to 10) salesActor ! Product(i.toString, i * 100, new Date())
  for(i <- 11 to 20) products =
    products.appended(Product(i.toString, i * 100, new Date()))
  salesActor ! products
}
