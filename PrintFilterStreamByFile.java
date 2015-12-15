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
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * <p>This is a code example of Twitter4J Streaming API - filter method support.<br>
 * Usage: java twitter4j.examples.stream.PrintFilterStream [follow(comma separated numerical user ids)] [track(comma separated filter terms)]<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class PrintFilterStreamByFile {
    /**
     * Main entry of this application.
     *
     * @param args follow(comma separated user ids) track(comma separated filter terms)
     * @throws twitter4j.TwitterException
     */
	
	static String idFile;
	static String tweetFile;
	static PrintWriter out = null;
	static ArrayList<Long> follow = new ArrayList<Long>();
	static ArrayList<String> track = new ArrayList<String>();
	static int count = 0;
	
    public static void main(String[] args) throws TwitterException {
        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.PrintFilterStreamByFile ID_File ");
            System.exit(-1);
        }

        idFile = args[0];
        tweetFile = idFile.split("\\.")[0] + "-tweets.txt";
        
        
		try {
			FileWriter outFile = new FileWriter(tweetFile, true);
			out = new PrintWriter(outFile);
			//out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                //System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                storeTweet(status.getUser().getId() + "::" + status.getText());
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

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        
    	// String users = "700896487,701053075,636744543";
    	// String[] split = users.split(",");

        
        buildFollowList();
        
        long[] followArray = new long[follow.size()];
        for (int i = 0; i < follow.size(); i++) {
            followArray[i] = follow.get(i);
        }
        String[] trackArray = track.toArray(new String[track.size()]);

        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        twitterStream.filter(new FilterQuery(0, followArray, trackArray));
    }

    private static void buildFollowList(){
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
			if(null == id)
				break;
			
			follow.add(Long.parseLong(id));
		}
		while (true);
    }
    
	private static void storeTweet(String tweet){
		//FileWriter outFile = new FileWriter(tweetFile, true);
		//PrintWriter out = new PrintWriter(outFile);
		out.println(tweet);
		out.flush();
		System.out.print("\r" + ++count);
		//System.out.print(++count);
		//out.close();
	}
}
