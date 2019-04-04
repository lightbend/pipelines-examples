package pipelines.examples.sensordata

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import SensorDataJsonSupport._
import KeyedSchemas._
import pipelines.akkastream.scaladsl._

object SensorDataStreamingIngress extends HttpIngress[SensorData] {
  implicit val entityStreamingSupport = EntityStreamingSupport.json()
  override def createLogic = defaultStreamingLogic
}
