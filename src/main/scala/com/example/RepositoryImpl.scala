package com.example

import scala.concurrent.ExecutionContext

import cats.data.Kleisli
import cats.effect.{ContextShift, IO}
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._

object RepositoryImpl extends Repository[Kleisli[IO, MongoCollection[A], *]] {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override def drop: Kleisli[IO, MongoCollection[A], Unit] =
    Kleisli(c => IO.fromFuture(IO(c.drop().toFuture())).map(_ => ()))

  override def findAll: Kleisli[IO, MongoCollection[A], Seq[A]] =
    Kleisli(c => IO.fromFuture(IO(c.find().toFuture())))

  override def insert(entity: A): Kleisli[IO, MongoCollection[A], Unit] =
    Kleisli(c => IO.fromFuture(IO(c.insertOne(entity).toFuture())).map(_ => ()))

  override def update(entity: A): Kleisli[IO, MongoCollection[A], Unit] =
    Kleisli(
      c =>
        IO.fromFuture(
            IO(
              c.updateOne(equal("_id", entity._id),
                           combine(set("name", entity.name),
                                   set("children", entity.children)))
                .toFuture()
            )
          )
          .map(_ => ())
    )

  override def delete(
      _id: ObjectId
  ): Kleisli[IO, MongoCollection[A], Unit] =
    Kleisli(
      c =>
        IO.fromFuture(IO(c.deleteOne(equal("_id", _id)).toFuture()))
          .map(_ => ())
    )
}
