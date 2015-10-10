<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="com.amazonaws.*" %>
<%@ page import="com.amazonaws.auth.*" %>
<%@ page import="com.amazonaws.services.ec2.*" %>
<%@ page import="com.amazonaws.services.ec2.model.*" %>
<%@ page import="com.amazonaws.services.s3.*" %>
<%@ page import="com.amazonaws.services.s3.model.*" %>
<%@ page import="com.amazonaws.services.dynamodbv2.*" %>
<%@ page import="com.amazonaws.services.dynamodbv2.model.*" %>
<%@page  import="java.util.HashMap" %>
<%@page  import="java.util.List" %>
<%@page  import="java.util.ArrayList" %>
<%@page  import="static tweetBasic.AWSResourceSetup.*" %>

<%! // Share the client objects across threads to
    // avoid creating new clients for each web request
    private AmazonEC2         ec2;
    private AmazonS3           s3;
    private AmazonDynamoDB dynamo;
 %>

<%
    /*
     * AWS Elastic Beanstalk checks your application's health by periodically
     * sending an HTTP HEAD request to a resource in your application. By
     * default, this is the root or default resource in your application,
     * but can be configured for each environment.
     *
     * Here, we report success as long as the app server is up, but skip
     * generating the whole page since this is a HEAD request only. You
     * can employ more sophisticated health checks in your application.
     */
    if (request.getMethod().equals("HEAD")) return;
%>

<%
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
	
	//String createdBy=scanResult.getItems().get(0).get("createdBy").getS();
	//String creationTime=scanResult.getItems().get(0).get("creationTime").getS();
	//out.println(scanResult);
	
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
		out.println("lat: "+lat+" lng: "+lng);
		
	}
	
    	          		 
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <title>Tweet Map</title>
    <link rel="stylesheet" href="styles/styles.css" type="text/css" media="screen">
</head>
<body>
    
        <div class="section grid grid5 sdb">
            
        </div>

        
</body>
</html>