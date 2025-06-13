scalaVersion := "2.13.16"

crossScalaVersions += "3.7.1"

libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc" % "4.3.4",
  "org.mindrot"     %  "jbcrypt"     % "0.4"
)
