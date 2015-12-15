package crawling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import spammerwadgets.OAuthConfigXML;
import spammerwadgets.OAuthTwitter;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/*
 * Download all the friends that a user has followed.
 * @author Jinxue Zhang
 */
public class GetFollowingMulti {
	PrintWriter outFriendsId = null;
	PrintWriter outMetaInfo = null;
	String userFile;
	ArrayList<Long> users;
	int numCheck; 
	int currentCheck;
	ArrayList<Twitter> twitters;
	private long updateFreq = 100;
	
	int friendUpper = 10000;

	GetFollowingMulti(String propFile, String access, 
			String userF){
		OAuthConfigXML config = new OAuthConfigXML(propFile, access);
		userFile = userF;

		buildTwitters(config);
		
		String[] splits = userF.split("\\.");
		buildOutFile(splits[0]);

		loadUsers();
	}

	private void buildOutFile(String userId) {
		// TODO Auto-generated method stub
		try {
			FileWriter outFile = new FileWriter("Friends-" + 
					userId + ".txt", true);
			FileWriter outFile2 = new FileWriter("Friends-stat-" + 
					userId + ".txt", true);
			outFriendsId = new PrintWriter(outFile);
			outMetaInfo = new PrintWriter(outFile2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void buildTwitters(OAuthConfigXML config) {
		// TODO Auto-generated method stub
		currentCheck = 0;
		numCheck = config.getNumber();
		twitters = new ArrayList<Twitter>();
		for (int i = 0; i < numCheck; i ++ ) {
			twitters.add(OAuthTwitter.getOAuthTwitter(config, 
					config.getScreenNumber()[i]));
			//twitters[i] = new TwitterFactory().getInstance();
		}
		Collections.shuffle(twitters);
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
				//users.add(Long.parseLong);
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
		}
		//users.add(627648100L);
		System.out.println("We will check " + users.size() + " users.");
	}

	/*
	 * Get the next available Twitter 
	 */
	private Twitter nextTwitter(){
		currentCheck ++;
		if(currentCheck == this.numCheck)
			currentCheck = 0;
	
		Thread.currentThread();
		try {
			Thread.sleep(updateFreq);
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}

		return twitters.get(currentCheck);
	}

	void doGet(boolean limit){
		int userIndex = 0;
		int userTotal = users.size();
		long cursor = -1;
		
		IDs ids = null;
		int friends = 0, rounds = 0, followers = 0, choose = 0, number = 0;
		String loc = null;
		
		for (Long usr : users) {
			friends = 0;
			followers = 0;
			rounds = 0;
			choose = 0;
			cursor = -1;
			number = 0;
			ids = null;
			loc = null;
			
			
			Twitter twitter;
			if (limit)
				try {
					twitter = nextTwitter();
					User tuser = twitter.showUser(usr);
					friends = tuser.getFriendsCount();
					followers = tuser.getFollowersCount();
					loc = tuser.getLocation();
				} catch (TwitterException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			/**
			 * If the number of followings is larger than 10000, we deal with it later
			 */
			if ((limit == false) || ((limit == true) && (friends <= friendUpper))){
				choose = 1;
				do {
					//ids = this.getFriends(twitters.get(currentCheck), usr);
					twitter = nextTwitter();
					try {
						ids = twitter.getFriendsIDs(usr, cursor);
		        		for (long id : ids.getIDs()) {
		        			outFriendsId.println(id);
		        		}
		            	number += ids.getIDs().length;
		            	rounds ++;
					} catch (TwitterException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						
						// If the user has been suspended
						break;
					}
				/*if (rounds > 6)
	            		break;*/
				}while ((cursor = ids.getNextCursor()) != 0);
				outFriendsId.println("%" + usr + ", " + number + ", " + rounds);
			}
			if (limit)
				System.out.println(userIndex + "/" + userTotal + ", " + friends + "," + followers + ", " + rounds + ", " + choose);
			else
				System.out.println(userIndex + "/" + userTotal + ", " + number + ", " + rounds); 
			outMetaInfo.println(usr + "," + friends + "," + followers + "," + rounds + ", " + choose + "::" + loc);
			userIndex ++;
		}
		outFriendsId.close();
		outMetaInfo.close();
	}

	public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java -jar crawling.GetFriendsMulti.jar properFile accessFile UserFile");
            System.exit(-1);
        }
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

		GetFollowingMulti userTL = new GetFollowingMulti(args[0], args[1], args[2]);
		userTL.doGet(false);
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}
}
