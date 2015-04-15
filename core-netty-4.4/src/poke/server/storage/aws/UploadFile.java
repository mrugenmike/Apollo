package poke.server.storage.aws;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class UploadFile {

	
	
	 public static void main(String[] args) throws IOException {

	        /*
	         * The ProfileCredentialsProvider will return your [default]
	         * credential profile by reading from the credentials file located at
	         * (/Users/harshadkulkarni/.aws/credentials).
	         */
	        AWSCredentials credentials = null;
	        try {
	           // credentials = new ProfileCredentialsProvider("default").getCredentials();
	        	credentials=new BasicAWSCredentials("AKIAJQ2I4FRN4YWSXCXQ", "3UVmA3agUfdHt+zN0QP7IEKEli1KBW/uY4XDTOJy");

	        } catch (Exception e) {
	           
	        }

	        AmazonS3 s3 = new AmazonS3Client(credentials);
	        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
	        s3.setRegion(usWest2);

	      // String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
	        String bucketName="raftlog";
	        String key = "harshaddddd";

	        System.out.println("===========================================");
	        System.out.println("Getting Started with Amazon S3");
	        System.out.println("===========================================\n");

	        try {
	          
	             
	            System.out.println("Creating bucket " + bucketName + "\n");

	            System.out.println("Listing buckets");
	            for (Bucket bucket : s3.listBuckets()) {
	            	 bucketName=bucket.getName();
	            	 System.out.println("@@@bucket name:"+bucketName);
	                //System.out.println(" - " + bucket.getName());
	            }
	          
	            System.out.println("Uploading a new object to S3 from a file\n");
	            s3.setRegion(Region.getRegion(Regions.US_WEST_1));
	            s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));

	            
	            System.out.println("Downloading an object");
	            S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
	            System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
	            //displayTextInputStream(object.getObjectContent());

	            System.out.println("Listing objects");
	            ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
	                    .withBucketName(bucketName)
	                    .withPrefix("My"));
	            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
	                System.out.println(" - " + objectSummary.getKey() + "  " +
	                                   "(size = " + objectSummary.getSize() + ")");
	            }
	            System.out.println();

	          
	       
	        } catch (AmazonServiceException ase) {
	            System.out.println("Caught an AmazonServiceException, which means your request made it "
	                    + "to Amazon S3, but was rejected with an error response for some reason.");
	            System.out.println("Error Message:    " + ase.getMessage());
	            System.out.println("HTTP Status Code: " + ase.getStatusCode());
	            System.out.println("AWS Error Code:   " + ase.getErrorCode());
	            System.out.println("Error Type:       " + ase.getErrorType());
	            System.out.println("Request ID:       " + ase.getRequestId());
	        } catch (AmazonClientException ace) {
	            System.out.println("Caught an AmazonClientException, which means the client encountered "
	                    + "a serious internal problem while trying to communicate with S3, "
	                    + "such as not being able to access the network.");
	            System.out.println("Error Message: " + ace.getMessage());
	        }
	    }

	    /**
	     * Creates a temporary file with text data to demonstrate uploading a file
	     * to Amazon S3
	     *
	     * @return A newly created temporary file with text data.
	     *
	     * @throws IOException
	     */
	    private static File createSampleFile() throws IOException {
	      
	        File file= new File("/Users/harshadkulkarni/Desktop/dp.jpeg");
	        return file;
	    }

	    /**
	     * Displays the contents of the specified input stream as text.
	     *
	     * @param input
	     *            The input stream to display as text.
	     *
	     * @throws IOException
	     */
	    private static void displayTextInputStream(InputStream input) throws IOException {
	        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
	        while (true) {
	            String line = reader.readLine();
	            if (line == null) break;

	            System.out.println("    " + line);
	        }
	        System.out.println();
	    }

	
	
	
	
	}