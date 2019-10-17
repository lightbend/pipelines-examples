package pipelines.examples.sensordata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl._

import pipelines.streamlets._
import pipelines.streamlets.avro._
import SensorDataJsonSupport._

class SensorDataHttpIngress extends AkkaServerStreamlet {
  val out = AvroOutlet[SensorData]("out").withPartitioner(RoundRobinPartitioner)
  def shape = StreamletShape.withOutlets(out)
  override def createLogic = HttpServerLogic.default(this, out)
}

