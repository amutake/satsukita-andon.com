package andon.api

import akka.actor.ActorSystem
import akka.http.Http
import akka.http.server.{ RouteResult, RoutingLog, RoutingSetup, RoutingSettings }
import akka.stream.scaladsl.Sink
import akka.stream.ActorFlowMaterializer

import scalikejdbc._

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorFlowMaterializer()
  implicit val executor = system.dispatcher

  val version = "dev"
  val host = "localhost"
  val port = 6039

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:./test", "", "")

  try {
    models.Articles.make()
  } catch {
    case e: Throwable => println(e.getMessage)
  }

  Http().bind(
    interface = host,
    port = port
  ).to(Sink.foreach { conn =>
    conn.flow.join(Routes.route(version)).run()
    ()
  }).run()
}
