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

package spammerwadgets;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @author Jinxue Zhang	-	Arizona State University
 * @since Twitter4J 2.1.7
 */
public class AutoOAuthAccess {
    /**
     * Usage: java  twitter4j.examples.oauth.AutoOAuthAccess [consumer key] [consumer secret] [username] [password]
     *
     * @param args message
     */
	
	static String fileName = "twitter4j.properties";
    public static void main(String[] args) {
        File file = new File(fileName);
        Properties prop = new Properties();
        InputStream is = null;
        OutputStream os = null;
        
        prop.setProperty("oauth.consumerKey", "E7uuluwC0ZsAFbV9pHimQ");
        prop.setProperty("oauth.consumerSecret", "c7amwRvOgb4icJArTRfgU8m2G64mbpVioqgmPDT4Yg");
        prop.setProperty("username", "AdrienneccDusti");
        prop.setProperty("password", "SIRENSSUBROUTINE");

        try {
            if (file.exists()) {
                is = new FileInputStream(file);
                prop.load(is);
            }
            if (args.length < 4) {
                if (null == prop.getProperty("oauth.consumerKey")
                        && null == prop.getProperty("oauth.consumerSecret")
                        		&& null == prop.getProperty("username")
                        		&& null == prop.getProperty("password")) {
                    // consumer key/secret are not set in twitter4j.properties
                    System.out.println(
                            "Usage: java twitter4j.examples.oauth.AutoOAuthAccess [consumer key] [consumer secret][username] [password]");
                    System.exit(-1);
                }
            } else {
                prop.setProperty("oauth.consumerKey", args[0]);
                prop.setProperty("oauth.consumerSecret", args[1]);
                prop.setProperty("username", args[2]);
                prop.setProperty("password", args[3]);
                os = new FileOutputStream(fileName);
                prop.store(os, fileName);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.exit(-1);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignore) {
                }
            }
        }
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            RequestToken requestToken = twitter.getOAuthRequestToken();
            System.out.println("Got request token.");
            System.out.println("Request token: " + requestToken.getToken());
            System.out.println("Request token secret: " + requestToken.getTokenSecret());
            AccessToken accessToken = null;

            while (null == accessToken) {
                System.out.println("Open the following URL and grant access to your account:");
                String url = requestToken.getAuthorizationURL();
                System.out.println(url);
                
                /*
                BufferedReader urlbr = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                String strTemp = "";
                while(null != (strTemp = urlbr.readLine())){
                	System.out.println(strTemp);
                }
                
                
                try {
                    Desktop.getDesktop().browse(new URI(requestToken.getAuthorizationURL()));
                } catch (IOException ignore) {
                } catch (URISyntaxException e) {
                    throw new AssertionError(e);
                }
                System.out.print("Enter the PIN(if available) and hit enter after you granted access.[PIN]:");
                String pin = br.readLine();
                
                */
                
                // Alternatively, we will generate the POST request automatically
                // Firstly, we parse the webpage of the authotizationURL
                Document doc = Jsoup.connect(url).get();
                
                Element form = doc.getElementById("oauth_form");
                
                System.out.println(form.toString());
                
                String action = form.attr("action");
                
                String authenticity_token = form.select("input[name=authenticity_token]").attr("value");
                String oauth_token = form.select("input[name=oauth_token]").attr("value");
                
                
                // Then submit the authentication webpage
                Document doc2 = Jsoup.connect(action)
                		.data("authenticity_token", authenticity_token)
                		.data("oauth_token", oauth_token)
                		.data("session[username_or_email]", prop.getProperty("username"))
                		.data("session[password]", prop.getProperty("password"))
                		.post();
                
                System.out.println(doc2.toString());
                
                Element oauth_pin = doc2.getElementsByTag("code").first();
                String pin = oauth_pin.text();
                try {
                    if (pin.length() > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                    } else {
                        accessToken = twitter.getOAuthAccessToken(requestToken);
                    }
                } catch (TwitterException te) {
                    if (401 == te.getStatusCode()) {
                        System.out.println("Unable to get the access token.");
                    } else {
                        te.printStackTrace();
                    }
                }
            }
            System.out.println("Got access token.");
            System.out.println("Access token: " + accessToken.getToken());
            System.out.println("Access token secret: " + accessToken.getTokenSecret());

            try {
                prop.setProperty("oauth.accessToken", accessToken.getToken());
                prop.setProperty("oauth.accessTokenSecret", accessToken.getTokenSecret());
                os = new FileOutputStream(file);
                prop.store(os, fileName);
                os.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.exit(-1);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignore) {
                    }
                }
            }
            System.out.println("Successfully stored access token to " + file.getAbsolutePath() + ".");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get accessToken: " + te.getMessage());
            System.exit(-1);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to read the system input.");
            System.exit(-1);
        }
    }
}
