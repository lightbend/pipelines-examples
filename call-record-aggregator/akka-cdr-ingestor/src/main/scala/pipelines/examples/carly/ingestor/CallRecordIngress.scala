package pipelines.examples.carly.ingestor

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import JsonCallRecord._
import pipelines.streamlets.avro._
import pipelines.examples.carly.data._
import pipelines.streamlets._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.HttpServerLogic

class CallRecordIngress extends AkkaServerStreamlet {

  //tag::docs-outlet-partitioner-example[]
  val out = AvroOutlet[CallRecord]("out").withPartitioner(RoundRobinPartitioner)
  //end::docs-outlet-partitioner-example[]

  final override val shape = StreamletShape.withOutlets(out)
  final override def createLogic = HttpServerLogic.default(this, out)
}
