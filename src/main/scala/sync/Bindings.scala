package sync

import com.spingo.op_rabbit.Queue
import com.spingo.op_rabbit.Directives._

object Bindings {

  val firehose = Queue.passive(topic(queue(
    "firehose",
    durable = false,
    exclusive = false,
    autoDelete = true),
    List("#"),
    exchange = Exchanges.firehose))

}
