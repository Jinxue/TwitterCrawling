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

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

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
public class GetUserTimelineByFileDiff {
	/**
	 * Usage: java twitter4j.examples.timeline.GetUserTimeline
	 * 
	 * @param args
	 *            String[]
	 */
	static PrintWriter outWhole = null;
	static PrintWriter outInter = null;
	static String userFile;

	public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java twitter4j.examples.GetUserTimelineByFile UserFile");
            System.exit(-1);
        }
        userFile = args[0];

		try {
			FileWriter outFileWhole = new FileWriter("CrawlTweets" + ".txt", true);
			outWhole = new PrintWriter(outFileWhole);

			FileWriter outFileInter = new FileWriter("CrawlInter" + ".txt", true);
			outInter = new PrintWriter(outFileInter);
			// out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		// gets Twitter instance with default credentials
		Twitter twitter = new TwitterFactory().getInstance();
		int count = 0;
		int countInter = 0;
		int totalCount = 0;
		int totalCountInter = 0;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy, MM, dd");
		Date start = null;
		try {
			start = sdf.parse("2000, 1, 1");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<Status> statuses = null;
		// String user = "Porter_Anderson";
		// user = "pqtad";
		// user = "paradunaa6";
		// user = "palifarous";
		// statuses = twitter.getUserTimeline(user);
		//String usersS = "490495670,210462629,731570426,174646182,381163846,308812526,595154838,47356666,590795627,466854690,560386951,406747522,767819150,193105498,725874775,381257304,486727765,39469575,68239634,187739283,27048882,172979072,16075589,618047540,841958365,707631710,76274554,334451278,573978436,782711790,498017142,764236789,88185343,293950091,726606902,221410697,566098451,32987449,73497385,223182159,571182929,769441794,16225570,404748449,249953519,713693096,727692930,178473099,366957548,255171527,411892240,370333418,753509292,765928471,49389074,422867487,185298515,112841821,524731824,132207254,431289858,35602356,747638244,76398937,276388822,221106768,608597994,532423674,509070257,235773945,263995811,61575477,48606725,518136068,450781132,631504150,842236698,840479664,306291201,59829276,541212815,264436593,262466303,302157661,135587748,399229428,265344815,225280446,540254332,218019401,260453139,187424025,177051847,351065900,406684867,293234971,219356963,280763734,59290083,214750688";

		//String[] splits = usersS.split(",");
		ArrayList<Long> users = new ArrayList<Long>();

		String line = null;

		//Read from the original file and write to the new
		//unless content matches data to be removed.
		try {
			while ((line = br.readLine()) != null) {
				
				users.add(Long.parseLong(line));
			}
			br.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//users.add(584928891L);
		//users.add(700425265L);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");

		System.out.println("Start at: " + dateFormat.format(date));

		Long sinceID = 
				//218903304682471424L; // 2012/06/29
				238893053518151680L;	// 2012/08/24
		Long maxID 	 = 
				250409135600975873L;	// 2012/09/24
				//238893053518151680L;	// 2012/08/24
				// 227659323180974080L;	// 2012/07/24
		
		boolean overflow = false;
		for (Long usr : users) {
			System.out.println("%" + usr);
			outWhole.println("%" + usr);
			outInter.println("%" + usr);
			Paging paging = null;
			count = 0;
			countInter = 0;
			for (int i = 1; i < 21; i++) {
				//paging = new Paging(i, 200);
				paging = new Paging(i, 200, sinceID, maxID);

				overflow = false;
				try {
					// statuses = twitter.getUserTimeline(user, paging);
					statuses = twitter.getUserTimeline(usr, paging);
					Thread.currentThread();
					try {
						Thread.sleep(10500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (TwitterException te) {
					te.printStackTrace();
					System.out.println("Failed to get timeline: "
							+ te.getMessage());
					//System.exit(-1);
				}
				if (statuses.isEmpty()){
					if(i > 16)
						overflow = true;
					break;
				}

				for (Status status : statuses) {
					boolean inter = false;
					//countRT += status.getRetweetCount();
					String record = "";
					String recordInter = "";
					record += status.getId();
					
					// For reply
					Long replyId = status.getInReplyToStatusId();
					if( replyId == -1)
						record += "::-1::-1";
					else{
						record += "::" + replyId;
						record += "::" + status.getInReplyToUserId();
						inter = true;
					}
					
					// For retweeting
					Long retweets = (long) status.getRetweetCount();
					record += "::" + retweets;
					if( retweets > 0){
						inter = true;
					
						if (status.getRetweetedStatus() != null)
							record += "::" + status.getRetweetedStatus().getId();
						else
							record += "::" + "-1"; // The source of retweeting
					}
					//record += "::" + status.isRetweet();
					// For mentions
					int len = status.getUserMentionEntities().length;
					if (len > 0) {
						record += "::";
						for (int l = 0; l < len; l++) {
							UserMentionEntity ent = status
									.getUserMentionEntities()[l];
							record += "," + ent.getId();
						}
						inter = true;
					} else
						record += "::" + "-1";
					
					recordInter += record;
					
					// For URL
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
					
					// For text
					record += "::" + cleanText(status.getText());
					
					// For creating time
					// record += "::" + status.getCreatedAt().toString();
					Long seconds =  
							// status.getCreatedAt();
							 (status.getCreatedAt().getTime() - start
									.getTime()) / 1000;
					record += "::" + seconds;
					recordInter += "::" + seconds;

					// For publishing source
					record += "::" + getSource(status.getSource());
					//System.out.println(record);
					outWhole.println(record);
					
					if(inter){
						outInter.println(recordInter);
						countInter ++;
					}
				}

				count += statuses.size();
				outWhole.flush();
				outInter.flush();
			}
			totalCount += count;
			totalCountInter += countInter;
			outWhole.println("%" + usr + "," + count + "," + countInter + "," + overflow);
			outInter.println("%" + usr + "," + count + "," + countInter + "," + overflow);
			System.out.println("%" + usr + "," + count + "," + countInter + "," + overflow);
			outWhole.println("------------------------------------------");
			outInter.println("------------------------------------------");
		}
		System.out.println("Total status count is " + totalCount + "," + totalCountInter);
		outWhole.println("#" + totalCount + "," + totalCountInter);
		outInter.println("#" + totalCount + "," + totalCountInter);
		outWhole.close();
		outInter.close();
		date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");

		System.out.println("End at: " + dateFormat.format(date));
	}

	private static String cleanText(String text) {
		// TODO Auto-generated method stub
		// Clear the carriage return
		String str = text.replaceAll("[\\r\\n]", " ");
		// str = text;
		return str;
	}

	private static String getSource(String source) {
		// TODO Auto-generated method stub
		String src = "web";
		if (source.equals("web"))
			return src;
		else {
			String[] splits = source.split(">");
			splits = splits[1].split("<");
			return splits[0];
		}

	}
}
