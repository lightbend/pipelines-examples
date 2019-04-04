package pipelines.examples.carly.ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.common.EntityStreamingSupport

import pipelines.akkastream.scaladsl._
import pipelines.examples.carly.ingestor.JsonCallRecord._
import pipelines.examples.carly.data.Codecs._
import pipelines.examples.carly.data._

class CallRecordStreamingIngress extends HttpIngress[CallRecord] {
  implicit val entityStreamingSupport = EntityStreamingSupport.json()

  override def createLogic = defaultStreamingLogic
}

