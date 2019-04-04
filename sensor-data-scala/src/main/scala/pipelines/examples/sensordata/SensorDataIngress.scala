package pipelines.examples.sensordata

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.akkastream.scaladsl._
import SensorDataJsonSupport._
import KeyedSchemas._

object SensorDataIngress extends HttpIngress[SensorData]
