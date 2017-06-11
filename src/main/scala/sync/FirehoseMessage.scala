package sync

import java.util.{HashMap => JavaHashMap}

import com.sksamuel.elastic4s.Indexable
import io.circe.{Decoder, Encoder}
import play.api.libs.json.JsObject
import io.circe._
import io.circe.generic.semiauto._



case class FirehoseMessage(body: JsObject,
  exchangeName: String,
  routingKey: Seq[String],
  appId: Option[String],
  messageId: Option[String],
  userId: Option[String],
  correlationId: Option[String],
  headers: Option[Map[String, String]])

object FirehoseMessage{
  import scala.collection.JavaConverters._
  def fromJavaHashMap(body: JsObject, exchangeName: String, routingKey: Seq[String], appId: Option[String], messageId: Option[String], userId: Option[String],
    correlationId: Option[String],
    headers: Option[JavaHashMap[String, String]]) = {

    FirehoseMessage(
    body= body,
    exchangeName= exchangeName,
    routingKey= routingKey,
    appId= appId,
    messageId= messageId,
    userId= userId,
    correlationId= correlationId,
    headers= headers.map{(j: JavaHashMap[String, String]) => j.asScala.toMap})
  }
}





