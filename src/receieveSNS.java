

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import tweetBasic.Tweet;
import static tweetBasic.AWSResourceSetup.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;



/**
 * Servlet implementation class receieveSNS
 */
public class receieveSNS extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String test="";
	private static Queue<String> tweetQueue=new LinkedList<String>();
	
	
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
		
		response.setContentType("text/event-stream");   
		 //encoding must be set to UTF-8
		response.setCharacterEncoding("UTF-8");
	 
	     PrintWriter writer = response.getWriter();
	     if(!test.equals("")){
	    	 writer.write("data: "+ test+ "\n\n");
	     }
	     
	     while(!tweetQueue.isEmpty()){
	    	 String t=tweetQueue.poll();
	    	 test=t;
	    	 writer.write("data: "+t +"\n\n");
	    	 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	     }
	     
	     test="";
		
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
		if (messagetype != null){
			
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
				String lat=Double.toString(latD);
				double lngD=tweet.getGeoLng();
				String lng=Double.toString(lngD);
				String content=tweet.getContent();
				String username=tweet.getUsername();
				String categorydb=tweet.getCategory();
				String sentiment=tweet.getSentiment();
				Long createdL=tweet.getCreatedLong();
				String created=Long.toString(createdL);
				Date date=tweet.getCreatedDate();
				// Format date.
			    DateFormat toFormat = new SimpleDateFormat("MMM dd · k:mm z");
			    String createdstr = toFormat.format(date);
	
				HashMap<String,String> tweetHM = new HashMap<String,String>();
				tweetHM.put("lat", lat);
				tweetHM.put("lng", lng);
				tweetHM.put("content", content);
				tweetHM.put("username", username);
				tweetHM.put("category", categorydb);
				tweetHM.put("sentiment", sentiment);
				tweetHM.put("created", created);
				tweetHM.put("createdstr", createdstr);
				
				// Convert object to JSON format.
				String json = new Gson().toJson(tweetHM);
				System.out.println("json:"+json);
				//put into the queue
				tweetQueue.add(json);
				
			}else if(messagetype.equals("SubscriptionConfirmation")){
				String Token="";
				if(userData.get("Token")!=null){
					Token=(String) userData.get("Token");
				}
				
				mySNSHelper.confirmTopicSubmission(Token);
			}
		}else{
			//test
			System.out.println("test");
			String msg=req.getParameter("msg");
			if(msg!=null){
				test=msg;
			}
		
		}
			
		
		
		
	}
	
}
