

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Servlet implementation class TrendsServlet
 */
public class TrendsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static ResponseList<Location> locations;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TrendsServlet() {
        super();
        getLocationIds();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String place = request.getParameter("place");
		String[] trends = getTrends(place);

		// Convert object to JSON format.
		String json = new Gson().toJson(trends);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
    private static void getLocationIds() {
	    try {
	
	    	ConfigurationBuilder cb = new ConfigurationBuilder();
	        cb.setDebugEnabled(true)
	           .setOAuthConsumerKey("xB127kA8Wn91LeZLJNHn7DQLD")
	           .setOAuthConsumerSecret("XEAEaQBWlDirhoT8noQmbbwDhbBjvjzQcEJIO80eZXIWj6nTKn")
	           .setOAuthAccessToken("605341080-dSZi4aYnavhiewli3oxRow2aLMpUdv58cqiM5Wmh")
	           .setOAuthAccessTokenSecret("OIDy5WNOgzcvl1NnbJIneZWTSos3v9CfOeR0NMok8eLCt");
	        
	        TwitterFactory tf = new TwitterFactory(cb.build());
	        Twitter twitter = tf.getInstance();
	
	        locations = twitter.getAvailableTrends();
	        
	    } catch (TwitterException te) {
	        te.printStackTrace();
	        System.out.println("Failed to get trends: " + te.getMessage());
	    }

    }
    
    private static Integer getTrendLocationId(String locationName) {
	    int idTrendLocation = 0;
        for (Location location : locations) {
	        if (location.getName().toLowerCase().equals(locationName.toLowerCase())) {
	            idTrendLocation = location.getWoeid();
	            break;
	        }
        }

        if (idTrendLocation > 0) {
        	return idTrendLocation;
        }

        return null;
    }
    
    private static String[] getTrends(String trendLocation) {
	    try {
	    	
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			   .setOAuthConsumerKey("xB127kA8Wn91LeZLJNHn7DQLD")
			   .setOAuthConsumerSecret("XEAEaQBWlDirhoT8noQmbbwDhbBjvjzQcEJIO80eZXIWj6nTKn")
			   .setOAuthAccessToken("605341080-dSZi4aYnavhiewli3oxRow2aLMpUdv58cqiM5Wmh")
			   .setOAuthAccessTokenSecret("OIDy5WNOgzcvl1NnbJIneZWTSos3v9CfOeR0NMok8eLCt");
	
			TwitterFactory tf = new TwitterFactory(cb.build());
	        Twitter twitter = tf.getInstance();
	
	        Integer idTrendLocation = getTrendLocationId(trendLocation);
	
	        if (idTrendLocation == null) {
	        	System.out.println("Trend Location Not Found");
	        	return null;
	        }
	
	        Trends trends = twitter.getPlaceTrends(idTrendLocation);
	        int numTrends = trends.getTrends().length;
	        String[] keywords = new String[numTrends];
	        
	        for (int i = 0; i < numTrends; i++) {
	        	String trend = trends.getTrends()[i].getName();
	        	keywords[i] = trend;
	        	System.out.println(trend);
	        }
	        	        
	        return keywords;
	
	    } catch (TwitterException te) {
	        te.printStackTrace();
	        System.out.println("Failed to get trends: " + te.getMessage());
	        System.exit(-1);
	        return null;
	    }
	    
    }

}
