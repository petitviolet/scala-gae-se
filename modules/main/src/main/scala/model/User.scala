package model

import net.petitviolet.gae.infra.MyDatabaseMapper
import scalikejdbc._

case class User(id: String, name: String, email: String)

object User extends MyDatabaseMapper[User] {
  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[User]) = autoConstruct(rs, n)
}
