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
      body {
      	font-family: Helvetica Neue, Helvetica, Arial, sans-serif; 
     }
      .map-container {
      	width: 80%;
      	height: 100%;
      	position: relative;
      }
      .map-container.active {
      	width: 98%;
      }
      #floating-panel {
		  position: absolute;
		  top: 10px;
		  width: 60%;
		  left: 20%;
		  z-index: 5;
		  background-color: #fff;
		  padding: 5px;
		  border: 1px solid #ccc;
		  text-align: center;
		  font-family: 'Roboto','sans-serif';
		  line-height: 30px;
		  padding-left: 10px;
		  -webkit-box-shadow: 1px 1px 14px -1px rgba(0,0,0,0.4);
		  -moz-box-shadow: 1px 1px 14px -1px rgba(0,0,0,0.4);
		  -ms-box-shadow: 1px 1px 14px -1px rgba(0,0,0,0.4);
		  box-shadow: 1px 1px 14px -1px rgba(0,0,0,0.4);
		}
      #map {
        height: 100%;
        width: 100%;
      }
      .tweet-point {
      	width: 8px;
      	height: 8px;
		border-radius: 150px;
     }
      .side-bar-container {
         position: fixed;
         right: 0;
         top: 0;
         width: 20%;
         height: 100%;
         overflow: auto;
         background: #fff;
      }
      .side-bar-container.active {
      	right: -18%;
      }
       .side-bar {
      	width: 90%;
      	float: left;
      }
      .side-bar-toggle {
      	cursor: pointer;
      	background: #4099FF;
      	width: 10%;
      	height: 100%;
      	float: left;
      }
      .side-bar-toggle .arrow {
      	width: 100%;
    	-webkit-transform: rotate(0deg);
      	-moz-transform: rotate(0deg);
      	-ms-transform: rotate(0deg);
      	transform: rotate(0deg);
      }
      .side-bar-toggle .arrow.closed {
		-webkit-transform: rotate(180deg);
      	-moz-transform: rotate(180deg);
      	-ms-transform: rotate(180deg);
      	transform: rotate(180deg);
      }
      .user-tweet {
      	border-bottom: 1px solid #ccc;
      	padding-bottom: 5px;
      	margin: 5px;
      }
      .user-tweet .user-name {
      	font-size: 12px;
      	font-weight: bold;
      }
      .user-tweet .user-content {
      	font-size: 10px;
      }
      .user-tweet .user-created {
        font-size: 10px;
        color: #ccc;
      }
      .user-tweet .user-sentiment {
        font-size: 10px;
        color: #4099FF;
      }
      .user-sentiment.positive, .user-tweet .user-sentiment.positive {
      	color: green;
      }
      .user-sentiment.negative, .user-tweet .user-sentiment.negative {
      	color: red;
      }
      .user-sentiment.neutral, .user-tweet .user-sentiment.neutral {
      	color: #FFCC00;
      }
      .fade-bg {
      	background: rgba(0,0,0,0.3);
      	position: fixed;
      	top: 0;
      	left: 0;
      	width: 100%;
      	height: 100%;
      	display: none;
      	z-index: 999;
     }
     .lightbox {
        width: 50%;
    	background: #fff;
    	z-index: 9999;
    	position: absolute;
    	top: 20%;
    	left: 25%;
    	display: none;
   	}
   	.lightbox .user {
   		font-size: 14px;
   		font-weight: bold;
   	}
   	.lightbox-tweet {
   		margin: 10px;
   		font-size: 12px;
   	}
   	
   	@-webkit-keyframes bounce {
	  from, 20%, 53%, 80%, to {
	    -webkit-animation-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
	    animation-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
	    -webkit-transform: translate3d(0,0,0);
	    transform: translate3d(0,0,0);
	  }
	
	  40%, 43% {
	    -webkit-animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    -webkit-transform: translate3d(0, -30px, 0);
	    transform: translate3d(0, -30px, 0);
	  }
	
	  70% {
	    -webkit-animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    -webkit-transform: translate3d(0, -15px, 0);
	    transform: translate3d(0, -15px, 0);
	  }
	
	  90% {
	    -webkit-transform: translate3d(0,-4px,0);
	    transform: translate3d(0,-4px,0);
	  }
	}
	
	@keyframes bounce {
	  from, 20%, 53%, 80%, to {
	    -webkit-animation-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
	    animation-timing-function: cubic-bezier(0.215, 0.610, 0.355, 1.000);
	    -webkit-transform: translate3d(0,0,0);
	    transform: translate3d(0,0,0);
	  }
	
	  40%, 43% {
	    -webkit-animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    -webkit-transform: translate3d(0, -30px, 0);
	    transform: translate3d(0, -30px, 0);
	  }
	
	  70% {
	    -webkit-animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    animation-timing-function: cubic-bezier(0.755, 0.050, 0.855, 0.060);
	    -webkit-transform: translate3d(0, -15px, 0);
	    transform: translate3d(0, -15px, 0);
	  }
	
	  90% {
	    -webkit-transform: translate3d(0,-4px,0);
	    transform: translate3d(0,-4px,0);
	  }
	}
	
	.bounce {
	  -webkit-animation-name: bounce;
	  animation-name: bounce;
	  -webkit-transform-origin: center bottom;
	  transform-origin: center bottom;
	}
	.animated {
	  -webkit-animation-duration: 1s;
	  animation-duration: 1s;
	  -webkit-animation-fill-mode: both;
	  animation-fill-mode: both;
	}

    </style>
  </head>

  <body>
  
  trends: <span id="trends"></span>
  <br><br>
  
  	<div class="map-container">
	    <div id="floating-panel">
	      <button onclick="toggleHeatmap()">Toggle heatmap</button>
	      <button onclick="changeGradient()">Change gradient</button>
	      <button onclick="changeRadius()">Change radius</button>
	      <button onclick="changeOpacity()">Change opacity</button>
	      
	      <select id="category" onchange="requestData()">
	      <option value="">All</option>
		  <option value="recreation">recreation</option>
		  <option value="science">science_technology</option>
		  <option value="sports">sports</option>
		  <option value="arts">arts_entertainment</option>
		  <option value="business">business</option>
		  </select>
		  
		  <select id="place" onchange="requestTrends()">
		  <option value="Worldwide">Worldwide</option>
		  <option value="New York">New York</option>
		  <option value="London">London</option>
		  <option value="Tokyo">Tokyo</option>
		  </select>
		  		  
	      <div> Total Tweets: <span id="Counter">0</span></div>
	    </div>
	
	   	<div id="map"></div>
  	</div>
   	<div class="side-bar-container">
   		<div class="side-bar-toggle">
   			<img class="arrow" src="${pageContext.request.contextPath}/images/right-arrow.png" />
   		</div>
   		<div class="side-bar"></div>
   	</div>

	<div class="fade-bg"></div>
	<div class="lightbox">
		<div class="lightbox-tweet">
			<h2 class="user"></h2>
			<div class="tweet"></div>
			<div class="user-sentiment"></div> 
		</div>
	</div>
   <script src="http://code.jquery.com/jquery-latest.min.js"></script>
   <script type="text/javascript" src="${pageContext.request.contextPath}/mapstyle.js"></script>
   <script>

