scalaVersion := "2.13.2"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.4.1",
  "org.mindrot"     %  "jbcrypt"     % "0.4",
  "org.scalatest"   %% "scalatest"   % "3.1.2" % "test"
)
