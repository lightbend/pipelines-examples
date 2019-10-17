package pipelines.examples.sensordata

import pipelines.streamlets._
import pipelines.streamlets.avro._
import pipelines.akkastream._
import pipelines.akkastream.util.scaladsl.MergeLogic

class SensorDataMerge extends AkkaStreamlet {
  val in0 = AvroInlet[SensorData]("in-0")
  val in1 = AvroInlet[SensorData]("in-1")
  val out = AvroOutlet[SensorData]("out", _.deviceId.toString)

  final override val shape = StreamletShape.withInlets(in0, in1).withOutlets(out)
  final override def createLogic = new MergeLogic(Vector(in0, in1), out)
}
