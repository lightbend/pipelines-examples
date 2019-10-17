package pipelines.example.warez

import scala.collection.immutable.Seq
import scala.concurrent.duration._

import pipelines.spark.testkit._
import pipelines.spark.sql.SQLImplicits._
import TestUtils._
import warez._

class SparkProductJoinerKitSpec extends SparkScalaTestSupport {

  val testKit = SparkStreamletTestkit(session)

  "SparkJoin3" should {
    "process streaming data" in {
      // create spark streamlet
      val join3 = new SparkProductJoiner()

      // setup inlet tap on inlet port
      val in0: SparkInletTap[Product] = testKit.inletAsTap[Product](join3.in0)
      val in1: SparkInletTap[StockUpdate] = testKit.inletAsTap[StockUpdate](join3.in1)
      val in2: SparkInletTap[PriceUpdate] = testKit.inletAsTap[PriceUpdate](join3.in2)

      // setup outlet tap on outlet port
      val out: SparkOutletTap[Product] = testKit.outletAsTap[Product](join3.out)

      val socksId = uuid
      val pantsId = uuid
      val socksSkus = genSkus()
      val pantsSkus = genSkus()
      val socks = Product(socksId, "Socks", "Warm in winter", Seq("clothing", "sock", "socks"), socksSkus)
      val pants = Product(pantsId, "Pants", "Denim for the masses", Seq("clothing", "pants"), pantsSkus)

      val stockUpdate = StockUpdate(socksId, socksSkus.head.id, 1)
      val priceUpdate = PriceUpdate(pantsId, pantsSkus.head.id, 100)

      // build data and send to inlet tap
      val data0 = List(socks, pants)
      in0.addData(data0)
      // try multiple updates
      val data1 = (1 to 100).map(_ ⇒ stockUpdate)
      in1.addData(data1)
      val data2 = List(priceUpdate)
      in2.addData(data2)

      testKit.run(join3, Seq(in0, in1, in2), Seq(out), 60.seconds)

      // get data from outlet tap
      val results = out.asCollection(session)

      results.foreach(println)

      // assert
      results must have length 2
      results.exists { p ⇒ p.name == "Socks" && p.skus.head.stock.contains(100) }
    }
  }
}

