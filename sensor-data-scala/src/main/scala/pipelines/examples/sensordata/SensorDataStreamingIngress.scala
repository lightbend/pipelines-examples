package pipelines.examples.sensordata

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import SensorDataJsonSupport._
import pipelines.akkastream.AkkaServerStreamlet
import pipelines.akkastream.util.scaladsl._
import pipelines.streamlets.{ RoundRobinPartitioner, StreamletShape }
import pipelines.streamlets.avro._

class SensorDataStreamingIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[SensorData]("out", RoundRobinPartitioner)
  def shape = StreamletShape.withOutlets(out)

  implicit val entityStreamingSupport = EntityStreamingSupport.json()
  override def createLogic = HttpServerLogic.defaultStreaming(this, out)
}
