package restmodels

import java.util.UUID

case class UserLogin(uName: String, password: String)
case class SubRequest(uId: UUID, gId: UUID)
// case class CreateGroup()
