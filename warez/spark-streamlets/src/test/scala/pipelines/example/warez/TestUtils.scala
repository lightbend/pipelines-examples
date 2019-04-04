package pipelines.example.warez
import java.util.UUID

import warez.Sku

import scala.collection.immutable.Seq

object TestUtils {
  def uuid: String = UUID.randomUUID().toString
  def genSkus(names: Seq[String] = Seq("small", "med", "large")): Seq[Sku] = names.map(Sku(uuid, _))
}
