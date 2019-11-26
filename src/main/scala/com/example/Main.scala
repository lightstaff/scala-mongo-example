package com.example

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import cats.data.Kleisli
import cats.implicits._
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._

object Main extends App {

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  val mongoClient = MongoClient(
    "mongodb://root:1234@localhost:27017/?authSource=admin"
  )

  val mongoDb = mongoClient
    .getDatabase("test_db")
    .withCodecRegistry(
      CodecRegistries.fromRegistries(
        CodecRegistries.fromProviders(classOf[A], classOf[B]),
        DEFAULT_CODEC_REGISTRY
      )
    )

  val mongoCollection = mongoDb.getCollection[A]("test")

  val id1 = new ObjectId()

  val id2 = new ObjectId()

  val id3 = new ObjectId()

  println("=========== start version of normal ==========")

  val f1 = for {
    _ <- mongoCollection.drop()
    _ <- mongoCollection.insertMany(
          Seq(A(id1, "a_1", Seq(B("b_1"), B("b_2"))),
              A(id2, "a_2", Seq.empty[B]),
              A(id3, "a_3", Seq.empty[B]))
        )
    _ <- mongoCollection.updateOne(
          equal("_id", id2),
          combine(set("name", "a_4"), set("children", Seq(B("b_3"))))
        )
    _ <- mongoCollection.deleteOne(equal("_id", id3))
    found <- mongoCollection.find()
  } yield found

  f1.subscribe(new Observer[A] {
    override def onNext(result: A): Unit = println(result)

    override def onError(e: Throwable): Unit = println(e.getMessage)

    override def onComplete(): Unit =
      println("=========== finish version of normal ==========")
  })

  println("=========== start version of repository ==========")

  val f2 = for {
    _ <- RepositoryImpl.drop
    _ <- RepositoryImpl.insert(
          A(id1,
            "a_1",
            Seq(
              B("b_1"),
              B("b_2")
            ))
        )
    _ <- RepositoryImpl.insert(
          A(id2, "a_2", Seq.empty[B])
        )
    _ <- RepositoryImpl.insert(
          A(id3, "a_3", Seq.empty[B])
        )
    _ <- RepositoryImpl
          .update(A(id2, "a_4", Seq(B("b_3"))))
    _ <- RepositoryImpl.delete(id3)
    found <- RepositoryImpl.findAll
  } yield found

  f2.run(mongoCollection).unsafeRunSync().foreach(println)

  println("=========== finish version of repository ==========")

  mongoClient.close()
}
