package com.example

import scala.concurrent.{ExecutionContext, Future}

import cats.data.Kleisli
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.ObjectId
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._

class RepositoryImpl(implicit ec: ExecutionContext)
    extends Repository[Kleisli[Future, MongoCollection[A], *]] {

  override def drop: Kleisli[Future, MongoCollection[A], Unit] =
    Kleisli(c => c.drop().toFuture().map(_ => ()))

  override def findAll: Kleisli[Future, MongoCollection[A], Seq[A]] =
    Kleisli(c => c.find().toFuture())

  override def findOne(
      _id: ObjectId
  ): Kleisli[Future, MongoCollection[A], Option[A]] =
    Kleisli(c => c.find(equal("_id", _id)).first().toFutureOption())

  override def insert(entity: A): Kleisli[Future, MongoCollection[A], Unit] =
    Kleisli(c => c.insertOne(entity).toFuture().map(_ => ()))

  override def update(entity: A): Kleisli[Future, MongoCollection[A], Unit] =
    Kleisli(
      c =>
        c.updateOne(equal("_id", entity._id),
                     combine(set("name", entity.name),
                             set("children", entity.children)))
          .toFuture()
          .map(_ => ())
    )

  override def delete(
      _id: ObjectId
  ): Kleisli[Future, MongoCollection[A], Unit] =
    Kleisli(c => c.deleteOne(equal("_id", _id)).toFuture().map(_ => ()))
}
