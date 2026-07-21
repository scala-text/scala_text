package domain

trait UserService {
  self: PasswordService & UserRepository =>

  val maxNameLength = 32

  def register(name: String, rawPassword: String): User = {
    if (name.length > maxNameLength) {
      throw new Exception("Too long name!")
    }
    if (find(name).isDefined) {
      throw new Exception("Already registered!")
    }
    insert(User(name, hashPassword(rawPassword)))
  }

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

class UserServiceImpl extends UserService with PasswordServiceImpl with UserRepositoryImpl
