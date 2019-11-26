package com.example

import org.mongodb.scala.bson.ObjectId

trait Repository[F[_]] {

  def drop: F[Unit]

  def findAll: F[Seq[A]]

  def findOne(_id: ObjectId): F[Option[A]]

  def insert(entity: A): F[Unit]

  def update(entity: A): F[Unit]

  def delete(_id: ObjectId): F[Unit]
}
