package service

import model.User
import net.petitviolet.gae.infra.Id
import scalikejdbc.DBSession

object UserService {
  def create(name: String, email: String): User = {
    User(Id.generate, name, email)
  }

  def store(user: User)(implicit s: DBSession): User = {
    User.createWithAttributes(
      'id -> user.id,
      'name -> user.name,
      'email -> user.email
    )
    user
  }
}
