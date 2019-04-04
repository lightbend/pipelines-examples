package pipelines.example

import org.scalatest.{ Matchers, WordSpec }

class DataGroupTest extends WordSpec with Matchers {

  "DataGroup" should {
    val groupSize = 20
    // simulate the behavior of the data producer
    val data = (0 to groupSize * 10)
      .map(i ⇒ (i.toLong / groupSize, i.toLong))
      .groupBy { case (k, _) ⇒ k }
      .map { case (k, seqKV) ⇒ (k, seqKV.map { case (_, v) ⇒ v }) }

    "report completed when it has received all data" in {
      val dataGroup = DataGroup(3, groupSize, data(3))
      assert(dataGroup.isComplete, "dataGroup should be complete with the data sample")
    }

    "report missing elements when it doesn't have all data for its group" in {
      val dataSubset = data(5).drop(3)
      val dataGroup = DataGroup(5, groupSize, dataSubset)
      assert(!dataGroup.isComplete, "dataGroup should be incomplete")
      dataGroup.missing should be(data(5).take(3).toSet)
      dataGroup.missingReport should be("(100,102)")
    }

  }
}
