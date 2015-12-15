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
public final class PrintFilterStreamGeo {
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
	
    public static void main(String[] args) throws TwitterException {
    	
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
            	/* my shutdown code here */
            	// Record the user status
            	idOut.println("Final status -------------------:");
            	for (Long id : discoveredUsers.keySet()){
            		idOut.println(id + "," + discoveredUsers.get(id));
            	}
            	idOut.close();
            }
         });    	
    	
        if (args.length < 5) {
            System.out.println("Usage: java twitter4j.examples.PrintFilterStreamGeo long1 lati1 long2 lati2 CITY");
            System.exit(-1);
        }
        if(args.length == 6){
        	// Preload the collected user IDs
        	preloadID(args[5]);
        }

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
            }

            private void CheckUser(Status status) {
				// TODO Auto-generated method stub
            	Long id =  status.getUser().getId();
            	/*String username = status.getUser().getScreenName();
            	String realname = status.getUser().getName();
            	String text = status.getText();
            	Date date = status.getCreatedAt();*/
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
			FileWriter outFile = new FileWriter("discoveredUser" + args[4] + ".txt", true);
			idOut = new PrintWriter(outFile);
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
        //TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		TwitterStream twitterStream = getOAuthTwitterStream();
        twitterStream.addListener(listener);
        /*ArrayList<Long> follow = new ArrayList<Long>();
        ArrayList<String> track = new ArrayList<String>();

        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);*/

        // Geographic location
        double[][] location = new double[2][2];
        //location[0] = new double[4];
        //location[1] = new double[4];
        location[0][0] = Double.parseDouble(args[0]);
        location[0][1] = Double.parseDouble(args[1]);
        location[1][0] = Double.parseDouble(args[2]);
        location[1][1] = Double.parseDouble(args[3]);
        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        //twitterStream.filter(new FilterQuery(0, followArray, trackArray, location));
        FilterQuery query = new FilterQuery();
        query.locations(location);
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
    	
		String line = id + "::" + username + "::" + realname + "::" + text + "::" + date + "::" + (new Date());
		//System.out.println(line);
		idOut.println(line);
		idOut.flush();
		System.out.print("\r" + ++count);
		
	}
}
