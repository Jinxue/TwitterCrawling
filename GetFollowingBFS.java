package crawling;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import spammerwadgets.OAuthConfigXML;
import spammerwadgets.OAuthTwitter;
import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/*
 * Crawl the sampled network from a seed by using a BFS method
 * @author Jinxue Zhang
 */
public class GetFollowingBFS {
	PrintWriter outFriendsId = null;
	PrintWriter outFriendEdgesId = null;
	long seed;
	LinkedList<Long> users;
	ArrayList<Long> outputList;
	int numCheck; 
	int currentCheck;
	ArrayList<Twitter> twitters;
	int updateFrequency;		// With the unit of milliseconds
	int maxUsers;				// The maximum number of crawled users
	int maxPerUsers;			// The maximum number of followings for each user


	GetFollowingBFS(String propFile, String access, 
			String seedID){
		OAuthConfigXML config = new OAuthConfigXML(propFile, access);
		seed = Long.parseLong(seedID);

		buildTwitters(config);
		
		buildOutFile(seedID);
		
		users = new LinkedList<Long>();
		users.add(seed);
		
		outputList = new ArrayList<Long>();
		outputList.add(seed);
		
		updateFrequency = 200;
		maxUsers = 100000;
		maxPerUsers = 1000;
	}

	private void buildOutFile(String userId) {
		// TODO Auto-generated method stub
		try {
			FileWriter outFile = new FileWriter("Followings-" + 
					userId + ".txt", false);
			outFriendsId = new PrintWriter(outFile);
			FileWriter outFile2 = new FileWriter("FollowingEdges-" + 
					userId + ".txt", false);
			outFriendEdgesId = new PrintWriter(outFile2);
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
		System.out.println("Total users for crawling: " + numCheck);
	}

	void doBFS(){
		int crawledUsers = 0;
		//int userTotal = users.size();
		long cursor = -1;
		
		IDs ids = null;
		int number = 0, rounds = 0;
		
		while(users.isEmpty() == false) {
			long usr = users.remove();
			ArrayList<Long> perUserList = new ArrayList<Long>();
			number = 0;
			rounds = 0;
			cursor = -1;
			ids = null;
			
			do {
				//ids = this.getFriends(twitters.get(currentCheck), usr);
				try {
					ids = twitters.get(currentCheck).getFriendsIDs(usr, cursor);
	        		for (long id : ids.getIDs()) {
	        			//outFriendsId.println(id);
	        			perUserList.add(id);
	        			// Place the followings to the queue
	        			//users.add(id);
	        		}
	            	number += ids.getIDs().length;
	            	rounds ++;
				} catch (TwitterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					
					// If the user has been suspended
					break;
				}
				currentCheck ++;
				if(currentCheck == this.numCheck)
					currentCheck = 0;
			
				Thread.currentThread();
				try {
					Thread.sleep(this.updateFrequency);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}while ((cursor = ids.getNextCursor()) != 0);
			
			/*
			 * If the number of followings has exceeded the maxPerUsers, we will use the maxPerUsers
			 */
			int actualNumber = 0;
			if (perUserList.size() > this.maxPerUsers){
				while (actualNumber < maxPerUsers && perUserList.isEmpty() == false){
					Random random = new Random();
					long select = perUserList.remove(random.nextInt(perUserList.size()));
					// Place the followings to the queue
					if (outputList.contains(select) == false){
						users.add(select);
						outputList.add(select);
						actualNumber ++;
					}
				}
			}else{
				for (long id : perUserList) {
         			// Place the followings to the queue
        			if (outputList.contains(id) == false){
        				users.add(id);
        				outputList.add(id);
        				actualNumber ++;
        			}
        		}
			}
			
			crawledUsers = outputList.size();
			//outFriendsId.println("%" + usr + ", " + number + ", " + actualNumber + ", " + rounds);
			System.out.println(usr + ", " + number + ", " + actualNumber + ", " + crawledUsers); 
			if(crawledUsers > this.maxUsers)
				break;
		}

		for (long id : outputList) {
			outFriendsId.println(id);
		}
		outFriendsId.close();
	}

	void doGet(){
		int userIndex = 0;
		int userTotal = outputList.size();
		long cursor = -1;
		
		IDs ids = null;
		int number = 0, rounds = 0;
		
		for (Long usr : outputList) {
			number = 0;
			rounds = 0;
			cursor = -1;
			ids = null;
			
			do {
				//ids = this.getFriends(twitters.get(currentCheck), usr);
				try {
					ids = twitters.get(currentCheck).getFriendsIDs(usr, cursor);
	        		for (long id : ids.getIDs()) {
	        			outFriendEdgesId.println(id);
	        		}
	            	number += ids.getIDs().length;
	            	rounds ++;
				} catch (TwitterException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					
					// If the user has been suspended
					break;
				}
				currentCheck ++;
				if(currentCheck == this.numCheck)
					currentCheck = 0;
			
				Thread.currentThread();
				try {
					Thread.sleep(this.updateFrequency);
				} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				}
			}while ((cursor = ids.getNextCursor()) != 0);

			outFriendEdgesId.println("%" + usr + ", " + number + ", " + rounds);
			System.out.println(userIndex + "/" + userTotal + ", " + number + ", " + rounds); 
			userIndex ++;
		}
		outFriendEdgesId.close();
	}

	public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java -jar crawling.GetFollowingBFS.jar properFile accessFile seedUserID");
            System.exit(-1);
        }
        
        // @JinxueZhang. ID: 355819995
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

		GetFollowingBFS userTL = new GetFollowingBFS(args[0], args[1], args[2]);
		userTL.doBFS();
		userTL.doGet();
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}

}
