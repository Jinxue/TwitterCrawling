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

import spammerwadgets.OAuthConfigXML;
import spammerwadgets.OAuthTwitter;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.7
 */

/*
 * We will record all tweets for a user (<3200 tweets)
 */
public class GetUserTimelineMulti {
	/**
	 * Usage: java twitter4j.examples.timeline.GetUserTimelineMulti
	 * 
	 * @param args
	 *            String FileAuth
	 *            String FileUserId
	 */
	
	PrintWriter out = null;
	PrintWriter outUserId = null;
	String userFile;
	ArrayList<Long> users;
	int numCrawl; 
	int currentCrawl;
	ArrayList<Twitter> twitters;
	Long sinceId;
	Long maxId;
	Date start;
	private long updateFreq = 100;
	
	GetUserTimelineMulti(String propFile, String access, 
			String userF, Long since, Long max){
		OAuthConfigXML config = new OAuthConfigXML(propFile, access);
		userFile = userF;
		this.sinceId = since;
		this.maxId = max;
		
		buildStartTime();
		
		buildTwitters(config);
		
		String[] splits = userF.split("\\.");
		buildOutFile(splits[0]);

		loadUsers();
	}

	GetUserTimelineMulti(String accessFile, 
			String userF, Long since, Long max){
		userFile = userF;
		this.sinceId = since;
		this.maxId = max;
		
		buildStartTime();
		
		buildTwitters(accessFile);
		
		String[] splits = userF.split(".");
		buildOutFile(splits[0]);

		loadUsers();
	}

	/*
	 * The access tokens for SampleTest application
	 */
	private Twitter getOAuthTwitter(String[] auth){
		
		if(auth.length != 4){
			System.out.println("The auth info is invalid!");
			System.exit(-1);
		}
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey(auth[0])
          .setOAuthConsumerSecret(auth[1])
          .setOAuthAccessToken(auth[2])
          .setOAuthAccessTokenSecret(auth[3]);
                
        return new TwitterFactory(cb.build()).getInstance();
	}

