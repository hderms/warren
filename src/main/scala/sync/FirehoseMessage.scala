package sync

import java.util.{HashMap => JavaHashMap}

import play.api.libs.json.JsObject


case class FirehoseMessage(body: JsObject, exchangeName: String, routingKey: Seq[String], appId: Option[String], messageId: Option[String], userId: Option[String], correlationId: Option[String], headers: Option[JavaHashMap[String, String]])
