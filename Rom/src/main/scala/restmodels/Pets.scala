package restmodels

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

object Pets {
  case class Pet(species: String, url: String)
  implicit val petCodec: Codec[Pet] = deriveCodec
}
