# TweetMapWeb


## How to run this project locally:
1. Clone [tweetMapWeb](https://github.com/MengyuWu/tweetMapWeb) and [tweetBasic](https://github.com/MengyuWu/tweetBasic).
2. To run the program, you need AwsCredentials. Use your own, or contact the project owners for one. Place the `AwsCredentials.properties` file in `src/` of both projects.
3. Import projects to Eclipse.
4. Resolve all library dependencies by-right clicking the each project / Properties / Java Build Path. Delete all libraries with the wrong file path and re-link your own if needed.
4. Install Web Platform Tools in Eclipse
  a. In Eclipse, go to Help, select 'Install New Software'.
  b. Choose "Web Tools- http://download.eclipse.org/webtools/repository/mars > Web Tools Platform SDK (WTP SDK) 3.7.1.
5. Install TomCat 7.0 on Eclipse IDE
  a. In Eclipse, go to Help, select 'Install New Software'
  b. Choose "Mars- http://download.eclipse.org/releases/mars". If you're using a different version of Eclipse like Kepler or Luna, change the end of the URL accordingly.
  c. Expand “Web, XML, and Java EE Development” section.
  d. Check JST Server Adapters and JST Server Adapters Extensions.
  e. Once installed, in Eclipse, go to Window / Preferences / Server / Runtime Environments
  f. Press Add button, select Apache / Apache Tomcat v7.0.
  g. Press Next, select location on the drive for Tomcat installation directory. Make sure that the directory exist.
  h. Then press 'Download and Install' button, accept terms and point to your installation directory and press OK button.
  j. Press Finish after it finishes downloading.
  k. Apache Tomcat v7.0 will show in the list under Server Runtime Environments now.
6. Check that Project Facts / 1.7 and change if necessary.
7. Go to tweetMapWeb / Properties / Build Path / Add Server Runtime > Apache Tomcat v7.0
8. Go to tweetBasic / AWSResourceSetup.java and change S3_BUCKET_NAME, DYNAMODB_TABLE_NAME to your desired bucket and table names.
9. Go to tweetBasic / Tweet.java and change DYNAMODB_TABLE_NAME to the one you used in the previous step.
10. Go to [AlchemyAPI](http://www.alchemyapi.com/api/register.html) to get an API key. Then add api_key.txt containing your secret key to the root directory of tweetBasic.
10. Run tweetBasic / `AWSResourceSetup.java` as a Java project to set up your resources.
11. Run tweetBasic / `Tweet.java` as Java project to set up tweet model.
12. Run tweetBasic / `GetTweet.java` as a Java project to get tweets from the Twitter Stream API. Let it run for a while to get more Twitter data. Note that Alchemy API only allows 1,000 requests per day, and we do not store tweets that are missing geolocation details or are written in unsupported languages by Alchemy API. Under these restrictions, we can only get about 350 Tweets per day on average.
13. Run tweetMapWeb / `realtime.jsp` and right click Run as / Run on server. Define a new Tomcat 7 web server if necessary.
14. Now you should be able to visit the link locally on your browser too.

## How to deploy to Elastic Beanstalk and use Elastic LoadBalancing
### Using AWS Toolkit in Eclipse
To create the EB instance, right click `realtime.jsp` and run on server then choose to manually define new server.
Inline image 1
Inline image 2
Inline image 3
To run on server, select this. It will set it up for you and lead you to the url, which actually works and can be accessed anywhere woot.
Inline image 4
To terminate and avoid getting charged:
### Using EB CLI in Terminal
```
$ eb init

region: 1) US West (N. Virginia)

applicate name: tweetMap

Select a platform.

6) Tomcat

Select a platform version. # Select Option 2.

1) Tomcat 8 Java 8

2) Tomcat 7 Java 7

3) Tomcat 7 Java 6

Do you want to set up SSH for your instances? y

Select a keypair. 1) # Whichever pem you prefer

$ eb create # to create env and run the first time

default

default

$ eb open # when AWS website shows your EB instance is ready

$ eb health # to check on your status

$ eb deploy # to update when you have newly committed changes

$ eb terminate # to destroy instance
```

