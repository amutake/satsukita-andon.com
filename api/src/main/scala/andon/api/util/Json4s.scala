package andon.api.util

import scala.concurrent.ExecutionContext
import scala.reflect.Manifest
import akka.stream.ActorFlowMaterializer
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.model.{ ContentTypes, HttpCharsets, MediaTypes }
import org.json4s.{ Serialization, Formats, NoTypeHints }
import org.json4s.jackson.{ Serialization => JacksonSerialization }
import org.json4s.ext.JodaTimeSerializers
// import org.json4s.native.{ Serialization => NativeSerialization }

trait Json4sSupport {

  val serialization: Serialization
  val formats: Formats

  implicit def json4sMarshaller[T <: AnyRef](implicit ec: ExecutionContext) =
    Marshaller.StringMarshaller.wrap(ContentTypes.`application/json`) { t: T =>
      serialization.write(t)(formats)
    }

  implicit def json4sUnmarshaller[T](implicit fm: ActorFlowMaterializer, ec: ExecutionContext, mf: Manifest[T]) =
    Unmarshaller.stringUnmarshaller.forContentTypes(MediaTypes.`application/json`).map { str =>
      serialization.read(str)(formats, mf)
    }
}

object Json4sJacksonSupport extends Json4sSupport {
  val serialization = JacksonSerialization
  val formats = JacksonSerialization.formats(NoTypeHints) ++ JodaTimeSerializers.all
}
