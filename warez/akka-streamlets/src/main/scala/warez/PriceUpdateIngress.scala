package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pipelines.streamlets.avro._
import JsonFormats._
import warez.dsl._

object PriceUpdateIngress extends HttpIngress[PriceUpdate](AvroOutlet[PriceUpdate]("out", _.productId.toString))

