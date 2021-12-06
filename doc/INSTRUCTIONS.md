Sbt Version : 1.1 Scala Version : "2.12.10" Java Version : 1.8.0

Docker and Spark are required to run the pipeline.

Clone github repository on local machine using IntelliJ import from version control feature. Also clone: 

    https://github.com/0x1DOCD00D/LogFileGenerator/


Create and deploy a docker container using the following image:

    src/main/kafka/docker-compose.yml

Configure the configuration file appropriately. Specifically change akka.kafka.dirToWatch to the log directory
of the LogFileGenerator project on your machine.

Navigate to the CourseProject directory and execute the follow commands in their own terminal sessions:

Assemble jar file in:

    sbt assembly

Run Akka System process:

    java -classpath "target/scala-2.12/CourseProject-assembly-0.1.jar:slf4j-jdk14-2.0.0-alpha5.jar" Main

Run Spark process:

    spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0 --class "Spark.SparkPlayGround" target/scala-2.12/CourseProject-assembly-0.1.jar

Run Kafka Consumer process (This session will contain the file output):

    java -classpath "target/scala-2.12/CourseProject-assembly-0.1.jar:slf4j-jdk14-2.0.0-alpha5.jar" Kafka.ConsumerApp


Navigate to the LogFileGenerator directory (or modify a file in the watched directory):

    java -classpath "target/scala-2.12/CourseProject-assembly-0.1.jar:slf4j-jdk14-2.0.0-alpha5.jar" Kafka.ConsumerApp

The formatted emails that would be sent -- had this been deployed on AWS -- will be printed to the console
in the terminal session containing the Kafka Consumer process.


    


