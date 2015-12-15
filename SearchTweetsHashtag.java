/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package crawling;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class SearchTweetsHashtag {
    /**
     * Usage: java twitter4j.examples.search.SearchTweets [query]
     *
     * @param args
     */
	
	static Hashtable<Long, Long> discoveredTweets = new Hashtable<Long, Long>(); 
	static PrintWriter out = null;
	static int count = 0;
	static int[] avgUsers;
	static int histCount = 100;
	static int currentIndex = 0;
	static String hashtag;
    static Date start;

	static String fileName = "twitter4j.properties";
    static Properties prop = new Properties();

    
    public static void main(String[] args) {
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
            	/* my shutdown code here */
            	// Record the user status
            	out.println("Final status -------------------:");
            	for (Long id : discoveredTweets.keySet()){
            		out.println(id + "," + discoveredTweets.get(id));
            	}
            	out.close();
            	
        		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        		Date date = new Date();
        		date = new Date();
        		System.out.println("End at: " + dateFormat.format(date));
            }
         });    	
    	
        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.PrintFilterStreamHashtag hashtag");
            System.exit(-1);
        }
        if(args.length == 2){
        	// Preload the collected user IDs
        	//preloadID(args[1]);
        }
   	
        buildStartTime();

        File file = new File(fileName);
        InputStream is = null;

        try {
            if (file.exists()) {
                is = new FileInputStream(file);
                prop.load(is);
            }
            else{
            	System.out.println(fileName + " doesn't exist!");
            	System.exit(-1);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        } 

        try {
			FileWriter outFile = new FileWriter(args[0] + ".txt", true);
			out = new PrintWriter(outFile);
			hashtag = args[0];
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		avgUsers = new int[histCount];
		
    	Query query = new Query();
    	query.setQuery(hashtag);
    	
    	//query.setCount(100);
    	query.since("2015-03-15");
    	//query.setPage(20);
    	
        //Twitter twitter = new TwitterFactory().getInstance();
    	Twitter twitter = getOAuthTwitter();
        
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		//System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

        while (true){
        //for (int i = 1; i <= 15; i ++){
        //	query.setPage(i);
        	try {
				doASearch(twitter, query);
			} catch (TwitterException te) {
				// TODO Auto-generated catch block
				te.printStackTrace();
				System.out.println("Failed to search tweets: " + te.getMessage());

				// back-off
				Thread.currentThread();
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
        	Thread.currentThread();
        	try {
        		Thread.sleep(5 * 1000);
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }
    }
    
    private static void doASearch(Twitter twitter, Query query) throws TwitterException{
        //try {
            QueryResult result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            int thisCount = 0;
            for (Status tweet : tweets) {

            	// Check the tweet
            	Long id =  tweet.getId();

            	if(discoveredTweets.containsKey(id)){
					//System.out.println("Already found this user: " + id);
					long num = discoveredTweets.get(id);
					discoveredTweets.put(id, num + 1);
				}else{
					discoveredTweets.put(id, (long) 1);
					storeATweet(tweet);
					thisCount ++;
				}
			}
            if(currentIndex < histCount){
            	avgUsers[currentIndex] = thisCount;
            	currentIndex ++;
            }else{
            	currentIndex = 0;
            }
            
            /* Calculate the average #users in last 10 times */
            double sum = 0;
            for (int num : avgUsers)
            	sum += num;
            System.out.print("\r" + count+ ", the average number of tweets in last " 
            	+ histCount + " is: " + sum / histCount);
            
            
        //} catch (TwitterException te) {
        //    te.printStackTrace();
        //    System.out.println("Failed to search tweets: " + te.getMessage());
        //    System.exit(-1);
        //}
    	
    }
    
	private static void storeATweet(Status status){
		int len = status.getUserMentionEntities().length;
		/*
		 * We only need the interactions
		 */
		if (len <= 0)
			return;

		String record = "";
		record += status.getId();
		record += "::" + status.getInReplyToStatusId();
		record += "::" + status.getInReplyToUserId();
		record += "::" + status.getRetweetCount();
		if (status.getRetweetedStatus() != null)
			record += "::" + status.getRetweetedStatus().getId();
		else
			record += "::" + "-1";
		//record += "::" + status.isRetweet();
		//int len = status.getUserMentionEntities().length;
		if (len > 0) {
			record += "::";
			for (int l = 0; l < len; l++) {
				UserMentionEntity ent = status
						.getUserMentionEntities()[l];
				record += "," + ent.getId();
			}
		} else{
			record += "::" + "-1";
		}
		len = status.getURLEntities().length;
		if (len > 0) {
			record += "::";
			for (int l = 0; l < len; l++) {
				URLEntity ent = status.getURLEntities()[l];
				record += "," + ent.getURL() + "|"
						//+ ent.getDisplayURL() + "|"
						+ ent.getExpandedURL();
			}
		} else
			record += "::" + "-1";
		record += "::" + cleanText(status.getText());
		// We just ignore the text content in this crawling
		//record += "::-1";
		record += "::" + 
				// status.getCreatedAt();
				 (status.getCreatedAt().getTime() - start
						.getTime()) / 1000;

		record += "::" + getSource(status.getSource());
		
		// Geo Location
		if (status.getGeoLocation() != null)
			record += "::" + status.getGeoLocation();
		else
			record += "::" + "-1";
		
		record += "::" + status.getUser().getId() + 
				"::" + status.getUser().getScreenName() +
				"::" + status.getUser().getName() +
				"::" + status.getCreatedAt();
    	
		//System.out.println(record);
		out.println(record);
		out.flush();
		count ++;
	}

	private static void buildStartTime() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy, MM, dd");
		start = null;
		try {
			start = sdf.parse("2000, 1, 1");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static String getSource(String source) {
		// TODO Auto-generated method stub
		//String src = "web";
		//if (source.equals("web"))
		//	return src;
		if(source.contains(">")){
			String[] splits = source.split(">");
			splits = splits[1].split("<");
			return splits[0];
		}else {
			return source;
		}
	}
	
	private static String cleanText(String text) {
		// TODO Auto-generated method stub
		// Clear the carriage return
		String str = text.replaceAll("[\\r\\n]", " ");
		// str = text;
		return str;
	}



	/*
	 * The access tokens for SampleTest application
	 */
	private static Twitter getOAuthTwitter(){
		
        ConfigurationBuilder cb = new ConfigurationBuilder();
        /*cb.setDebugEnabled(true)
          .setOAuthConsumerKey("Oo1Np62vR41MJTeVViKMmA")
          .setOAuthConsumerSecret("9e2ad6usWLJikjoI4yNuDQ85rrf9eRvfLmUDRBPtZ8")
          .setOAuthAccessToken("701461304-mbDoQtYqtSG9Q2RHLlSBUFEgRWbKE0CpViYowNXs")
          .setOAuthAccessTokenSecret("aWeao9VabuGfphGcH83SozAwI0hgmlmg3ptS5KGGVs");
         */
        cb.setDebugEnabled(true)
        .setOAuthConsumerKey(prop.getProperty("oauth.consumerKey"))
        .setOAuthConsumerSecret(prop.getProperty("oauth.consumerSecret"))
        .setOAuthAccessToken(prop.getProperty("oauth.accessToken"))
        .setOAuthAccessTokenSecret(prop.getProperty("oauth.accessTokenSecret"));

        return new TwitterFactory(cb.build()).getInstance();
	}
}
