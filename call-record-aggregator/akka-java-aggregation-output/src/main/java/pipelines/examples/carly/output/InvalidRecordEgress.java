package pipelines.examples.carly.output;

import akka.NotUsed;
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
        return getAtLeastOnceSource(in)
          .via(flowWithContext().asFlow())
          .to(getAtLeastOnceSink());
      }
    };
  }

  private FlowWithContext<InvalidRecord,PipelinesContext,Object,PipelinesContext,NotUsed> flowWithContext() {
    return FlowWithPipelinesContext.<InvalidRecord>create().map(metric -> doPrint(metric));
  }
}
