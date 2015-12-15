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

import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */
public class FoundUsersBySearchHashtag {
    /**
     * Usage: java twitter4j.examples.search.SearchTweets [query]
     *
     * @param args
     */
	
	static Hashtable<Long, Long> discoveredUsers = new Hashtable<Long, Long>(); 
	static PrintWriter out = null;
	static int count = 0;
	static int[] avgUsers;
	static int histCount = 100;
	static int currentIndex = 0;
	static String hashtag;

    public static void main(String[] args) {
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
            	/* my shutdown code here */
            	// Record the user status
            	out.println("Final status -------------------:");
            	for (Long id : discoveredUsers.keySet()){
            		out.println(id + "," + discoveredUsers.get(id));
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
        	preloadID(args[1]);
        }
   	
		try {
			FileWriter outFile = new FileWriter("discoveredUser" + args[0] + ".txt", true);
			out = new PrintWriter(outFile);
			hashtag = args[0];
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		avgUsers = new int[histCount];
		
    	Query query = new Query();
    	//query.setGeoCode(new GeoLocation(Double.parseDouble(args[0])
    	//		, Double.parseDouble(args[1])), Double.parseDouble(args[2]), args[3]);
    	query.setQuery(hashtag);
    	// New York Metropolitan
    	//query.setGeoCode(new GeoLocation(40.7, -74), 100, "km");
    	// Bay Area MetroPolitan
    	//query.setGeoCode(new GeoLocation(37.78, -122.4), 100, "km");
    	// LA MetroPolitan
    	//query.setGeoCode(new GeoLocation(34.05, -118.24), 200, "km");
    	
    	query.setCount(100);
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
                //System.out.println("@" + tweet.getFromUser() + tweet.getId() + " - " + tweet.getText());

            	// Check the tweet
            	Long id =  tweet.getUser().getId();
            	String screenName = tweet.getUser().getScreenName();
            	
            	/*String location = "";
            	if (tweet.getUser().getLocation() != null){
            		location += tweet.getUser().getLocation();
            	if (tweet.getGeoLocation() != null){
            		location += "::" + tweet.getGeoLocation().toString();
            	}*/
            	String location = tweet.getUser().getLocation() + "::" + tweet.getGeoLocation();
            	// If the tweet is a retweet, the source of the tweet is from the target area
            	/*if (tweet.isRetweet()){
            		id = tweet.getRetweetedStatus().getUser().getId();
            		screenName = tweet.getRetweetedStatus().getUser().getScreenName();
            		//location = tweet.getRetweetedStatus().getUser().getLocation();
            		location = tweet.getRetweetedStatus().getUser().getLocation() + "::" + tweet.getRetweetedStatus().getGeoLocation();
            	}*/

            	if(discoveredUsers.containsKey(id)){
					//System.out.println("Already found this user: " + id);
					long num = discoveredUsers.get(id);
					discoveredUsers.put(id, num + 1);
				}else{
					discoveredUsers.put(id, (long) 1);
					storeUserID(id, screenName, location);
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
            System.out.print("\r" + count+ ", the average number of users in last " 
            	+ histCount + " is: " + sum / histCount);
            
            
        //} catch (TwitterException te) {
        //    te.printStackTrace();
        //    System.out.println("Failed to search tweets: " + te.getMessage());
        //    System.exit(-1);
        //}
    	
    }
    
	private static void preloadID(String idFile) {
		// TODO Auto-generated method stub
    	FileReader fr = null;
		try {
			fr = new FileReader(idFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader br = new BufferedReader(fr);
		String id = null;
		do {
			try {
				id = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (null == id)
				break;

			// Pass the text line
			if(id.startsWith("Final"))
				continue;
			
			//String[] comb = user.split("\\|");
			String[] comb = id.split(",");
			if(comb.length == 1)
				discoveredUsers.put(Long.parseLong(comb[0]), (long) 1);
			else if(comb.length == 2)
				discoveredUsers.put(Long.parseLong(comb[0]), Long.parseLong(comb[1]));
		} while(null != id);
		count += discoveredUsers.size();
	}

	private static void storeUserID(Long id, String screenName, String location){
		//FileWriter outFile = new FileWriter(tweetFile, true);
		//PrintWriter out = new PrintWriter(outFile);
		out.println(id + "::" + screenName + "::" + location);
		out.flush();
		count ++;
		//System.out.print("\r" + ++count);
		//System.out.print(++count);
		//out.close();
	}

	/*
	 * The access tokens for SampleTest application
	 */
	private static Twitter getOAuthTwitter(){
		
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("Oo1Np62vR41MJTeVViKMmA")
          .setOAuthConsumerSecret("9e2ad6usWLJikjoI4yNuDQ85rrf9eRvfLmUDRBPtZ8")
          .setOAuthAccessToken("701461304-mbDoQtYqtSG9Q2RHLlSBUFEgRWbKE0CpViYowNXs")
          .setOAuthAccessTokenSecret("aWeao9VabuGfphGcH83SozAwI0hgmlmg3ptS5KGGVs");

        /*cb.setDebugEnabled(true)
        .setOAuthConsumerKey(prop.getProperty("oauth.consumerKey"))
        .setOAuthConsumerSecret(prop.getProperty("oauth.consumerSecret"))
        .setOAuthAccessToken(prop.getProperty("oauth.accessToken"))
        .setOAuthAccessTokenSecret(prop.getProperty("oauth.accessTokenSecret"));*/

        return new TwitterFactory(cb.build()).getInstance();
	}
}
