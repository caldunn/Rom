import com.kosprov.jargon2.api.Jargon2.*
import zio.*

case class Argon2GeneratorError(msg: String)

object HashMe {
  private val passwordAsBytes: Array[Byte] = "mahPassword".getBytes()
  private val hasher = jargon2Hasher()
    .`type`(Type.ARGON2id)
    .memoryCost(65536)
    .timeCost(3)
    .parallelism(4)
    .saltLength(16)
    .hashLength(16)

  def getPassword: String = hasher.password(passwordAsBytes).encodedHash()

  def checkHash(encodedPassword: String): Boolean =
    val kek = jargon2Verifier()
      .hash(encodedPassword)
      .password(passwordAsBytes)
      .verifyEncoded()
    kek

  def checkForSecret: zio.IO[Argon2GeneratorError, Unit] =
    ZIO
      .fromOption(sys.env.get("ARGON_PASSWORD_SECRET"))
      .orElseFail(Argon2GeneratorError("No System variable set"))
      .unit

}
