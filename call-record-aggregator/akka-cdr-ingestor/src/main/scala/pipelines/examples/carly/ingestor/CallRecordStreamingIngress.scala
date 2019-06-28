package pipelines.examples.carly.ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.common.EntityStreamingSupport

import pipelines.streamlets.avro._
import pipelines.examples.carly.ingestor.JsonCallRecord._
import pipelines.examples.carly.data._
import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.HttpServerLogic

object CallRecordStreamingIngress extends AkkaServerStreamlet {
  implicit val entityStreamingSupport = EntityStreamingSupport.json()

  val out = AvroOutlet[CallRecord]("out", _.user)
  final override val shape = StreamletShape.withOutlets(out)

  override final def createLogic = HttpServerLogic.defaultStreaming(this, out)
}
