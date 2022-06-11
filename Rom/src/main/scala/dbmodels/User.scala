package dbmodels

import java.util.UUID
import java.time.Instant

case class User(id: UUID, uName: String, password: String, email: String, mobile_no: String)
case class Group(id: UUID, gName: String, description: String)
case class Subscription(gId: UUID, uId: UUID, ts: Instant)
case class Announcement(id: UUID, gId: UUID, created: Instant, msg: String)
