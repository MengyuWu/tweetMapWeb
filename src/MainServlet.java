import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.gson.Gson;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import static tweetBasic.AWSResourceSetup.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tweetBasic.Tweet;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

//mw2907, nb2406

/**
 * Servlet implementation class MainServlet
 */
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ConfigurationBuilder cb = new ConfigurationBuilder();

	static{
		cb.setDebugEnabled(true)
	       .setOAuthConsumerKey("ZuBFtIjHubaHmaomVCN6HNRI5")
	       .setOAuthConsumerSecret("Ll0LZgKPly3QvIxIYMhtAxwxeUkleHc9Xya1Q5zAPxaga2wIpD")
	       .setOAuthAccessToken("3095412628-4kLyHeZWV3p4Swmqx0d2lGSfJbtNqPbl0VPuMta")
	       .setOAuthAccessTokenSecret("bKdTWWUVrtg1WtTog65t2XscxdvNbHszxDQLHBpZkutIG");
	}


    private static TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
//        super();
//        // TODO Auto-generated constructor stub
//
//       StatusListener listener = new StatusListener() {
//           @Override
//           public void onStatus(Status status) {
////               System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
////               System.out.println(" user location:"+status.getUser().getLocation());
////               System.out.println(" Geo location:"+status.getGeoLocation());
//
//               if(status.getGeoLocation() != null){
//                  System.out.println("Has Geo location:"+status.getGeoLocation());
//               	long id = status.getId();
//                   String strId = String.valueOf(id);
//                   String username = status.getUser().getScreenName();
//                   String content = status.getText();
//                   String userLocation = status.getUser().getLocation();
//                   double geoLat = 0;
//                   double geoLng = 0;
//                   Date createdAt = status.getCreatedAt();
//
//                   if(status.getGeoLocation() != null){
//                   	geoLat = status.getGeoLocation().getLatitude();
//                   	geoLng = status.getGeoLocation().getLongitude();
//                   }
//
//                   Tweet t = new Tweet(strId, username,content, userLocation, geoLat,geoLng, createdAt);
//                   t.saveTweetToDynamoDB();
//                   System.out.println("save tweet");
//               }
//
//
//               //System.exit(0);
//
//           }
//
//           @Override
//           public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//              // System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
//           }
//
//           @Override
//           public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
//               System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
//           }
//
//           @Override
//           public void onScrubGeo(long userId, long upToStatusId) {
//               System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
//           }
//
//           @Override
//           public void onStallWarning(StallWarning warning) {
//               System.out.println("Got stall warning:" + warning);
//           }
//
//           @Override
//           public void onException(Exception ex) {
//               ex.printStackTrace();
//           }
//       };
//       twitterStream.addListener(listener);
//       twitterStream.sample();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Do get");
		System.out.println("category:"+request.getParameter("category"));
		String category=request.getParameter("category");
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		 Condition condition = new Condition()
		    .withComparisonOperator(ComparisonOperator.NE.toString())
		    .withAttributeValueList(new AttributeValue().withN("0"));
		Condition condition2 = new Condition()
		 	.withComparisonOperator(ComparisonOperator.NE.toString())
		    .withAttributeValueList(new AttributeValue().withN("0"));
		
		Condition condition3 = new Condition()
	 	.withComparisonOperator(ComparisonOperator.CONTAINS);
	    
		
		
		scanFilter.put("geoLat", condition);
		scanFilter.put("geoLng",condition2);
		if(category!=null && !category.isEmpty()){
			condition3.withAttributeValueList(new AttributeValue().withS(category));
			scanFilter.put("content", condition3);
		}
		
		String tableName = DYNAMODB_TABLE_NAME;
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = DYNAMODB.scan(scanRequest);

		int size = scanResult.getItems().size();
		List<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
		for(int i = 0; i<size; i++){
			String latstr = scanResult.getItems().get(i).get("geoLat").getN();
			String lngstr = scanResult.getItems().get(i).get("geoLng").getN();
			String content = scanResult.getItems().get(i).get("content").getS();
			String username = scanResult.getItems().get(i).get("username").getS();
//			double lat = Double.parseDouble(latstr);
//			double lng = Double.parseDouble(lngstr);
			
			HashMap<String,String> tweet = new HashMap<String,String>();
			tweet.put("lat", latstr);
			tweet.put("lng", lngstr);
			tweet.put("content", content);
			tweet.put("username", username);

			tweets.add(tweet);
		}

		//convert object to JSON format
		System.out.println("size: "+tweets.size());
		String json = new Gson().toJson(tweets);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	 }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
