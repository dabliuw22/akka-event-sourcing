package com.leysoft.local

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor

import scala.collection.mutable.{HashMap, HashSet}

class LocalVotesActor extends PersistentActor with ActorLogging {

  val persons = HashSet[Person]()

  val votes = HashMap[Person, Int]()

  val f: (VoteEvent => Unit) = event => {
    if(!persons.contains(event.vote.person)) {
      persons.add(event.vote.person)
      val key = event.vote.candidate
      val count = votes.getOrElse(key, 0)
      votes.put(key, count + 1)
      log.info(s"Event: $event, count: ${count + 1}")
    }
  }

  override def persistenceId: String = "votes-id"

  override def receiveCommand: Receive = {
    case vote: Vote =>
      persist(VoteEvent(vote))(f)
    case "print" => log.info(s"Votes: $votes, Persons: $persons")
  }

  override def receiveRecover: Receive = {
    case VoteEvent => f
  }
}

object LocalVotesActor {

  def apply: LocalVotesActor = new LocalVotesActor()

  def props: Props = Props[LocalVotesActor]
}

sealed case class Person(id: String, name: String)

class Candidate(override val id: String, override val name: String,
                val politicalParty: String) extends Person(id, name)

case class Vote(person: Person, candidate: Candidate)

case class VoteEvent(vote: Vote)
