package pipelines.example.warez

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.streaming.{ GroupState, GroupStateTimeout, OutputMode }
import pipelines.spark.sql.SQLImplicits._
import pipelines.spark.{ Join3Logic, SparkJoin3 }
import warez.KeyedSchemas._
import warez.{ PriceUpdate, Sku, StockUpdate }
import SparkProductJoiner._

class SparkProductJoiner extends SparkJoin3[warez.Product, warez.StockUpdate, warez.PriceUpdate, warez.Product] {
  override def createLogic(): Join3Logic[warez.Product, warez.StockUpdate, warez.PriceUpdate, warez.Product] =
    new Join3Logic[warez.Product, warez.StockUpdate, warez.PriceUpdate, warez.Product] {
      override def process(products: Dataset[warez.Product], stocks: Dataset[warez.StockUpdate], prices: Dataset[warez.PriceUpdate]): Dataset[warez.Product] = {
        val stocksAsProducts = stocks.map(stockUpdate2Product)
        val pricesAsProducts = prices.map(priceUpdate2Products)
        val withStocks = products
          .union(stocksAsProducts)
          .union(pricesAsProducts)
          .groupByKey(p ⇒ p.id)
          .flatMapGroupsWithState(OutputMode.Append(), GroupStateTimeout.NoTimeout)(stateFunc)
        withStocks
      }
    }
}

object SparkProductJoiner {
  private[warez] def stockUpdate2Product(s: StockUpdate): warez.Product = {
    warez.Product(s.productId, "", "", Seq.empty[String], Seq(Sku(s.skuId, "", stock = Option(s.diff), price = None)))
  }

  private[warez] def priceUpdate2Products(p: PriceUpdate): warez.Product = {
    warez.Product(p.productId, "", "", Seq.empty[String], Seq(Sku(p.skuId, "", stock = None, price = Option(p.price))))
  }

  type ProductId = String

  private[warez] def emptyProduct: warez.Product = new warez.Product

  private[warez] def calcStockDiff(a: Option[Int], b: Option[Int]): Option[Int] = (a, b) match {
    case (Some(i), Some(j)) ⇒ Some(i + j)
    case (Some(i), None)    ⇒ Some(i)
    case (None, Some(j))    ⇒ Some(j)
    case _                  ⇒ None
  }

  private[warez] def mergeSkus(a: Sku, b: Sku): Sku = {
    val name = if (a.name.length > b.name.length) a.name else b.name
    val stock = calcStockDiff(a.stock, b.stock)
    Sku(a.id, name, stock, b.price)
  }

  private[warez] def mergeProducts(acc: warez.Product, skuId: String, newSku: Sku) = {
    val skuIndex = acc.skus.indexWhere(_.id == skuId)
    if (skuIndex < 0) {
      acc.copy(skus = acc.skus :+ newSku)
    } else {
      val sku = acc.skus(skuIndex)
      acc.copy(skus = acc.skus.updated(skuIndex, mergeSkus(sku, newSku)))
    }
  }

  private[warez] def updateProduct(currentProduct: warez.Product, prods: Iterator[warez.Product]): warez.Product = {
    val empty = emptyProduct
    prods.foldLeft(currentProduct) { (acc, p) ⇒
      p match {
        // Is StockUpdate
        case warez.Product(_, "", "", _, Seq(Sku(skuId, "", Some(diff), None))) ⇒ {
          acc match {
            case warez.Product("", "", "", Seq(), Seq()) ⇒ empty
            case _ ⇒
              val newSku = Sku(skuId, "", Some(diff), None)
              mergeProducts(acc, skuId, newSku)
          }
        }
        // Is PriceUpdate
        case warez.Product(_, "", "", _, Seq(Sku(skuId, "", None, Some(price)))) ⇒ {
          acc match {
            case warez.Product("", "", "", Seq(), Seq()) ⇒ empty
            case _ ⇒
              val newSku = Sku(skuId, "", None, Some(price))
              mergeProducts(acc, skuId, newSku)
          }
        }
        // Is Product
        case newProd ⇒ acc.copy(id = newProd.id, name = newProd.name, description = newProd.description, keywords = newProd.keywords, skus = newProd.skus)
      }
    }
  }

  private[warez] def invalid(p: warez.Product): Boolean = p.description.isEmpty && p.name.isEmpty && p.keywords.isEmpty

  val stateFunc: (ProductId, Iterator[warez.Product], GroupState[warez.Product]) ⇒ Iterator[warez.Product] = (_, prods, state) ⇒ {
    val out = updateProduct(state.getOption.getOrElse(emptyProduct), prods)
    (if (invalid(out)) {
      // return nothing
      None
    } else {
      // update state only when output is valid
      state.update(out)
      Some(out)
    }).toIterator
  }
}
