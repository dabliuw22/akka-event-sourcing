package com.leysoft

import akka.actor.ActorSystem

import com.leysoft.local.{LocalSalesActor, Product}

import java.util.Date

object Main extends App {
  val system = ActorSystem("event-sourcing-system")

  val salesActor = system.actorOf(LocalSalesActor.props("id-local"),
    "sales-actor")

  for(i <- 1 to 10) salesActor ! Product(i.toString, i * 100, new Date())
}
