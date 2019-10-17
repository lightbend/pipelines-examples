package pipelines.examples.carly.output;

import akka.NotUsed;
import akka.kafka.ConsumerMessage.CommittableOffset;
import akka.stream.javadsl.*;
import pipelines.streamlets.*;
import pipelines.streamlets.avro.*;
import pipelines.akkastream.*;
import pipelines.akkastream.javadsl.*;

import pipelines.examples.carly.data.*;


public class AggregateRecordEgress extends AkkaStreamlet {
  public AvroInlet<AggregatedCallStats> in = AvroInlet.create("in", AggregatedCallStats.class);

  @Override public StreamletShape shape() {
    return StreamletShape.createWithInlets(in);
  }

  @Override
  public StreamletLogic createLogic() {
    return new RunnableGraphStreamletLogic(getStreamletContext()) {
      @Override
      public RunnableGraph<?> createRunnableGraph() {
        return getSourceWithOffsetContext(in)
          .via(
            FlowWithOffsetContext.<AggregatedCallStats>create()
              .map(metric -> {
                System.out.println(metric);
                return metric;
              })
          )
          .to(getSinkWithOffsetContext());
      }
    };
  }
}
