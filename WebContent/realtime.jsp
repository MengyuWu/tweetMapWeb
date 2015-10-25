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
<%@page import="com.google.gson.Gson" %>

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



<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>real time tweet map</title>
    <style>
      html, body {
        height: 100%;
        margin: 0;
        padding: 0;
      }
      #map {
        height: 100%;
        width: 80%;
      }
     /*  .side-bar {
          position: fixed;
          right: 0;
          top: 0;
          width: 20%;
          background: #fff;
      } */
#floating-panel {
  position: absolute;
  top: 10px;
  left: 25%;
  z-index: 5;
  background-color: #fff;
  padding: 5px;
  border: 1px solid #999;
  text-align: center;
  font-family: 'Roboto','sans-serif';
  line-height: 30px;
  padding-left: 10px;
}

      #floating-panel {
        background-color: #fff;
        border: 1px solid #999;
        left: 25%;
        padding: 5px;
        position: absolute;
        top: 10px;
        z-index: 5;
      }
    </style>
  </head>

  <body>
    <div id="floating-panel">
      <button onclick="toggleHeatmap()">Toggle Heatmap</button>
      <button onclick="changeGradient()">Change gradient</button>
      <button onclick="changeRadius()">Change radius</button>
      <button onclick="changeOpacity()">Change opacity</button>
      
      <select id="category" onchange="getCategoy(this)">
      <option value="">All</option>
	  <option value="recreation">recreation</option>
	  <option value="science">science_technology</option>
	  <option value="sports">sports</option>
	  <option value="arts">arts_entertainment</option>
	  <option value="business">business</option>
	  </select>
      <div> Tweet Num:<span id="Counter">0</span></div>
    </div>

   <div id="map"></div>
   <!--<div class="side-bar"></div> -->

   <script src="http://code.jquery.com/jquery-latest.min.js"></script>
   <script>

//global valuable;
var map, heatmap, pointsmap;
var markers = [];
var circles = [];
var flag = "heat";
var tweetDataJS;

// color
var b_default="#0000FF";
var r_negative="#FF0000";
var g_positive="#009900";
var y_neutral="#FFCC00";

function initMap() {
	
  initializeData();
  
  var mapProp = {
		  center:{lat: 37.775, lng: -122.434},
		  zoom:2,
		  mapTypeId:google.maps.MapTypeId.ROADMAP
  };
  
  map = new google.maps.Map(document.getElementById('map'), mapProp);

     google.maps.event.addListenerOnce(map, 'idle', function(){
      requestData();
  // document.getElementById('ajax_loading_icon').style.display = "none";
  // document.getElementById('map_canvas').style.visibility = "visible";
  });
     
  // google.maps.event.addDomListener(window, 'load', requestData);
  // requestData();    
  
  heatmap = new google.maps.visualization.HeatmapLayer({
	data: getPoints(tweetDataJS),
    map: map
  });
}


function getPointsMap(){
	flag = "points";
	
/* 	var mapProp = {
			  center:{lat: 37.775, lng: -122.434},
			  zoom:13,
			  mapTypeId:google.maps.MapTypeId.ROADMAP
			  };

	  
	map = new google.maps.Map(document.getElementById('map'), mapProp); */
	
	var positions = getPoints(tweetDataJS);
	var sentiments = getSentiment(tweetDataJS);
	
	  for (i in positions) {
	  	var color = getSentimentColor(sentiments[i]);
	  	var latLng = positions[i];
	    var point = new google.maps.Circle({
	  		  center: latLng,
	  		  radius:20000,
	  		  strokeColor:color,
	  		  strokeOpacity:0.8,
	  		  strokeWeight:2,
	  		  fillColor:color,
	  		  fillOpacity:0.9
	  		  });
	  	point.setMap(map); 
	  	circles.push(point);
	  	
	  	//marker:
	  	/* var marker = new google.maps.Marker({
	  	    position:{lat: latLng.lat(), lng:latLng.lng()},
	  	    map:map,
	  	    title: 'Hello World!'
	  	  }); 
	  	markers.push(marker); */
	  }
};


function getSentimentColor(sentiment){
	/* var b_default="#0000FF";
	var r_negative="#FF0000";
	var g_positive="#009900";
	var y_neutral="#FFCC00"; */
	var color=b_default;
	if (sentiment == "negative") {
		color=r_negative;
	} else if (sentiment == "positive") {
		color = g_positive;
	} else if (sentiment == "y_neutral") {
		color = y_neutral
	}
	return color;
};


