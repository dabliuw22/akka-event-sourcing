package com.leysoft.local

import java.util.Date

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

class LocalSalesActor(val id: String) extends PersistentActor with ActorLogging {

  var count: Long = 0L

  var total: Long = 0L

  val f = (event: Sale) => {
    count += 1
    total += event.product.amount
    log.info(s"Event: $event, count: $count, total: $total")
  }

  /**
    * Identificador único.
    */
  override def persistenceId: String = id

  /**
    * Default receive. En este método recibimos los mensajes y persistimos
    * eventos, además de realizar un manejo de los eventos y cambio de estado
    * por parte del actor de forma segura.
    */
  override def receiveCommand: Receive = {
    case product: Product =>
      persist(Sale(count, product)) {f}
    case products: List[Product] =>
      persistAll(products.zip(count to (count + products.length))
        .map(pair => Sale(pair._2, pair._1))) {f}
  }

  /**
    * En este método se recuperan los eventos persistidos luego de un inicio
    * o reinicio del actor y se realiza la actualización del estado del actor.
    */
  override def receiveRecover: Receive = {
    case sale: Sale =>
      count = sale.id
      total += sale.product.amount
      log.info(s"Recover: ${sale}, count: $count, total: $total")
  }

  /**
    * Método llamado si persist() falla. Detendra (STOP) al actor, por lo
    * que se recomeinda iniciar al actor dentro de un tiempo.
    */
  override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.error(s"Fail to persist event: $event because $cause")
    super.onPersistFailure(cause, event, seqNr)
  }

  /**
    * Método llamado si el Journal falla al persistir el evento. El actor
    * pasara a RESUME.
    */
  override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.error(s"Persist rejected for event: $event because $cause")
    super.onPersistRejected(cause, event, seqNr)
  }
}

object LocalSalesActor {

  def apply(id: String): LocalSalesActor = new LocalSalesActor(id)

  def props(id: String) = Props(LocalSalesActor(id))
}

case class Product(name: String, amount: Long, date: Date)

case class Sale(id: Long, product: Product)