	private void buildTwitters(String accessFile) {
		// TODO Auto-generated method stub
		File inFile = new File(accessFile);

		if (!inFile.isFile()) {
			System.out.println("Parameter is not an existing file");
			return;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(userFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		currentCrawl = 0;
		twitters = new ArrayList<Twitter>();

		String line = null;

		//Read from the original file and write to the new
		//unless content matches data to be removed.
		try {
			while ((line = br.readLine()) != null){
				twitters.add(this.getOAuthTwitter(line.split("\\s+")));
			}
			br.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		numCrawl = twitters.size();
	}

	private void buildOutFile(String userId) {
		// TODO Auto-generated method stub
		Long rand = (new Date().getTime() - start.getTime()) / 1000;
		try {
			FileWriter outFile = new FileWriter("CrawlTweets-" + 
					userId + "-" + rand + ".txt", true);
			out = new PrintWriter(outFile);

			FileWriter outFileUserId = new FileWriter("CrawlIds-" + 
					userId + "-" + rand + ".txt", true);
			outUserId = new PrintWriter(outFileUserId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void buildTwitters(OAuthConfigXML config) {
		// TODO Auto-generated method stub
		currentCrawl = 0;
		numCrawl = config.getNumber();
		twitters = new ArrayList<Twitter>();
		for (int i = 0; i < numCrawl; i ++ ) {
			twitters.add(OAuthTwitter.getOAuthTwitter(config, 
					config.getScreenNumber()[i]));
			//twitters[i] = new TwitterFactory().getInstance();
		}
	}

	private void buildStartTime() {
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

	private void loadUsers() {
		// TODO Auto-generated method stub
		File inFile = new File(userFile);

		if (!inFile.isFile()) {
			System.out.println("Parameter is not an existing file");
			return;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(userFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		users = new ArrayList<Long>();

		String line = null;

		//Read from the original file and write to the new
		//unless content matches data to be removed.
		try {
			while ((line = br.readLine()) != null){
				if (line.isEmpty()){
					System.out.println("Empty line!");
					continue;
				}

				//users.add(Long.parseLong(line));
				// For the user file with ID,screenName format
				if (line.contains(","))
					users.add(Long.parseLong(line.split(",")[0]));
				else
					users.add(Long.parseLong(line.split("\\s+")[0]));
			}
			br.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (NumberFormatException ne){
			System.out.println("User error: " + line);
		}
		//users.add(627648100L);
		System.out.println("We will crawl " + users.size() + " users.");
	}

	public void showRateLimit(){
		for (Twitter twitter : this.twitters){
			try {
				System.out.println(twitter.showUser(twitter.getId()).getRateLimitStatus());
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	long totalCount = 0;
	public void doCrawl(){
		List<Status> statuses = null;
		int count = 0;
		int userIndex = 0;
		int userTotal = users.size();
		
		boolean overflow = false;
		for (Long usr : users) {
			//System.out.println("%" + usr);
			
			out.println("%" + usr);
			
			Paging paging = null;
			count = 0;
			//for (int i = 1; i < 21; i++) {
			for (int i = 1; i < 4; i++) {
				paging = new Paging(i, 200);
				//paging = new Paging(i, 200, sinceId, maxId);

				overflow = false;
				try {
					// statuses = twitter.getUserTimeline(user, paging);
					statuses = twitters.get(currentCrawl).getUserTimeline(usr, paging);
					currentCrawl ++;
					if(currentCrawl == this.numCrawl)
						currentCrawl = 0;
					
					/*Thread.currentThread();
					try {
						Thread.sleep(updateFreq);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
				} catch (TwitterException te) {
					te.printStackTrace();
					System.out.println("Failed to get timeline: "
							+ te.getMessage());
					//System.exit(-1);
				}
				if(statuses ==null){
					break;
				}
				if (statuses.isEmpty()){
					if(i > 16)
						overflow = true;
					break;
				}
				for (Status status : statuses) {
					
					int len = status.getUserMentionEntities().length;
					/*
					 * We only need the interactions
					 */
					if (len <= 0)
						continue;

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
					//record += "::" + cleanText(status.getText());
					// We just ignore the text content in this crawling
					record += "::-1";
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
					
					//System.out.println(record);
					out.println(record);
					count ++;
				}

				//count += statuses.size();
				
			}
			out.flush();
			
			totalCount += count;
			out.println("%" + usr + ", " + count + ", " + overflow);
			outUserId.println(usr + "," + count);
			
			//System.out.println("%" + usr + ", " + count + ", " + overflow);
			System.out.println(userIndex + "/" + userTotal + ", " 
			+ count); 
			//+ ", " + (overflow ? "overflow" : "not overflow"));
			userIndex ++;
			out.println("------------------------------------------");
		}
		System.out.println("Total status count is " + totalCount);
		outUserId.println("#" + totalCount);
		this.closeFile();
	}
	
	public void closeFile(){
		out.println("#" + totalCount);
		out.close();
		outUserId.close();
	}

	public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java twitter4j.examples.GetUserTimelineByFile properFile accessFile UserFile");
            System.exit(-1);
        }

		Long sinceID =
				//1L;
				//206082662035763202L;  // 2012/05/25
				//217681144844533761L; // 2012/06/26
				//238893053518151680L;	// 2012/08/24
				
				//263390611707662336L;  //2012/10/30 from edxOnline			// For LA
				//274617899430318080L;	// 2012/11/30 from gnip				// For NY-new
				//272017119149961217L;	// 2012/11/23 from gnip				// For SF-new
				
				//349919775218089984L;	// 2013/06/26 08:59:10 MST from @AndrewYNg  // For the 2M sampled users
				373579301611782144L;	//Fri Aug 30 15:53:40 MST 2013 from @lintool // for SF/tucson geo dataset
				
		Long maxID 	 = 
				//250409135600975873L;	// 2012/09/24
				//238893053518151680L;	// 2012/08/24
				// 227659323180974080L;	// 2012/07/24
				//217681144844533761L; // 2012/06/26
				
				
				//296794391572529152L;		// 2013/01/30  from edxOnline	// For LA
				//307288584120049665L;	// 2013/03/01 From techreview		// For NY-new
				//306145513219174402L;	// 2013/2/25, Gnip					// For SF-new
				
				//384087303649374208L;	// Sat Sep 28 15:48:43 MST 2013 from @azprofessor  // For the 2M sampled users
		
				407172842384400385L;	// Sun Dec 01 08:42:25 MST 2013 from @lixiaZ		// For SF/tucson geo dataset
				
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

		GetUserTimelineMulti userTL = new GetUserTimelineMulti(args[0], args[1], args[2], sinceID, maxID);
		userTL.doCrawl();
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}

	private String cleanText(String text) {
		// TODO Auto-generated method stub
		// Clear the carriage return
		String str = text.replaceAll("[\\r\\n]", " ");
		// str = text;
		return str;
	}

	private String getSource(String source) {
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
}
