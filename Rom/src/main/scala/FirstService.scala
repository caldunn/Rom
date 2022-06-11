import zio.*

case class Event(desc: String)

trait FirstService {
  def magicNumber: UIO[Int]
  def unit: UIO[Unit]
}
object FirstService {
  def magic: RIO[FirstService, Int] =
    ZIO.serviceWithZIO[FirstService](_.magicNumber)
}

case class FirstServiceLive() extends FirstService:
  override def unit: UIO[Unit]       = ZIO.succeed(())
  override def magicNumber: UIO[Int] = ZIO.succeed(420)

object FirstServiceLive {
  val layer: ULayer[FirstService] = ZLayer.succeed(FirstServiceLive())

}

trait SecondService {
  def magicNumber: UIO[Int]
  def unit: UIO[Unit]
}
object SecondService {
  def magic: RIO[FirstService, Int] =
    ZIO.serviceWithZIO[FirstService](_.magicNumber)
}

case class SecondServiceLive() extends SecondService:
  override def unit: UIO[Unit]       = ZIO.succeed(())
  override def magicNumber: UIO[Int] = ZIO.succeed(69)

object SecondServiceLive {
  val layer: ULayer[SecondService] = ZLayer.succeed(SecondServiceLive())

}
