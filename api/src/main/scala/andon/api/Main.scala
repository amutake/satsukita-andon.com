package andon.api

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.server.{ RouteResult, RoutingLog, RoutingSetup, RoutingSettings }
import akka.stream.scaladsl.Sink
import akka.stream.ActorFlowMaterializer

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorFlowMaterializer()
  implicit val executor = system.dispatcher

  val version = "dev"
  val host = "localhost"
  val port = 6039

  Http().bind(
    interface = host,
    port = port
  ).to(Sink.foreach { conn =>
    conn.flow.join(Routes.route(version)).run()
    ()
  }).run()
}
