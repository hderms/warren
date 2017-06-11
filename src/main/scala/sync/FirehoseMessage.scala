package sync

import java.util.{HashMap => JavaHashMap}

import play.api.libs.json.JsObject



case class FirehoseMessage(body: JsObject,
  exchangeName: String,
  routingKey: List[String],
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
    println("in hash map")

    FirehoseMessage(
    body= body,
    exchangeName= exchangeName,
      routingKey = routingKey.toList,
    appId= appId,
    messageId= messageId,
    userId= userId,
    correlationId= correlationId,
    headers= headers.map{(j: JavaHashMap[String, String]) => j.asScala.toMap})
  }
}





