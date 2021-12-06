### INPUT
The input of the pipeline is Professor Grechanik's LogFileGenerator project. Logs generated will be
read by the Akka system and fed into the rest of the pipeline.

### Akka
The first node in the pipeline is a two actor Akka system. The first actor -- LogWatcher-- is responsible for watching
a specific directory using my custom NioWatcher class. When files are modified or created, all file events
will be returned to this actor. LogWatcher than sends a message to a LogHandler actor. This message is a list
of files. LogHandler than reads the entire contents of these files, into a list of logs. Log Handler than creates a
KafkaLogProducer object which is responsible for writing the logs to a Kafka topic.


### Spark
The second node is Spark, which is subscribed to the kafka topic from above. Spark reads the incoming logs
into a dataframe, and then proceeds to transform the data. Spark parses the logs, then filters the data for error logs,
then maps these error logs to the time interval they occurred in. Then, all error logs for a given time interval are 
grouped aggregated into a list, and finally mapped to a formatted string. These formatted strings containing a bucket 
and error logs that occured in that bucket are than written back to the kafka cluster to a new topic.


### Kafka consumer
The last node is a Kafka consumer, subscribed the topic mentioned above in the spark section. The consumer
reads incoming logs, creates a formatted email for each record read, and using AWS Simple Email Service to send an email.
Due to AWS costs, the email is sent from my UIC email to my UIC email.

### Output
When deployed to AWS emails are sent to concerned parties (i.e me because it cost more to use un verified emails). 
WHen run locally (this configuration is set to true for ease of grading), ConsumerApp prints the email text (that would 
be sent by AWS) to the console.