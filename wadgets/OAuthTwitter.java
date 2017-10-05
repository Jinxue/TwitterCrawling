package spammerwadgets;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class OAuthTwitter{

	/*
	 * The screen name
	 */
	static String screenName;
	
	static OAuthConfigXML config;
	
	public static Twitter getOAuthTwitter(OAuthConfigXML conf, String name){
		
		screenName = name;
		config = conf;
		
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey(config.getOAuthConsumerKey())
          .setOAuthConsumerSecret(config.getOAuthConsumerSecret())
          .setOAuthAccessToken(config.getoAuthAccessTokenByScreenName(screenName).trim())
          .setOAuthAccessTokenSecret(config.getoAuthAccessTokenSecretByScreenName(screenName).trim());
        
        /**
         * This is an important notice for developers still using HTTP plaintext connections. 
         * On January 14th, 2014, connections to api.twitter.com will be restricted to TLS/SSL connections only. 
         * If your application still uses HTTP plaintext connections you will need to update it to use HTTPS connections, 
         * otherwise your app will stop functioning. 
         */
        
        // This declaration is not needed in Twitter4J 4.0.2
        //cb.setUseSSL(true);
        
        return new TwitterFactory(cb.build()).getInstance();
	}

}
