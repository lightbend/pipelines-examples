package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pipelines.streamlets.avro._
import JsonFormats._
import warez.dsl._

class ProductIngress extends HttpIngress[Product](AvroOutlet[Product]("out", _.id.toString))
