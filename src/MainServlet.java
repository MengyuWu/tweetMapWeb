import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.google.gson.Gson;

import static tweetBasic.AWSResourceSetup.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MainServlet
 */
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Do get");
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		 Condition condition = new Condition()
		    .withComparisonOperator(ComparisonOperator.NE.toString())
		    .withAttributeValueList(new AttributeValue().withN("0"));
		Condition condition2=new Condition()
		 	.withComparisonOperator(ComparisonOperator.NE.toString())
		    .withAttributeValueList(new AttributeValue().withN("0"));
		scanFilter.put("geoLat", condition);
		scanFilter.put("geoLng",condition2); 
		String tableName=DYNAMODB_TABLE_NAME;
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = DYNAMODB.scan(scanRequest);
		
		int size=scanResult.getItems().size();
		List<List<Double>> locations=new ArrayList<List<Double>>();
		for(int i=0; i<size; i++){
			String latstr=scanResult.getItems().get(i).get("geoLat").getN();
			String lngstr=scanResult.getItems().get(i).get("geoLng").getN();
			double lat=Double.parseDouble(latstr);
			double lng=Double.parseDouble(lngstr);
			ArrayList<Double> pair=new ArrayList<Double>();
			pair.add(lat);
			pair.add(lng);
			locations.add(pair);
		}
		
		//convert object to JSON format
		String json = new Gson().toJson(locations);
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
