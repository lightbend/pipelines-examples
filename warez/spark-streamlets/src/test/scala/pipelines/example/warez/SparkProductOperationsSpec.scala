package pipelines.example.warez

import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.OptionValues._

import scala.collection.immutable.Seq
import warez.{ PriceUpdate, Product, Sku, StockUpdate }

class SparkProductOperationsSpec extends WordSpec with Matchers {

  "A Product" should {
    "be updated correctly" in {
      val skus = Array(
        Sku("1", "Small Hole", Some(10), Some(5)),
        Sku("2", "Medium Hole", Some(10), Some(10)),
        Sku("3", "Large Hole", Some(15), Some(20))
      )
      val description = "A cartoon hole that can be applied to any surface."
      val keywords = Array("black", "hole", "gag", "plot device", "roger rabbit")

      val p = new Product(
        "123456789",
        "Acme Portable Hole",
        description,
        keywords,
        skus
      )

      val priceUpdate = PriceUpdate(
        "123456789",
        "1",
        10
      )
      val stockUpdate = StockUpdate(
        "123456789",
        "1",
        10
      )
      val zero = SparkProductJoiner.emptyProduct
      val p1 = SparkProductJoiner.updateProduct(zero, Seq(p).toIterator)
      p1 == p should equal(true)
      val prodPrice = SparkProductJoiner.priceUpdate2Products(priceUpdate)
      val p2 = SparkProductJoiner.updateProduct(p1, Seq(prodPrice).toIterator)
      p2.skus.find(_.id == "1").value.price should equal(Some(10))
      val prodStock = SparkProductJoiner.stockUpdate2Product(stockUpdate)
      val p3 = SparkProductJoiner.updateProduct(p2, Seq(prodStock).toIterator)
      p3.skus.find(_.id == "1").value.stock should equal(Some(20))
      // the same price update should cause no change here
      val p4 = SparkProductJoiner.updateProduct(p3, Seq(prodPrice).toIterator)
      p4.skus.find(_.id == "1").value.price should equal(Some(10))
      p4.skus.find(_.id == "1").value.stock should equal(Some(20))
      p4.description should be(description)
      p4.keywords should be(keywords)
    }
  }
}
