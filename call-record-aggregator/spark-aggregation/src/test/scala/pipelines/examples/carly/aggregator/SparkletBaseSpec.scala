package pipelines.examples.carly.aggregator

import org.apache.spark.sql.SparkSession

import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }

abstract class SparkletBaseSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  var session: SparkSession = _

  override def beforeAll(): Unit = {
    session = SparkSession.builder().master("local[2]").appName("test").getOrCreate()
  }

  override def afterAll(): Unit = {
    session.stop()
  }
}