//global valuable;
var map, heatmap, pointsmap;
var markers = [];
var circles = [];
var flag = "points";
var tweetDataJS;


// color
var b_default="#0000FF";
var r_negative="#FF0000";
var g_positive="#009900";
var y_neutral="#FFCC00";

function loadRichMarker() {
	var richLib = "https://google-maps-utility-library-v3.googlecode.com/svn/trunk/richmarker/src/richmarker-compiled.js";
	$.getScript(richLib, initMap);
}

function initMap() {
	
  requestData();
  requestTrends();
  startEventListening();
  
  var mapProp = {
		  center:{lat: 37.775, lng: -122.434},
		  zoom:2,
		  mapTypeId:google.maps.MapTypeId.ROADMAP,
		  styles:mapStyle
  };
  
  map = new google.maps.Map(document.getElementById('map'), mapProp);
            
  heatmap = new google.maps.visualization.HeatmapLayer({
	data: getPoints([]),
    map: map
  });
}

function startEventListening() {
	 
    var eventSource = new EventSource("receieveSNS");
     
    eventSource.onmessage = function(event) {
    	console.log(event);
    	var tweet = (JSON.parse(event.data));
    	// Avoid duplicated tweets
    	if (tweet['content'] != tweetDataJS[0]['content']) {
	    	tweetDataJS.unshift(tweet);
	        addMarker(tweet);
	        addToSideBar(tweet);
	    }
    };
     
}


function getPointsMap(){
	flag = "points";
	
	var positions = getPoints(tweetDataJS);
	var sentiments = getSentiment(tweetDataJS);
	
	  for (var i in positions) {
		  var color = getSentimentColor(sentiments[i]);
          var marker = new RichMarker({
              position: positions[i],
              map: map,
              shadow: 'none',
              content: '<div class="tweet-point" data-user="' + tweetDataJS[i]['username'] +
              '" data-content="' + tweetDataJS[i]['content'].replace('"', '\"') +
              '" data-created="' + tweetDataJS[i]['createdstr'] + 
              '" data-geoloc="' + '(' + Math.round(tweetDataJS[i]['lat']) + ', ' + Math.round(tweetDataJS[i]['lng']) + ')' +
              '" data-sentiment="' + tweetDataJS[i]['sentiment'] + 
              '" data-category="' + tweetDataJS[i]['category'] + 
              '" style="background:' + color + '"></div>'
          }).setMap(map); 
	  }

};



