package pipelines.examples.carly.output;

import akka.NotUsed;
import akka.kafka.ConsumerMessage.CommittableOffset;
import akka.stream.javadsl.*;

import pipelines.streamlets.*;
import pipelines.streamlets.avro.*;
import pipelines.akkastream.*;
import pipelines.akkastream.javadsl.*;
import pipelines.examples.carly.data.*;

public class InvalidRecordEgress extends AkkaStreamlet { 
  public AvroInlet<InvalidRecord> in = AvroInlet.create("in", InvalidRecord.class);

  private Object doPrint(final InvalidRecord record) {
    System.out.println(record);
    return record;
  }

  @Override public StreamletShape shape() {
    return StreamletShape.createWithInlets(in);
  }

  @Override
  public StreamletLogic createLogic() {
    return new RunnableGraphStreamletLogic(getStreamletContext()) {
      @Override
      public RunnableGraph<?> createRunnableGraph() {
        return getSourceWithOffsetContext(in)
          .via(flowWithContext())
          .to(getSinkWithOffsetContext());
      }
    };
  }

  private FlowWithContext<InvalidRecord,CommittableOffset,Object,CommittableOffset,NotUsed> flowWithContext() {
    return FlowWithOffsetContext.<InvalidRecord>create().map(metric -> doPrint(metric));
  }
}
