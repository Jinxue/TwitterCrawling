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
import java.util.Date;

import spammerwadgets.OAuthConfigXML;
import spammerwadgets.OAuthTwitter;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class ShowStatusMulti {
	PrintWriter out = null;
	String stFile;
	ArrayList<Long> statuses;
	int numCrawl; 
	int currentCrawl;
	ArrayList<Twitter> twitters;

	ShowStatusMulti(String propFile, String access, 
			String stF){
		OAuthConfigXML config = new OAuthConfigXML(propFile, access);
		stFile = stF;
		
		buildTwitters(config);
		
		String[] splits = stF.split("\\.");
		buildOutFile(splits[0]);

		loadStatuses();
	}

	ShowStatusMulti(String accessFile, 
			String stF){
		stFile = stF;
		
		buildTwitters(accessFile);
		
		String[] splits = stF.split(".");
		buildOutFile(splits[0]);

		loadStatuses();
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
			br = new BufferedReader(new FileReader(stFile));
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
		try {
			FileWriter outFile = new FileWriter("mediaURL-" + 
					userId + ".txt", true);
			out = new PrintWriter(outFile);
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

	private void loadStatuses() {
		// TODO Auto-generated method stub
		File inFile = new File(stFile);

		if (!inFile.isFile()) {
			System.out.println("Parameter is not an existing file");
			return;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(stFile));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		statuses = new ArrayList<Long>();

		String line = null;

		//Read from the original file and write to the new
		//unless content matches data to be removed.
		try {
			while ((line = br.readLine()) != null)
				statuses.add(Long.parseLong(line.split("::")[0]));
			br.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("We will crawl " + statuses.size() + " statuses.");
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
	
	public void doCrawl(){
		int stIndex = 0;
		int stTotal = statuses.size();
		
		for (Long status : statuses) {
			//System.out.println("%" + usr);
	        try {
	        	/* ******************************************************
	        	 * This code is very important to keep safe with the limit
	        	 * rate
	        	 * ******************************************************/
	        	/* The first account has reached the limit..... */
	        	/*if(currentCrawl == 0)
	        		currentCrawl ++;*/
	            Status st = twitters.get(currentCrawl).showStatus(status);
				currentCrawl ++;
				if(currentCrawl == this.numCrawl)
					currentCrawl = 0;

				Thread.currentThread();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/*********************************************************
				 * End.
				 *********************************************************/
				
				if(st == null)
					continue;
				
				String record = "";
				record += st.getId();
				record += "::";
				record += showEntities(st);
				
	            out.println(record);
			} catch (TwitterException te) {
				te.printStackTrace();
				System.out.println("Failed to show status: "
						+ te.getMessage());
			}
			System.out.println(stIndex + "/" + stTotal); 
			stIndex ++;
		}
		out.println("#" + statuses.size());
		closeFile();
	}
	
    private String showEntities(Status status){
    	int len = status.getURLEntities().length;
		String record = "";
		if (len > 0) {
			//record += "::";
			for (int l = 0; l < len; l++) {
				URLEntity ent = status.getURLEntities()[l];
				record += "," + ent.getURL() + "|"
						//+ ent.getDisplayURL() + "|"
						+ ent.getExpandedURL();
			}
		} else
			record = "-1";
		//System.out.println(record);
		len = status.getMediaEntities().length;
		record += "::";
		if (len > 0) {
			//record += "::";
			for (int l = 0; l < len; l++) {
				MediaEntity ent = status.getMediaEntities()[l];
				record += "," + ent.getMediaURL() + "|"
						//+ ent.getDisplayURL() + "|"
						+ ent.getExpandedURL() + "|" + ent.getURL();
			}
		} else
			record += "-1";
		return record;
    }
	
    public void closeFile(){
		out.close();
	}

	public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Usage: java twitter4j.examples.ShowUserMulti properFile accessFile UserFile");
            System.exit(-1);
        }

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		System.out.println("----------------------------------------------");
		System.out.println("Start at: " + dateFormat.format(date));

		ShowStatusMulti st = new ShowStatusMulti(args[0], args[1], args[2]);
		st.doCrawl();
		//userTL.showRateLimit();
		
		date = new Date();
		System.out.println("----------------------------------------------");
		System.out.println("End at: " + dateFormat.format(date));
	}

}
