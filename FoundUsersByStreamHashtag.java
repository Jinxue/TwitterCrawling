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

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

/**
 * <p>This is a code example of Twitter4J Streaming API - filter method support.<br>
 * Usage: java twitter4j.examples.stream.PrintFilterStream [follow(comma separated numerical user ids)] [track(comma separated filter terms)]<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class FoundUsersByStreamHashtag {
    /**
     * Main entry of this application.
     *
     * @param args follow(comma separated user ids) track(comma separated filter terms)
     * @throws twitter4j.TwitterException
     */
	
	static Hashtable<Long, Long> discoveredUsers = new Hashtable<Long, Long>(); 
	static PrintWriter idOut = null;
	static int count = 0;
	
	static String fileName = "twitter4j.properties";
    static Properties prop = new Properties();
    
    static Date start;
    static int tweets = 0;
	
    public static void main(String[] args) throws TwitterException {
    	
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
            	// my shutdown code here
            	// Record the user status
            	idOut.println("Final status -------------------:");
            	for (Long id : discoveredUsers.keySet()){
            		idOut.println(id + "," + discoveredUsers.get(id));
            	}
            	idOut.close();
            }
         });
    	
        if (args.length != 1) {
            System.out.println("Usage: java twitter4j.examples.PrintFilterStreamHashtag hashtag");
            System.exit(-1);
        }
        /*if(args.length == 2){
        	// Preload the collected user IDs
        	preloadID(args[1]);
        }*/

        //buildStartTime();
        
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
      
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            	CheckUser(status);
            	
            	/*storeATweet(status);
            	tweets += 1;
            	if (tweets % 1000 == 0){
            		System.out.println("We now have tweets: " + tweets);
            	}*/
            }

            private void CheckUser(Status status) {
				// TODO Auto-generated method stub
            	Long id =  status.getUser().getId();
            	String username = status.getUser().getScreenName();
            	String realname = status.getUser().getName();
            	String text = status.getText();
            	Date date = status.getCreatedAt();
				if(discoveredUsers.containsKey(id)){
					//System.out.println("Already found this user: " + id);
					long num = discoveredUsers.get(id);
					discoveredUsers.put(id, num + 1);
				}else{
					discoveredUsers.put(id, (long) 1);
					storeUserID(status);
				}
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}
        };
        
		try {
			FileWriter outFile = new FileWriter("discoveredUser" + args[0] + ".txt", true);
			idOut = new PrintWriter(outFile);
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        //TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		TwitterStream twitterStream = getOAuthTwitterStream();
        twitterStream.addListener(listener);

        FilterQuery query = new FilterQuery();
        String[] track = {args[0]};
        //String[] track = {"#BaltimoreRiots"};
        query.track(track);
        twitterStream.filter(query);
    }

	/*
	 * The access tokens for SampleTest application
	 */
	private static TwitterStream getOAuthTwitterStream(){
		
        ConfigurationBuilder cb = new ConfigurationBuilder();
        /*cb.setDebugEnabled(true)
          .setOAuthConsumerKey("Oo1Np62vR41MJTeVViKMmA")
          .setOAuthConsumerSecret("9e2ad6usWLJikjoI4yNuDQ85rrf9eRvfLmUDRBPtZ8")
          .setOAuthAccessToken("700944906-6zdtjgw0Tskn90eDTB36sx1RCOaTff9C2zt1QYa8")
          .setOAuthAccessTokenSecret("x56tcXGBdQ2TZOs4Ci69Rm2QiUJMgjKVQTn1W7uM");*/

        cb.setDebugEnabled(true)
        .setOAuthConsumerKey(prop.getProperty("oauth.consumerKey"))
        .setOAuthConsumerSecret(prop.getProperty("oauth.consumerSecret"))
        .setOAuthAccessToken(prop.getProperty("oauth.accessToken"))
        .setOAuthAccessTokenSecret(prop.getProperty("oauth.accessTokenSecret"));

        return new TwitterStreamFactory(cb.build()).getInstance();
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

	private static void storeUserID(Long id){
		//FileWriter outFile = new FileWriter(tweetFile, true);
		//PrintWriter out = new PrintWriter(outFile);
		idOut.println(id);
		idOut.flush();
		System.out.print("\r" + ++count);
		//System.out.print(++count);
		//out.close();
	}

	private static void storeUserID(Status status) {
		// TODO Auto-generated method stub
		Long id =  status.getUser().getId();
		String username = status.getUser().getScreenName();
    	String realname = status.getUser().getName();
    	String text = status.getText();
    	Date date = status.getCreatedAt();
    	
		//String line = id + "::" + username + "::" + realname + "::" + text + "::" + date + "::" + (new Date());
    	String line = id + "," + username;
		//System.out.println(line);
		idOut.println(line);
		idOut.flush();
		System.out.print("\r" + ++count);
		
	}
	
	/*private static void storeATweet(Status status){
		int len = status.getUserMentionEntities().length;
		
		//We only need the interactions
		
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
	}*/


}
