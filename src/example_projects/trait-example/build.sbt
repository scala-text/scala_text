scalaVersion := "2.13.10"

crossScalaVersions += "3.2.0"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.0.0",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
