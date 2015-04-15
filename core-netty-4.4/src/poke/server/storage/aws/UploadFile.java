package poke.server.storage.aws;
import java.io.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadFile {
	private static Logger logger = LoggerFactory.getLogger(UploadFile.class);

	final static AmazonS3Client s3 = new AmazonS3Client(new BasicAWSCredentials("AKIAJQ2I4FRN4YWSXCXQ", "3UVmA3agUfdHt+zN0QP7IEKEli1KBW/uY4XDTOJy"));
	

	 public static synchronized String  uploadImage(ByteString imageBytes,String filename) {
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);
		String bucketName = "raftlog";
		String bucketUrl = "https://s3-us-west-1.amazonaws.com/raftlog";
		try {
			s3.setRegion(Region.getRegion(Regions.US_WEST_1));
			InputStream stream = new ByteArrayInputStream(imageBytes.toByteArray());
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength(imageBytes.size());
			if(filename.toLowerCase().contains("png")){
				meta.setContentType("image/png");
			} else{
				if(filename.toLowerCase().contains("jpg"))
				meta.setContentType("image/jpg");
			}
			if(filename.toLowerCase().contains("gif")){
				meta.setContentType("image/gif");
			}

			if(filename.toLowerCase().contains("jpeg")){
				meta.setContentType("image/jpeg");
			}

			final PutObjectResult putObjectResult = s3.putObject(new PutObjectRequest(bucketName,filename,stream,meta));
			new StringBuilder().append(bucketUrl).append("/").append(filename).toString();
		} catch (Exception e) {
			logger.error("Failed to upload the image to bucket: {} ",e.getMessage());
			return null;
		}
		 return null;
	}


}

