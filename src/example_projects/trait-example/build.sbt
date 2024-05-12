scalaVersion := "2.13.14"

crossScalaVersions += "3.4.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.3.0",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
