scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0",
  "org.mindrot"     %  "jbcrypt"     % "0.4",
  "org.scalatest"   %% "scalatest"   % "3.2.8" % "test"
)
