//Here you can provide the database, password, even the username to perform the backup in gzip
def sqlDatabasefile = new MySqlBackuper(database:'cp1026', password:'').doBackup()

println 'Performing MySql Backup to Amazon S3'

def storer = new S3Storer(awsAccessKey:'', awsSecretKey:'')
// The filename and the bucket name
def f = storer.storeFile(sqlDatabasefile, '')


class MySqlBackuper {
	def host = 'localhost'
	def user = 'root'
	def password = ''
	def database = ''

	def doBackup() {
		def filename = "$database-${System.currentTimeMillis()}"
		def sqlfilename = "${filename}.sql"
		def zipfilename = "${filename}.gz"

		println "Trying Backup, database='$database' with '$user' in file $zipfilename"
		//This is the command to perform the dump via mySql tools
		def command = "mysqldump --user=${user} --password='${password}' ${database}"

		def dump = command.execute()
		dump.waitFor()

		new File(sqlfilename).write(dump.text)

		//AntBuilder is awesome, cool to have it in runtime, not only in develoment time
		def ant = new AntBuilder()
		//Create the Gzip file
		ant.gzip zipfile:zipfilename, src:sqlfilename
		ant.delete file:sqlfilename
		
		//return the GZip filename
		zipfilename
	}
}



/**
 * This class is responsible to store files in Amazon S3.
 */
@Grab(group='net.java.dev.jets3t', module='jets3t', version='0.7.2')
class S3Storer {
	def awsAccessKey
	def awsSecretKey
	private def awsCredentials = null
	private def s3Service = null
	
	/**
	 * 
	 */
	def storeFile(file, bucketName) {
		
		def object = new org.jets3t.service.model.S3Object(new File(file))
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
			awsCredentials = new org.jets3t.service.security.AWSCredentials(awsAccessKey, awsSecretKey);
		}
		awsCredentials
	}
	
	private def getS3Service() {
		if(!s3Service)  {
			s3Service = new org.jets3t.service.impl.rest.httpclient.RestS3Service(getCredentials())
		}
		s3Service
	}
}

