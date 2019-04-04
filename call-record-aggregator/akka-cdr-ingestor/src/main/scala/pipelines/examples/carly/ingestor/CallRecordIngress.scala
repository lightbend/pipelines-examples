package pipelines.examples.carly.ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import pipelines.akkastream.scaladsl.HttpIngress
import JsonCallRecord._
import pipelines.examples.carly.data._
import pipelines.examples.carly.data.Codecs._

class CallRecordIngress extends HttpIngress[CallRecord]
