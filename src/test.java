import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class test {

	public static void main(String [ ] args) {
		
		String createdDate = "2015-10-25T05:23:46.000Z";
	    DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	    DateFormat toFormat = new SimpleDateFormat("EEE MM/dd/yyyy kk:mm:ss z");
	    Date date;
	    String createdstr;
		try {
			date = fromFormat.parse(createdDate);
		    createdstr = toFormat.format(date);
		} catch (ParseException e) {
			createdstr = createdDate;
		}
		
		System.out.println(createdstr);
		
		// Ascending array list of ints
		ArrayList<Integer> array = new ArrayList<Integer>();
		array.add(1);
		array.add(3);
		array.add(4);
		System.out.println(array);
		array.add(1, 2);
		System.out.println(array);
		array.add(6);
		System.out.println(array);

		int index = array.size() - 1;
		int a = 5;
		while (index >= 0 && array.get(index) > a) {
			index--;
		}
		
		array.add(++index, a);
		System.out.println(array);
		
		// Descending array list of hash maps take 1
		// Expect 55 - 62 - 66 order
//		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
//		
//		String newest = "1445751455000";
//		String second = "1445751062000";
//		String oldest = "1445750966000";
//		
//	    HashMap<String, String> newest_tweet = new HashMap<String, String>();
//	    newest_tweet.put("created", newest);
//		
//	    HashMap<String, String> oldest_tweet = new HashMap<String, String>();
//	    oldest_tweet.put("created", oldest);
//
//	    HashMap<String, String> second_tweet = new HashMap<String, String>();
//	    second_tweet.put("created", second);
//	    
//	    tweets.add(newest_tweet);
//	    tweets.add(oldest_tweet);
//	    System.out.println(tweets);
//	    
//		int position = tweets.size() - 1;
//
//			while (position >= 0 && Long.parseLong(tweets.get(position).get("created")) < Long.parseLong(second)) {
//				position--;
//			}	
//			tweets.add(++position, second_tweet);
//			
//		System.out.println(tweets);
	    
		// Descending array list of hash maps take 2
		
		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
		
		String newest = "1445751455000";
		String second = "1445751062000";
		String oldest = "1445750966000";
		
	    HashMap<String, String> newest_tweet = new HashMap<String, String>();
	    newest_tweet.put("created", newest);
		
	    HashMap<String, String> oldest_tweet = new HashMap<String, String>();
	    oldest_tweet.put("created", oldest);

	    HashMap<String, String> second_tweet = new HashMap<String, String>();
	    second_tweet.put("created", second);
	    
	    tweets.add(newest_tweet);
	    tweets.add(oldest_tweet);
	    System.out.println(tweets);
	    
		int position = 0;

			while (position < tweets.size() && Long.parseLong(tweets.get(position).get("created")) > Long.parseLong(second)) {
				position++;
			}	
			tweets.add(position, second_tweet);
			
		System.out.println(tweets);
		
	}
}

