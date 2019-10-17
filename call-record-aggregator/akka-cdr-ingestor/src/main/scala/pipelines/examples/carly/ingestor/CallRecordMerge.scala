package pipelines.examples.carly.ingestor

import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.MergeLogic

import pipelines.examples.carly.data._

class CallRecordMerge extends AkkaStreamlet {
  val in0 = AvroInlet[CallRecord]("in-0")
  val in1 = AvroInlet[CallRecord]("in-1")
  val in2 = AvroInlet[CallRecord]("in-2")
  val out = AvroOutlet[CallRecord]("out", _.user)
  final override val shape = StreamletShape.withInlets(in0, in1, in2).withOutlets(out)
  final override def createLogic = new MergeLogic(Vector(in0, in1, in2), out)
}
