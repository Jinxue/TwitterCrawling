package crawling;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class GetVerifiedUsers {

	static PrintWriter outf = null;

	public static void main(String[] args) {
		
		String usr = "verified";
		int count = 0;
		try {
			FileWriter outFile = new FileWriter("verifiedUsers.txt", false);
			outf = new PrintWriter(outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean alt = false;
        try {
            long cursor = -1;
            IDs ids;
            System.out.println("Listing following ids.");
            do {
	            Twitter twitter = getOAuthTwitter1();
	            if (alt == false)
	            	twitter = getOAuthTwitter2();
	            alt = !alt;
            	ids = twitter.getFriendsIDs(usr, cursor);
                for (long id : ids.getIDs()) {
                    //System.out.println(id);
                    outf.println(id);
                }
                count += ids.getIDs().length;
            } while ((cursor = ids.getNextCursor()) != 0);
            
            System.out.println("The total number of friends: " + count);
            outf.close();
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get friends' ids: " + te.getMessage());
            System.exit(-1);
        }
    }
	
	/*
	 * Application CheckTweetMali
	 */
	static Twitter getOAuthTwitter1() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("8ouu3VG1dSDFOsz0UGFnpw")
				.setOAuthConsumerSecret(
						"SBut7xHvLGFpSBPnt2TxFKq2nZuZoNbaXolncCH4Os")
				.setOAuthAccessToken(
						"701886691-1IXa78lrYrGc6uzhOs7PxgNl5LQfNE0o8ObEJEPZ")
				.setOAuthAccessTokenSecret(
						"XqqYtQuHykY4AQquBcSaK5yJRuJ6d5j75jMft7MmgE");
		
		// This declaration is not needed in Twitter4J 4.0.2
		//cb.setUseSSL(true);
		
		return new TwitterFactory(cb.build()).getInstance();
	}

	static Twitter getOAuthTwitter2() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey("96vFsO7KxTojBCFWsig")
				.setOAuthConsumerSecret(
						"zTxu00E7XlsKRgEz32FcTXrB5UhLEkSpZS1sK8n68w")
				.setOAuthAccessToken(
						"1668836653-o6JiSJ8d4EkFGngTPRuBoQ36qCuF3BzgPOUBUeJ")
				.setOAuthAccessTokenSecret(
						"LEMyxXqHzHLqRLuHwRJXXTwEuRKxjaksekr5eproWXeND");
		
		// This declaration is not needed in Twitter4J 4.0.2
		//cb.setUseSSL(true);
		
		return new TwitterFactory(cb.build()).getInstance();
	}
}
