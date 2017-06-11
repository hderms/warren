package sync

import com.spingo.op_rabbit.properties.{Header, TypedHeader}

/**
  * Created by rtuser on 6/10/17.
  */
object Headers {
    val ExchangeName = TypedHeader[String]("exchange_name")

    val RoutingKeys = TypedHeader[Seq[String]]("routing_keys")

  val RequestId = TypedHeader[String]("x-request-id")
}
