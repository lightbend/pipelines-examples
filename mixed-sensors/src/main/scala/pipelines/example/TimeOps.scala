package pipelines.example

object TimeOps {

  def nowAsOption: Option[Long] = Some(System.currentTimeMillis())

}
