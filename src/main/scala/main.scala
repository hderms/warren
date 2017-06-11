
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.spingo.op_rabbit.RabbitControl
import misc.LoggingDecider
import rabbitadmin.RabbitAdmin
import sync.Sync
import sync.db.Db.FireHoseDb
import sync.db.reactive.Reactive

import scala.util.Properties



object Main extends App {
  implicit val actorSystem = ActorSystem("firehoseSync")
  val actorMaterializerSettings = ActorMaterializerSettings(actorSystem).withSupervisionStrategy(LoggingDecider.decider)
  implicit val materializer = ActorMaterializer(actorMaterializerSettings)(actorSystem)
  implicit val ec = materializer.executionContext
  val rabbitControl = actorSystem.actorOf(Props[RabbitControl])
  val sync = new Sync(rabbitControl)(Reactive.subscriber(FireHoseDb.client))

  val shouldShutoffHose = Properties.envOrElse("TURNOFF_FIREHOSE_ON_SHUTDOWN", "true").toBoolean

  sys.addShutdownHook {
    if (shouldShutoffHose) {
      RabbitAdmin.firehose.stop
    }
    println("Stopping...")
  }

  RabbitAdmin.firehose.start

  http.Service.builder.start.runAsync(x => println(x))

  sync.run
}
