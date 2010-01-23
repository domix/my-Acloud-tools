import org.jets3t.service.security.AWSCredentials
import org.jets3t.service.S3Service
import org.jets3t.service.model.S3Bucket
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object





@Grab(group='net.java.dev.jets3t', module='jets3t', version='0.7.2')
class S3Storer {
	def awsAccessKey
	def awsSecretKey
	private def awsCredentials = null
	private def s3Service = null
	
	def storeFile(file, bucketName) {
		
		def object = new S3Object(new File(file))
		def bucket = getS3Service().getBucket(bucketName)
		
		if(bucket) {
			println "Storing $file on $bucketName wait a moment please..."
			def storedFile = s3Service.putObject(bucket, object);
			println "Successfully upload $file"
			storedFile
		} else {
			println "the bucket $bucketName cant be found."
			null
		}
		
	}
	
	private def getCredentials() {
		if(!awsCredentials)  {
			awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
		}
		awsCredentials
	}
	
	private def getS3Service() {
		if(!s3Service)  {
			s3Service = new RestS3Service(getCredentials())
		}
		s3Service
	}
}

