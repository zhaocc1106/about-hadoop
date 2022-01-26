name := "spark-examples"

version := "0.1"

scalaVersion := "2.12.14"

idePackagePrefix := Some("org.spark_examples")

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.1.2"