package domain

import scalikejdbc._

trait UserRepository {
  def insert(user: User): User

  def find(name: String): Option[User]

  def find(id: Long): Option[User]
}

trait UserRepositoryImpl extends UserRepository {
  def insert(user: User): User = DB localTx { implicit s =>
    val id = sql"""insert into users (name, password) values (${user.name}, ${user.hashedPassword})"""
      .updateAndReturnGeneratedKey.apply()
    user.copy(id = id)
  }

  def createUser(rs: WrappedResultSet): User =
    User(rs.long("id"), rs.string("name"), rs.string("password"))

  def find(name: String): Option[User] = DB readOnly { implicit s =>
    sql"""select * from users where name = $name """
      .map(createUser).single.apply()
  }

  def find(id: Long): Option[User] = DB readOnly { implicit s =>
    sql"""select * from users where id = $id """
      .map(createUser).single.apply()
  }
}

