package com.leysoft.local

import java.util.Date

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

class LocalSalesActor(val id: String) extends PersistentActor with ActorLogging {

  var generatedId: Long = 0L

  var totalAmount: Long = 0L

  override def persistenceId: String = id

  override def receiveCommand: Receive = {
    case product: Product =>
      persist(Sale(generatedId, product)) {event =>
        generatedId += 1
        totalAmount += product.amount
        log.info(s"Event: $event, total: $totalAmount")
      }
  }

  override def receiveRecover: Receive = {
    case sale: Sale =>
      generatedId = sale.id
      totalAmount += sale.product.amount
      log.info(s"Recover: ${sale.id}, total: $totalAmount")
  }
}

object LocalSalesActor {

  def apply(id: String): LocalSalesActor = new LocalSalesActor(id)

  def props(id: String) = Props(LocalSalesActor(id))
}

case class Product(name: String, amount: Long, date: Date)

case class Sale(id: Long, product: Product)