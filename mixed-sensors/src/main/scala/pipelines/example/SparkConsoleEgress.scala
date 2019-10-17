package pipelines.example

import pipelines.streamlets.StreamletShape
import pipelines.streamlets.avro._
import pipelines.spark.{ SparkStreamlet, SparkStreamletLogic, StreamletQueryExecution }
import pipelines.spark.sql.SQLImplicits._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.DataFrame

class SparkConsoleEgress extends SparkStreamlet {
  val in1 = AvroInlet[Data]("in1")
  val in2 = AvroInlet[Data]("in2")
  val shape = StreamletShape.withInlets(in1, in2)

  def asTimestamp = udf((t: Long) ⇒ new java.sql.Timestamp(t))
  def elapsedTime = udf((t1: Long, t0: Long) ⇒ t1 - t0)

  override def createLogic() = new SparkStreamletLogic {
    override def buildStreamingQueries = {
      val stream1 = readStream(in1).withColumn("source", lit("spark")).withColumn("elapsed", elapsedTime($"t2", $"t1"))
      val stream2 = readStream(in2).withColumn("source", lit("akka")).withColumn("elapsed", elapsedTime($"t2", $"t1"))

      // commented-out process: simple stats to compute min/max/mean on a window
      // val dataCount = stream1.union(stream2).withColumn("ts", asTimestamp($"timestamp"))
      //      val stats = dataCount
      //        .withWatermark("ts", "1 second")
      //        .groupBy(window($"ts", "5 minutes", "1 minute"), $"source")
      //        //.agg(max($"elapsed"), min($"elapsed"), avg($"elapsed"), count($"source"))

      val quantiles: (String ⇒ Long ⇒ (DataFrame, Long) ⇒ Unit) = { name ⇒ period ⇒ (df, time) ⇒
        df.cache()
        val count = df.count()
        val cps = count.toDouble / period
        val quans = df.stat.approxQuantile("elapsed", Array(0.1, 0.5, 0.9, 0.99), 0.01)
        println(s"$time, $name, $count, $cps, " + quans.mkString(", "))
      }

      val period = 60 * 5 // seconds

      val q1 = stream1.writeStream.foreachBatch(quantiles("spark")(period))
        .trigger(Trigger.ProcessingTime(s"$period seconds"))
        .option("checkpointLocation", context.checkpointDir("console-egress-q1"))
        .start()
      val q2 = stream2.writeStream.foreachBatch(quantiles("akka")(period))
        .trigger(Trigger.ProcessingTime(s"$period seconds"))
        .option("checkpointLocation", context.checkpointDir("console-egress-q2"))
        .start()

      new Thread() {
        override def run(): Unit = {
          while (true) {
            val progress = q1.lastProgress
            if (progress != null) {
              println("***************** [PROGRESS] *********************")
              println(progress.toString())
              println("**************************************************")
            }
            Thread.sleep(60 * 1000)
          }
        }
      } //.start  // uncomment to enable the query progress

      StreamletQueryExecution(q1, q2)
    }
  }
}
