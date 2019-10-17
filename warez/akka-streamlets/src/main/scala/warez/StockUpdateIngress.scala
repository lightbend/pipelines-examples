package warez

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pipelines.streamlets.avro._
import JsonFormats._
import warez.dsl._

class StockUpdateIngress extends HttpIngress[StockUpdate](AvroOutlet[StockUpdate]("out", _.productId.toString))

