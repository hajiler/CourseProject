sbt clean
sbt assembly
clear
#spark-submit --class "Spark.SparkPlayGround" /Users/hajiler/school/cs441/CourseProject/target/scala-2.13/CourseProject-assembly-0.1.jar
spark-submit --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.2.0 --class "Spark.SparkPlayGround" /Users/hajiler/school/cs441/CourseProject/target/scala-2.13/CourseProject-assembly-0.1.jar