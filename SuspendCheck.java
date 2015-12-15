package crawling;

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

import spammerwadgets.OAuthConfigXML;
import spammerwadgets.OAuthTwitter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class SuspendCheck {

	PrintWriter outSuspendId = null;
	String userFile;
	ArrayList<Long> users;
	int numCheck; 
	int currentCheck;
	ArrayList<Twitter> twitters;
	Date start;


	SuspendCheck(String propFile, String access, 
			String userF){
		OAuthConfigXML config = new OAuthConfigXML(propFile, access);
		userFile = userF;

		buildStartTime();
		
		buildTwitters(config);
		
		String[] splits = userF.split("\\.");
		buildOutFile(splits[0]);

		loadUsers();
	}

	private void buildOutFile(String userId) {
		// TODO Auto-generated method stub
		Long rand = (new Date().getTime() - start.getTime()) / 1000;
		try {
			FileWriter outFile = new FileWriter("Suspend-" + 
					userId + "-" + rand + ".txt", true);
			outSuspendId = new PrintWriter(outFile);
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
			while ((line = br.readLine()) != null)
				users.add(Long.parseLong(line));
			br.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//users.add(627648100L);
		System.out.println("We will check " + users.size() + " users.");
	}

	private boolean isSuspended(Twitter twitter, Long usr) {
		// TODO Auto-generated method stub
		boolean ret = false;
		try {
			User user = twitter.showUser(usr);
			System.out.println(user.getName());
		} catch (TwitterException te) {
			// TODO Auto-generated catch block
			//te.printStackTrace();
			//System.out.println(te);
			if (te.getErrorMessage() != null && 
					te.getErrorMessage().contains("suspended")){
				ret = true;
				System.out.println(usr);
				outSuspendId.println(usr);
			}
		}
		return ret;
	}

	void doCheck(){
		int userIndex = 0;
		int userTotal = users.size();

		for (Long usr : users) {
			this.isSuspended(twitters.get(currentCheck), usr);
			currentCheck ++;
			if(currentCheck == this.numCheck)
				currentCheck = 0;
			
			System.out.println(userIndex + "/" + userTotal); 
			userIndex ++;

			Thread.currentThread();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		outSuspendId.close();
	}

	public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java twitter4j.examples.GetUserTimelineByFile properFile accessFile UserFile");
            System.exit(-1);
        }
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

		SuspendCheck userTL = new SuspendCheck(args[0], args[1], args[2]);
		userTL.doCheck();
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}
}
