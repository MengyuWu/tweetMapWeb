

import static tweetBasic.AWSResourceSetup.SQS;
import static tweetBasic.AWSResourceSetup.SQS_QUEUE_NAME;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import tweetBasic.Tweet;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Servlet implementation class TweetsServlet
 */
public class TweetsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static TwitterStream twitterStream;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TweetsServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String streaming = request.getParameter("streaming"); // check if on/off
		
		if (streaming.equalsIgnoreCase("off")) {
			twitterStream.shutdown();
		} else {
			String trends = request.getParameter("trends");
			String[] keywords = trends.split(",");

			ConfigurationBuilder cb = new ConfigurationBuilder();
	        cb.setDebugEnabled(true)
	           .setOAuthConsumerKey("xB127kA8Wn91LeZLJNHn7DQLD")
	           .setOAuthConsumerSecret("XEAEaQBWlDirhoT8noQmbbwDhbBjvjzQcEJIO80eZXIWj6nTKn")
	           .setOAuthAccessToken("605341080-dSZi4aYnavhiewli3oxRow2aLMpUdv58cqiM5Wmh")
	           .setOAuthAccessTokenSecret("OIDy5WNOgzcvl1NnbJIneZWTSos3v9CfOeR0NMok8eLCt");
	         
	        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	        StatusListener listener = new StatusListener() {
	            @Override
	            public void onStatus(Status status) {
	                
	                if (status.getGeoLocation() != null) {

	                	long id=status.getId();
	                    String strId=String.valueOf(id);
	                    String username=status.getUser().getScreenName();
	                    String content=status.getText();
	                    String userLocation=status.getUser().getLocation();
	                    double geoLat = 0;
	                    double geoLng = 0;
	                    Date createdAt=status.getCreatedAt();
	                    
	                    if (status.getGeoLocation() != null) {
	                    	geoLat=status.getGeoLocation().getLatitude();
	                    	geoLng=status.getGeoLocation().getLongitude();
	                    }
	                    
	                    // Save tweet to DynamoDB and send to SQS for sentiment processing
	                    if (status.getGeoLocation() != null) {
	                    	Tweet t = new Tweet(strId, username, content, userLocation, geoLat, geoLng, createdAt);
	                        t.saveTweetToDynamoDB();
		                    String queueUrl = SQS.getQueueUrl(new GetQueueUrlRequest(SQS_QUEUE_NAME)).getQueueUrl();
	                        SQS.sendMessage(new SendMessageRequest(queueUrl, t.getId()));
	                    }
                   
	                }
               
	            }

	           @Override
	           public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	              //System.out.println("[TweetGet] Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	           }
	
	           @Override
	           public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	               System.out.println("[TweetGet] Got track limitation notice:" + numberOfLimitedStatuses);
	           }
	
	           @Override
	           public void onScrubGeo(long userId, long upToStatusId) {
	               System.out.println("[TweetGet] Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	           }
	
	           @Override
	           public void onStallWarning(StallWarning warning) {
	               System.out.println("[TweetGet] Got stall warning:" + warning);
	           }
	
	           @Override
	           public void onException(Exception ex) {
	               ex.printStackTrace();
	           }
	       };
	       
	       twitterStream.addListener(listener);
	       
	       FilterQuery qry = new FilterQuery();
	
	       qry.track(keywords);
	
	       twitterStream.filter(qry);
	       twitterStream.sample();
		
		}

		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
