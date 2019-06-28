package pipelines.examples.sensordata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.akkastream.AkkaServerStreamlet
import pipelines.akkastream.util.scaladsl._
import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._

import SensorDataJsonSupport._

object SensorDataIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[SensorData]("out", s ⇒ s.deviceId.toString + s.timestamp.toString)
  def shape = StreamletShape.withOutlets(out)
  override def createLogic = HttpServerLogic.default(this, out)
}