function getHeatMap(){
	flag = "heat";
	
	/* var mapProp = {
			  center:{lat: 37.775, lng: -122.434},
			  zoom:13,
			  mapTypeId:google.maps.MapTypeId.ROADMAP
			  };

	map = new google.maps.Map(document.getElementById('map'), mapProp); */
	heatmap = new google.maps.visualization.HeatmapLayer({
	    data: getPoints(tweetDataJS),
	    map: map
	  });
};


function toggleHeatmap() {
	if (flag == "heat") {
		 heatmap.setMap(null);
		 getPointsMap();
	 } else if (flag="points") {
		 DeletePoints();
		 getHeatMap();
	 }
};

function DeletePoints() {
    // Loop through all the markers and remove
    for (var i = 0; i < circles.length; i++) {
    	circles[i].setMap(null);
    }

    circles = [];
};

function changeGradient() {
  var gradient = [
    'rgba(0, 255, 255, 0)',
    'rgba(0, 255, 255, 1)',
    'rgba(0, 191, 255, 1)',
    'rgba(0, 127, 255, 1)',
    'rgba(0, 63, 255, 1)',
    'rgba(0, 0, 255, 1)',
    'rgba(0, 0, 223, 1)',
    'rgba(0, 0, 191, 1)',
    'rgba(0, 0, 159, 1)',
    'rgba(0, 0, 127, 1)',
    'rgba(63, 0, 91, 1)',
    'rgba(127, 0, 63, 1)',
    'rgba(191, 0, 31, 1)',
    'rgba(255, 0, 0, 1)'
  ]
  heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
}

function changeRadius() {
  heatmap.set('radius', heatmap.get('radius') ? null : 20);
}

function changeOpacity() {
  heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

function getPoints(data) {
  return data.map(function(tweet) {
      return new google.maps.LatLng(
          tweet['lat'],
          tweet['lng']
      );
  });
};

function getSentiment(data){
	return data.map(function(tweet) {
	     return tweet['sentiment'];
	});
}

function requestData() {
  window.setInterval(function() {
  	// alert("real time update");
  	var e = document.getElementById("category");
	var key = e.options[e.selectedIndex].value;
	
    $.getJSON('MainServlet',{
        category:key
    },  function(data) {
      // update local data;
      tweetDataJS = data;
      updateCounter(data);
     
       if (flag == "heat") {
         // TODO: update points in heatmap
          if (heatmap && typeof heatmap.setMap === 'function') {
            heatmap.setMap(null);
          }
          getHeatMap();
       } else if (flag == "points") {
         //TODO: update points in heatmap
         DeletePoints();
         getPointsMap();
       }
     populateSideBar(data);
   });
  }, 30000);
}; 

function initializeData() {
	  $.getJSON('MainServlet', function(data) {
	      // Update local data.
	      tweetDataJS = data;
	      updateCounter(data);
	     
	       if (flag == "heat") {
	         // Update points in heatmap.
	         if (heatmap && typeof heatmap.setMap === 'function') {
	           heatmap.setMap(null);
	          }
	          getHeatMap();
	       } else if (flag == "points") {
	         // Update points in heatmap.
	         DeletePoints();
	         getPointsMap();
	       }
	      // populateSideBar(data);
	  });
};

function getCategoy(category) {
    var key = category.value;  
    // document.write(key);
    $.getJSON('MainServlet',{
         category:key
       },  function(data) {
        // Update local data.
        tweetDataJS = data;
        updateCounter(data);
       
         if (flag == "heat") {
           // Update points in heatmap.
           if (heatmap && typeof heatmap.setMap === 'function') {
              heatmap.setMap(null);
            }
            getHeatMap();
         } else if (flag == "points") {
           // Update points in heatmap.
           DeletePoints();
           getPointsMap();
         }
  
     });
};

function updateCounter(data) {
  document.getElementById("Counter").innerHTML = data.length;
};

// Sidebar
function populateSideBar(data) {
  $('.side-bar').empty();
  for (var i = 0; i <= 10; i++) {
      $('.side-bar').append(
        '<div>' + data[i]['content'] + '</div><hr>'
      );
  }
};

    </script>
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAYAptnpHlY5oYzYwUz_lPE3RyIrR5cJpU&signed_in=true&libraries=visualization&callback=initMap">
    </script>
  </body>
</html>
