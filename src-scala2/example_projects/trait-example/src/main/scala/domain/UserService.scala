package domain

import org.mindrot.jbcrypt.BCrypt
import scalikejdbc._

class UserService {
  val maxNameLength = 32

  // ストレージ機能
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

  // パスワード機能
  def hashPassword(rawPassword: String): String =
    BCrypt.hashpw(rawPassword, BCrypt.gensalt())

  def checkPassword(rawPassword: String, hashedPassword: String): Boolean =
    BCrypt.checkpw(rawPassword, hashedPassword)

  // ユーザー登録
  def register(name: String, rawPassword: String): User = {
    if (name.length > maxNameLength) {
      throw new Exception("Too long name!")
    }
    if (find(name).isDefined) {
      throw new Exception("Already registered!")
    }
    insert(User(name, hashPassword(rawPassword)))
  }

  // ユーザー認証
  def login(name: String, rawPassword: String): User = {
    find(name) match {
      case None       => throw new Exception("User not found!")
      case Some(user) =>
        if (!checkPassword(rawPassword, user.hashedPassword)) {
          throw new Exception("Invalid password!")
        }
        user
    }
  }
}
