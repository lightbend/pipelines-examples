package pipelines.examples.sensordata

import pipelines.akkastream._
import pipelines.akkastream.scaladsl._
import pipelines.streamlets._
import pipelines.streamlets.avro._

object InvalidMetricLogger extends AkkaStreamlet {
  val inlet = AvroInlet[InvalidMetric]("in")
  val shape = StreamletShape.withInlets(inlet)

  override def createLogic = new RunnableGraphStreamletLogic() {
    val flow = FlowWithPipelinesContext[InvalidMetric]
      .map { invalidMetric ⇒
        system.log.warning(s"Invalid metric detected! $invalidMetric")

        invalidMetric
      }

    def runnableGraph = {
      atLeastOnceSource(inlet).via(flow).to(atLeastOnceSink)
    }
  }
}
