package FileWatcher

import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.S3ObjectSummary

import scala.collection.mutable.Map
class s3Handler extends DirectoryWatcher {
  private val client = AmazonS3ClientBuilder.defaultClient()
  private val bucketName = ""

  override def startWatch(): List[String] = {
    val objects = pollBucket()
    checkBucketForModifications(objects)
  }
  def checkBucketForModifications(objectsInBucket: List[S3ObjectSummary]): List[String] = {
    List()
  }

  def pollBucket(): List[S3ObjectSummary] = {
    client.listObjects(bucketName)
      .getObjectSummaries
      .toArray().asInstanceOf[Array[S3ObjectSummary]]
      .toList
  }
}
