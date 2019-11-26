package com.example

import org.mongodb.scala.bson.ObjectId

final case class A(_id: ObjectId, name: String, children: Seq[B])

final case class B(name: String)
