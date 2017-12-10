package model

import net.petitviolet.gae.infra.MyDatabaseMapper
import scalikejdbc._

case class Memo(id: String, userId: String, title: String, content: String)

object Memo extends MyDatabaseMapper[Memo] {
  override def extract(rs: WrappedResultSet, n: scalikejdbc.ResultName[Memo]) = autoConstruct(rs, n)
}
