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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class PrintSampleStream {
    /**
     * Main entry of this application.
     *
     * @param args
     */
	static Hashtable<Long, Long> discoveredUsers = new Hashtable<Long, Long>(); 
	static PrintWriter idOut = null;
	static int count = 0;
	static int tweetsCount = 0;
	static String fileName = "twitter4j.properties";
    static Properties prop = new Properties();

    public static void main(String[] args) throws TwitterException {
        //TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    	
    	
		TwitterStream twitterStream = getOAuthTwitterStream();

		try {
			FileWriter outFile = new FileWriter("sampledUsers" + ".txt", true);
			idOut = new PrintWriter(outFile);
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
				++ tweetsCount;
                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
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

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
    }
    
	private static TwitterStream getOAuthTwitterStream(){
		
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
		System.out.print("\r" + ++count + ", " + tweetsCount);
	}

}
