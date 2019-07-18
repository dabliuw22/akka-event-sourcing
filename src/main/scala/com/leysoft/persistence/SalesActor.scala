package com.leysoft.persistence

import java.util.Date

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.journal.{EventAdapter, EventSeq, ReadEventAdapter}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import DomainModel._
import DataModel._

class SalesActor(val id: String, val actor: ActorRef) extends PersistentActor with ActorLogging {

  var count: Long = 0L

  var total: Long = 0L

  val f = (event: Sale) => {
    count += 1
    total += event.product.amount
    actor ! event
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
      persist(Sale(count, product)) { event => f(event)
        log.info(s"Event: $event, count: $count, total: $total")
      }
    case products: List[Product] =>
      persistAll(products.zip(count to (count + products.length))
        .map(pair => Sale(pair._2, pair._1))) {event => f(event)
        log.info(s"Event: $event, count: $count, total: $total")
      }
  }

  /**
    * En este método se recuperan los eventos persistidos luego de un inicio
    * o reinicio del actor y se realiza la actualización del estado del actor.
    */
  override def receiveRecover: Receive = {
    case sale: Sale =>
      count += sale.id
      total += sale.product.amount
      log.info(s"Recover: $sale, count: $count, total: $total")
    case RecoveryCompleted => count += 1
  }

  /**
    * Método llamado si persist() (enviando el evento) falla o el jornal no esté disponible.
    * Detendra (STOP) al actor,
    * por lo que se recomeinda iniciar al actor dentro de un tiempo.
    */
  override def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.error(s"Fail to persist event: $event because $cause")
    super.onPersistFailure(cause, event, seqNr)
  }

  /**
    * Método llamado si el Journal falla al persistir (escribir) el evento. El actor
    * pasara a RESUME.
    */
  override def onPersistRejected(cause: Throwable, event: Any, seqNr: Long): Unit = {
    log.error(s"Persist rejected for event: $event because $cause")
    super.onPersistRejected(cause, event, seqNr)
  }

  /**
    * Método llamado si ocurre un fallo en receiveRecover() o el jornal no esté disponible.
    * Detendra (STOP) al actor.
    */
  override def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    super.onRecoveryFailure(cause, event)
  }
}

object SalesActor {

  def apply(id: String, actor: ActorRef): SalesActor =
    new SalesActor(id, actor)

  def props(id: String, actor: ActorRef) = Props(SalesActor(id, actor))
}

object DomainModel {

  case class Product(name: String, amount: Long, date: Date)

  case class Sale(id: Long, product: Product)

  case class SaleOffer(id: Long, product: Product, offer: Double)
}

object DataModel {

  case class SaleData(id: Long, name: String, amount: Long, date: Date)

  case class SaleOfferData(id: Long, name: String, amount: Long, date: Date, offer: Double)
}

class SaleReadEventAdapter extends ReadEventAdapter {

  //journal->serializer->readEventAdapter->actor
  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case sale: SaleData => EventSeq.single(SaleOfferData(sale.id,
      sale.name, sale.amount, sale.date, 0D))
    case offer: SaleOfferData => EventSeq.single(offer)
    case _ => throw new IllegalArgumentException
  }
}

class SaleEventAdapter extends EventAdapter {

  override def manifest(event: Any): String = "SMF"

  // journal->serializer->fromJournal->actor
  override def fromJournal(event: Any, manifest: String): EventSeq = event match {
    case SaleData(id, name, amount, date) =>
      EventSeq.single(Sale(id, Product(name, amount, date)))
    case SaleOfferData(id, name, amount, date, offer) =>
      EventSeq.single(SaleOffer(id, Product(name, amount, date), offer))
  }

  // actor->toJournal->serializer->journal
  override def toJournal(event: Any): Any = event match {
    case Sale(id, product) =>
      SaleData(id, product.name, product.amount, product.date)
    case SaleOffer(id, product, offer) =>
      SaleOfferData(id, product.name, product.amount, product.date, offer)
  }
}