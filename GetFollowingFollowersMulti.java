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
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

/*
 * Download all the friends that a user has followed.
 * @author Jinxue Zhang
 */
public class GetFollowingFollowersMulti {
	PrintWriter outMetaInfo = null;
	String userFile;
	ArrayList<Long> users;
	int numCheck; 
	int currentCheck;
	ArrayList<Twitter> twitters;
	private long updateFreq = 50;
	
	int friendUpper = 10000;

	GetFollowingFollowersMulti(String propFile, String access, 
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
			FileWriter outFile = new FileWriter("stat-" + 
					userId + ".txt", true);
			outMetaInfo = new PrintWriter(outFile);
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

	void doGet(){
		int userIndex = 0;
		int userTotal = users.size();
		int friends = 0, followers = 0;
		String username = " ";
		String loc = " ";
		
		for (Long usr : users) {
			friends = 0;
			followers = 0;
			
			Twitter twitter = nextTwitter();
			try {
				User tuser = twitter.showUser(usr);
				friends = tuser.getFriendsCount();
				followers = tuser.getFollowersCount();
				username = tuser.getScreenName();
				if(tuser.getLocation() != null)
					loc = tuser.getLocation().replace("\n", "").replace("\r", "");
				else
					loc = "null"; 
			} catch (TwitterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				/*try {
					Thread.sleep(updateFreq * 10);
					User tuser = twitter.showUser(usr);
					friends = tuser.getFriendsCount();
					followers = tuser.getFollowersCount();
				}catch (TwitterException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}
			System.out.println(userIndex + "/" + userTotal + "," + username + ", " + friends + "," + followers);
			outMetaInfo.println(usr + "::" + username + "::" + friends + "::" + followers + "::" + loc);
			userIndex ++;
		}
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

		GetFollowingFollowersMulti userTL = new GetFollowingFollowersMulti(args[0], args[1], args[2]);
		userTL.doGet();
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}
}
