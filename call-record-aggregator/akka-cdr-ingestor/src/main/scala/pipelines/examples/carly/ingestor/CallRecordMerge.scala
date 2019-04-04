package pipelines.examples.carly.ingestor

import pipelines.akkastream.scaladsl.Merge
import pipelines.examples.carly.data._
import pipelines.examples.carly.data.Codecs._

object CallRecordMerge extends Merge[CallRecord](3)

