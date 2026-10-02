package domain

case class User(id: Long, name: String, hashedPassword: String)

object User {
  def apply(name: String, hashedPassword: String): User =
    User(0L, name, hashedPassword)
}
