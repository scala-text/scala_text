scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.5",
  "org.mindrot"     %  "jbcrypt"     % "0.4",
  "org.scalatest"   %% "scalatest"   % "3.0.8" % "test"
)
