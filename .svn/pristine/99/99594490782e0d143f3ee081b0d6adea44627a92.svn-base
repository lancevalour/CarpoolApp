package yicheng.carpoolapp;

import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;


public class AmazonClientManager {
	  private static final String LOG_TAG = "AmazonClientManager";

	    private AmazonS3Client s3Client = null;
	    private AmazonSQSClient sqsClient = null;
	    private AmazonSimpleDBAsyncClient sdbClient = null;
	    private AmazonSNSClient snsClient = null;
	    
	    public AmazonClientManager() {
	    }
	                
	    public AmazonS3Client s3() {
	        validateCredentials();
	        return s3Client;
	    }
	        
	    public AmazonSQSClient sqs() {
	        validateCredentials();    
	        return sqsClient;
	    }

	    public AmazonSimpleDBAsyncClient sdb() {
	        validateCredentials();    
	        return sdbClient;
	    }

	    public AmazonSNSClient sns() {
	        validateCredentials();    
	        return snsClient;
	    }
	    
	    public boolean hasCredentials() {
	        return PropertyLoader.getInstance().hasCredentials();
	    }
	    
	    public void validateCredentials() {
	        if ( s3Client == null || sqsClient == null || sdbClient == null || snsClient == null ) {        
	            Log.i( LOG_TAG, "Creating New Clients." );
	            
	            Region region = Region.getRegion(Regions.US_WEST_2); 
	        
	            AWSCredentials credentials = new BasicAWSCredentials( PropertyLoader.getInstance().getAccessKey(), PropertyLoader.getInstance().getSecretKey() );
			    s3Client = new AmazonS3Client( credentials );
			    s3Client.setRegion(region);
			    
			    sqsClient = new AmazonSQSClient( credentials );
			    sqsClient.setRegion(region);
			    
			    sdbClient = new AmazonSimpleDBAsyncClient( credentials );
			    sdbClient.setRegion(region);
			    
			    snsClient = new AmazonSNSClient( credentials );
			    snsClient.setRegion(region);
	        }
	    }
	    
	    public void clearClients() {
	        s3Client = null;
	        sqsClient = null;
	        sdbClient = null;
	        snsClient = null;    
	    }
}
