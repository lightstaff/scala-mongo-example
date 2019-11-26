package com.example

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import cats.data.Kleisli
import cats.implicits._
import org.bson.codecs.configuration.CodecRegistries
import org.mongodb.scala._
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

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

  val repository: Repository[Kleisli[Future, MongoCollection[A], *]] =
    new RepositoryImpl()

  val id1 = new ObjectId()

  val id2 = new ObjectId()

  val id3 = new ObjectId()

  val _result = for {
    _ <- repository.drop
    _ <- repository.insert(
          A(id1,
            "a_1",
            Seq(
              B("b_1"),
              B("b_2")
            ))
        )
    _ <- repository.insert(
          A(id2, "a_2", Seq.empty[B])
        )
    _ <- repository.insert(
          A(id3, "a_3", Seq.empty[B])
        )
    _ <- repository
          .update(A(id2, "a_2", Seq(B("b_3"))))
    _ <- repository.delete(id3)
    found <- repository.findAll
  } yield found

  val result =
    Await.result(_result.run(mongoCollection), Duration(10, TimeUnit.SECONDS))

  result.foreach(println)

  mongoClient.close()
}
