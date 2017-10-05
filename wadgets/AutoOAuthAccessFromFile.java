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
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @author Jinxue Zhang - Arizona State University
 * @since Twitter4J 2.1.7
 */
public class AutoOAuthAccessFromFile {
	/**
	 * Usage: java twitter4j.examples.oauth.AutoOAuthAccessFromFile [usersFile]
	 * 
	 * @param args
	 *            usersFile The format of the usersFile is username | password
	 *            for each line
	 */
	String usersFile, tokenFile;
	BufferedReader br;

	org.w3c.dom.Document doc;

	org.w3c.dom.Element rootElement;

	//String consumerKey = "E7uuluwC0ZsAFbV9pHimQ";
	//String consumerSecret = "c7amwRvOgb4icJArTRfgU8m2G64mbpVioqgmPDT4Yg";
	String fileName = "twitter4j.properties";

	int count = 0;

	AutoOAuthAccessFromFile(String usersF, String appName, String consumerKey, String consumerSecret) {

		OutputStream os = null;
		//InputStream is = null;
		//File file = new File(fileName);
		Properties prop = new Properties();

		try {
			/*if (file.exists()) {
				is = new FileInputStream(file);
				prop.load(is);
			} else {*/
				//prop.setProperty("oauth.consumerKey", OAuthConfigXML.oAuthConsumerKey);
				//prop.setProperty("oauth.consumerSecret", OAuthConfigXML.oAuthConsumerSecret);
			prop.setProperty("oauth.consumerKey", consumerKey);
			prop.setProperty("oauth.consumerSecret", consumerSecret);
				
			os = new FileOutputStream(fileName);
				prop.store(os, fileName);
			//}
		} catch (IOException ie) {
			ie.printStackTrace();
			System.out
					.println("Failed to operate the files " + ie.getMessage());
			System.exit(-1);
		}

		usersFile = usersF;

		tokenFile = usersFile.split("\\.")[0] + "-" + appName + "-AccessToken.xml";
		// Pasrse the usersFile
		FileReader fr = null;
		try {
			fr = new FileReader(usersFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		br = new BufferedReader(fr);

		// Store the results in a XML file
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// root elements
		doc = docBuilder.newDocument();
		rootElement = doc.createElement("UserAccessTokens");
		doc.appendChild(rootElement);

	}

	void getAllTokens() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		String user = null;
		do {
			try {
				user = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (null == user)
				break;

			//String[] comb = user.split("\\|");
			String[] comb = user.split("\\s+");
			String username = comb[0];
			String password = comb[1];

			// Then get the access token
			// AccessToken accessToken = null;

			getAndWriteTokenToXML(username, password);

			System.out.println("End at: " + dateFormat.format(date));
			System.out.println(count
					+ "----------------------------------------------");

			/*if(count == 111)
				break;*/
			
			Thread.currentThread();
			Random rand = new Random();
			try {
				Thread.sleep(rand.nextInt(3) * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (null != user);

		// write the content into file
		storeTokensToFile();

		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void storeTokensToFile() {
		if (count > 0) {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = null;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(tokenFile));

			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Record " + count + " items. File saved!");
		}
	}

	void getAndWriteTokenToXML(String username, String password) {

		try {
			AccessToken accessToken = null;
			accessToken = getAccessToken(username, password);
			if(accessToken == null){
				return;
			}
			// Store the accessToken;
			org.w3c.dom.Element token = doc.createElement("UserAccessToken");
			rootElement.appendChild(token);

			org.w3c.dom.Element usernameXML = doc.createElement("username");
			usernameXML.appendChild(doc.createTextNode(username));
			token.appendChild(usernameXML);

			org.w3c.dom.Element passwordXML = doc.createElement("password");
			passwordXML.appendChild(doc.createTextNode(password));
			token.appendChild(passwordXML);

			org.w3c.dom.Element tokenXML = doc.createElement("AccessToken");
			tokenXML.appendChild(doc.createTextNode(accessToken.getToken()));
			token.appendChild(tokenXML);

			org.w3c.dom.Element tokenSecretXML = doc
					.createElement("AccessTokenSecret");
			tokenSecretXML.appendChild(doc.createTextNode(accessToken
					.getTokenSecret()));
			token.appendChild(tokenSecretXML);

			count++;
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get accessToken: " + te.getMessage());
			// storeTokensToFile();
			// System.exit(-1);
			Thread.currentThread();
			Random rand = new Random();
			try {
				Thread.sleep(rand.nextInt(200) * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// We will try again
			getAndWriteTokenToXML(username, password);
		} catch (IOException ie) {
			ie.printStackTrace();
			System.out.println("Failed to connect the server: "
					+ ie.getMessage());
			// storeTokensToFile;
			// System.exit(-1);
			Thread.currentThread();
			Random rand = new Random();
			try {
				Thread.sleep(rand.nextInt(200) * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// We will try again
			getAndWriteTokenToXML(username, password);
		}
	}

	AccessToken getAccessToken(String username, String password)
			throws TwitterException, IOException {

		AccessToken accessToken = null;
		//ConfigurationBuilder cb = new ConfigurationBuilder();
		//cb.setUseSSL(true);
		Twitter twitter = new TwitterFactory().getInstance();
		RequestToken requestToken = twitter.getOAuthRequestToken();
		System.out.println("Got request token.");
		System.out.println("Request token: " + requestToken.getToken());
		System.out.println("Request token secret: "
				+ requestToken.getTokenSecret());

		while (null == accessToken) {
			System.out
					.println("Open the following URL and grant access to your account:");
			String url = requestToken.getAuthorizationURL();
			System.out.println(url);

			// Alternatively, we will generate the POST request automatically
			// Firstly, we parse the webpage of the authotizationURL
			Document authDoc = Jsoup.connect(url).get();

			Element form = authDoc.getElementById("oauth_form");

			System.out.println(form.toString());

			String action = form.attr("action");

			String authenticity_token = form.select(
					"input[name=authenticity_token]").attr("value");
			String oauth_token = form.select("input[name=oauth_token]").attr(
					"value");

			// Then submit the authentication webpage
			Document tokenDoc = Jsoup.connect(action)
					.data("authenticity_token", authenticity_token)
					.data("oauth_token", oauth_token)
					.data("session[username_or_email]", username)
					.data("session[password]", password).post();

			// System.out.println(doc2.toString());

			Element oauth_pin = tokenDoc.getElementsByTag("code").first();
			if(oauth_pin == null){
				System.out.println("Something wrong!!!!!!!");
				return null;
			}
			String pin = oauth_pin.text();
			try {
				if (pin.length() > 0) {
					accessToken = twitter
							.getOAuthAccessToken(requestToken, pin);
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
		System.out.println("Username: " + username);
		System.out.println("Password: " + password);
		System.out.println("Access token: " + accessToken.getToken());
		System.out.println("Access token secret: "
				+ accessToken.getTokenSecret());
		return accessToken;
	}

	public static void main(String[] args) throws ParserConfigurationException,
			InterruptedException, TransformerException {
		if (args.length < 4) {
			System.out.println("Usage: java twitter4j.examples.oauth.AutoOAuthAccessFromFile" +
							" [usersFile] [appname] [Consumer key] [Consumer secret]");
			System.exit(-1);
		}
		//String fileName = "user-community.txt";

		AutoOAuthAccessFromFile autoOAuth = new AutoOAuthAccessFromFile(args[0], args[1], args[2], args[3]);
		autoOAuth.getAllTokens();
	}
}
