package pipelines.examples.carly.ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import JsonCallRecord._
import pipelines.streamlets.avro._
import pipelines.examples.carly.data._
import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.HttpServerLogic

object CallRecordIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[CallRecord]("out", _.user)
  final override val shape = StreamletShape.withOutlets(out)

  override final def createLogic = HttpServerLogic.default(this, out)
}