function getSentimentColor(sentiment){
	var color = b_default;
	if (sentiment == "negative") {
		color = r_negative;
	} else if (sentiment == "positive") {
		color = g_positive;
	} else if (sentiment == "neutral") {
		color = y_neutral
	}
	return color;
};


function getHeatMap(){
	flag = "heat";

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
		 deletePoints();
		 getHeatMap();
	 }
};

function deletePoints() {
    $('.tweet-point').remove();
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
  	var e = document.getElementById("category");
	var key = e.options[e.selectedIndex].value;
	
    $.getJSON('MainServlet',{
        category:key
    },  function(data) {
      // update local data;
      tweetDataJS = data;
      updateCounter(data);
     
      if (flag == "heat") {
        // update points in heatmap
        if (heatmap && typeof heatmap.setMap === 'function') {
          heatmap.setMap(null);
        }
        getHeatMap();
      } else if (flag == "points") {
        // update points in heatmap
        deletePoints();
        getPointsMap();
      }
     
     populateSideBar(data);
     
   });
}; 

function requestTrends() {
  	var e = document.getElementById("place");
	var key = e.options[e.selectedIndex].value;
    $.getJSON('TrendsServlet',{
        place:key
    },  function(data) {
    	document.getElementById('trends').innerHTML = data;
   });
};

function updateCounter(data) {
  document.getElementById("Counter").innerHTML = data.length;
};

function populateSideBar(data) {
  $('.side-bar').empty();
  for (var i = 0; i <= 10; i++) {
	  var tweet = data[i];
	  var parsedContent = wrapLinks(tweet['content']);
      $('.side-bar').append(
    	'<div class="user-tweet">' +
	    	'<div class="user-created">' + tweet['createdstr'] + ' · (' + Math.round(tweet['lat']) + ', ' +  Math.round(tweet['lng']) + ') </div>' +
	    	'<div class="user-name">' + tweet['username'] + '</div>' +
        	'<div class="user-content">' + parsedContent + '</div>' +
        	'<div class="user-sentiment ' + tweet['sentiment'] + '">' + tweet['sentiment'] +
        	' · ' + tweet['category'] + '</div>' +
       	'</div>'
      );
  }
};

function wrapLinks(content) {
	return content.replace(		
			/(?:(https?\:\/\/[^\s]+))/m,
			'<a target="_blank" href="$1">$1</a>');	
}

function addMarker(tweet){
  position = new google.maps.LatLng(
          tweet['lat'],
          tweet['lng']
      );
  sentiment = tweet['sentiment'];
  var color = getSentimentColor(sentiment);
  var marker = new RichMarker({
      position: position,
      map: map,
      shadow: 'none',
      content: '<div class="tweet-point animated bounce" data-user="' + tweet['username'] +
      '" data-content="' + tweet['content'].replace('"', '\"') + '" style="background:' + color + '"></div>'
  }).setMap(map); 
};

function addToSideBar(tweet) {
  var parsedContent = wrapLinks(tweet['content']);
     $('.side-bar').prepend(
   	 '<div class="user-tweet">' +
    	'<div class="user-created">' + tweet['createdstr'] +  '</div>' +
       	'<div class="user-created"> (' + tweet['lat'] + ', ' +  tweet['lng'] + ') </div>' +
    	'<div class="user-name">' + tweet['username'] + '</div>' +
       	'<div class="user-content">' + parsedContent + '</div>' +
       	'<div class="user-sentiment ' + tweet['sentiment'] + '">' + tweet['sentiment'] +
       	', ' + tweet['category'] + '</div>' +
      	'</div>'
     );
};

$('.side-bar-toggle').on('click', function() {
	$('.map-container').toggleClass('active');
	$('.side-bar-container').toggleClass('active');
	$('.side-bar-container .arrow').toggleClass('closed');
	google.maps.event.trigger(map, 'resize');
});

$('body').on('click', '.tweet-point', function() {
	var content = $(this).data('content');
	var user = $(this).data('user');
	var created = $(this).data('created');
	var geoloc = $(this).data('geoloc');
	var sentiment = $(this).data('sentiment');
	var category = $(this).data('category');
	$('.fade-bg, .lightbox').fadeIn();
	$('.lightbox .user').text(user + " · " + created + " · " + geoloc);
	$('.lightbox .tweet').html(wrapLinks(content) + "\n");
	$('.lightbox .user-sentiment').removeClass("positive negative neutral").addClass(sentiment).text(sentiment + " · " + category);
});

$('.fade-bg').click(function() {
	$(this).fadeOut();
	$('.lightbox').fadeOut();
});
    </script>
    <script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAYAptnpHlY5oYzYwUz_lPE3RyIrR5cJpU&signed_in=true&libraries=visualization&callback=loadRichMarker">
    </script>
  </body>
</html>
