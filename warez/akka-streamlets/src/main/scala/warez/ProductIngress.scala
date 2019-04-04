package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.akkastream.scaladsl._
import JsonFormats._
import KeyedSchemas._

object ProductIngress extends HttpIngress[Product]