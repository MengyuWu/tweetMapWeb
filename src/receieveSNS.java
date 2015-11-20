

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tweetBasic.Tweet;
import static tweetBasic.AWSResourceSetup.*;

import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

/**
 * Servlet implementation class receieveSNS
 */
public class receieveSNS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public receieveSNS() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setAttribute("newTweet", "new tweet test");
		request.getRequestDispatcher("realtime.jsp").forward(request, response);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, SecurityException {
		String messagetype = req.getHeader("x-amz-sns-message-type");
		if (messagetype == null)
			return;

		Scanner scan = new Scanner(req.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		scan.close();
		System.out.println("Received SNS: " + builder.toString());
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> userData = mapper.readValue(builder.toString(), Map.class);
		
		if (messagetype.equals("Notification")) {
			
			String id=(String) userData.get("Message");
			if(id==null){
				return;
			}
			
			Tweet tweet=null;
			try {
				tweet = DYNAMODB_MAPPER.load(Tweet.class, id);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Create tweet hash.
			double latD=tweet.getGeoLat();
			double lngD=tweet.getGeoLng();
			String content=tweet.getContent();
			String username=tweet.getUsername();
			String categorydb=tweet.getCategory();
			String sentiment=tweet.getSentiment();
			
			String lat=Double.toString(latD);
			String lng=Double.toString(lngD);
			ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
			HashMap<String,String> tweetHM = new HashMap<String,String>();
			tweetHM.put("lat", lat);
			tweetHM.put("lng", lng);
			tweetHM.put("content", content);
			tweetHM.put("username", username);
			tweetHM.put("category", categorydb);
			tweetHM.put("sentiment", sentiment);
			tweets.add(tweetHM);
			// Convert object to JSON format.
			String json = new Gson().toJson(tweets);
			req.setAttribute("newTweet", json);
			//req.setCharacterEncoding("UTF-8");
			//resp.setContentType("application/json");
			//resp.setCharacterEncoding("UTF-8");
			//resp.getWriter().write(json);
			
			req.getRequestDispatcher("realtime.jsp").forward(req, resp);
			
			
			
		}else if(messagetype.equals("SubscriptionConfirmation")){
			String Token="";
			if(userData.get("Token")!=null){
				Token=(String) userData.get("Token");
			}
			
			mySNSHelper.confirmTopicSubmission(Token);
		}
		
	}
	
}
