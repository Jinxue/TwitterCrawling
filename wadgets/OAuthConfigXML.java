package spammerwadgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OAuthConfigXML {

	int numberOfConfig = 0;

	ArrayList<String> twitterScreenNames = new ArrayList<String>();

	// For snakegroup application which is suspended at this time
	//String oAuthConsumerKey = "E7uuluwC0ZsAFbV9pHimQ";
	//String oAuthConsumerSecret = "c7amwRvOgb4icJArTRfgU8m2G64mbpVioqgmPDT4Yg";

	// For InfoMng application
	// static final String oAuthConsumerKey = "Qv2mNcfPKEqLdqYJe0g6cw";
	//static final String oAuthConsumerSecret = "i39YzEMV5Qbo6huPuiFxGjZF58RqYD75ASa8HP3CdE"; 
		
	// For CommunityQ application
	String oAuthConsumerKey = "A234hozDSm3JaVvBPmxw";
	String oAuthConsumerSecret = "5CFeJ1ntoBkzXuTjhfbGyASr8f5IXWVX6l20Bgs4zM"; 
		
	ArrayList<String> oAuthAccessTokens = new ArrayList<String>();
	ArrayList<String> oAUthAccessTokenSecrets = new ArrayList<String>();

	HashMap<String, String> oAuthAccessTokenMap = new HashMap<String, String>();
	HashMap<String, String> oAuthAccessTokenSecretMap = new HashMap<String, String>();

	public OAuthConfigXML(String propertyFile, String XMLFile){
		InputStream is = null;
		File file = new File(propertyFile);
		Properties prop = new Properties();

		try {
			if (file.exists()) {
				is = new FileInputStream(file);
				prop.load(is);
				this.oAuthConsumerKey = prop.getProperty("oauth.consumerKey");
				this.oAuthConsumerSecret = prop.getProperty("oauth.consumerSecret");
			}
		}catch (IOException ie) {
				ie.printStackTrace();
				System.out
						.println("Failed to operate the files " + ie.getMessage());
				System.exit(-1);
			}
		
		loadOAuthConfigXML(XMLFile);
		}

	public OAuthConfigXML(String XMLFile){
		loadOAuthConfigXML(XMLFile);
	}
	
	private void loadOAuthConfigXML(String XMLFile) {
		// TODO Auto-generated method stub
		
	
		File fXmlFile = new File(XMLFile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = null;
		try {
			doc = dBuilder.parse(fXmlFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();

		System.out.println("Root element :"
				+ doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("UserAccessToken");
		
		numberOfConfig = nList.getLength();
		
		System.out.println("-----------------------");
		System.out.println("The XML file has " + numberOfConfig + " users.");
		
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				/*System.out.println("username : "
						+ getTagValue("username", eElement));
				System.out.println("password : "
						+ getTagValue("password", eElement));
				System.out.println("Access Token : "
						+ getTagValue("AccessToken", eElement));
				System.out.println("Access Token Secret : "
						+ getTagValue("AccessTokenSecret", eElement));*/
			
				twitterScreenNames.add(temp, getTagValue("username", eElement));
				oAuthAccessTokens.add(temp, getTagValue("AccessToken", eElement));
				oAUthAccessTokenSecrets.add(temp, getTagValue("AccessTokenSecret", eElement));
			}
		}

		for (int i = 0; i < numberOfConfig; i++) {
			oAuthAccessTokenMap
					.put(twitterScreenNames.get(i), oAuthAccessTokens.get(i));
			oAuthAccessTokenSecretMap.put(twitterScreenNames.get(i),
					oAUthAccessTokenSecrets.get(i));
		}
	}

	private String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}

	public String getOAuthConsumerKey() {
		return oAuthConsumerKey;
	}

	public String getOAuthConsumerSecret() {
		return oAuthConsumerSecret;
	}

	public String getoAuthAccessTokenByScreenName(String sreenName) {
		return oAuthAccessTokenMap.get(sreenName);
	}

	public String getoAuthAccessTokenSecretByScreenName(String sreenName) {
		return oAuthAccessTokenSecretMap.get(sreenName);
	}

	public int getNumber() {
		return numberOfConfig;
	}

	public String[] getScreenNumber() {
		String [] ret = new String[numberOfConfig]; 
		twitterScreenNames.toArray(ret);
		return ret;
	}

	public ArrayList<String> getScreenNumberList() {
		return twitterScreenNames;
	}

}
