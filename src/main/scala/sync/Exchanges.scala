package sync

import com.spingo.op_rabbit.Exchange

object Exchanges {
   val firehose = Exchange.passive("amq.rabbitmq.trace")

}
