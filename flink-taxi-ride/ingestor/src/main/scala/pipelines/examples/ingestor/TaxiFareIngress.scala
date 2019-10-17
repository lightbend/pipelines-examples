package pipelines.examples
package ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.streamlets.avro._
import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.flink.avro._
import TaxiFareJsonProtocol._
import pipelines.akkastream.util.scaladsl.HttpServerLogic

class TaxiFareIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[TaxiFare]("out", _.rideId.toString)

  final override val shape = StreamletShape.withOutlets(out)
  final override def createLogic = HttpServerLogic.default(this, out)
}
