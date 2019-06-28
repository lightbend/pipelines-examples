package pipelines.examples.sensordata

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import SensorDataJsonSupport._
import pipelines.akkastream.AkkaServerStreamlet
import pipelines.akkastream.util.scaladsl._
import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._

object SensorDataStreamingIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[SensorData]("out", s ⇒ s.deviceId.toString + s.timestamp.toString)
  def shape = StreamletShape.withOutlets(out)

  implicit val entityStreamingSupport = EntityStreamingSupport.json()
  override def createLogic = HttpServerLogic.defaultStreaming(this, out)
}
