package spammerwadgets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public final class OAuthConfig {
	
	static int numberOfConfig = 15;
	
	static String[] twitterScreenNames = {//"azcardinal1",
									//"Babylon52", 		// 1 backlist URL (http://businessinsurancecompare.info/myvideos12/)
									"BeautifulAfric1", 
									"greatcanyon1",
									"GreatWallChina1", 
									//"lalaker3", 		//3 backlist URLs
									"musiclady1231", 
									//"PyramidEgypt",	// 1 backlist
									"swimmingphil", 	// swimmingphil@163.com
									"TajMahal18",		// 1 backlist but not be suspended yet
									"MouseZhang",
									//"JohnHaden1",		// 1 back list
									"DragonChow1",
									"JacksonMoory",
									"AdelmanYao",
									//"PerrigSue",		// 1 back list http://businessinsurancecompare.info/myvideos7/
									"LittleMouseZhan",
									"clipperFan18",
									"YellowStone14",
									"SpringBeautifu1",
									"ChristinaChong5"};
	String oAuthConsumerKey = "E7uuluwC0ZsAFbV9pHimQ";
	String oAuthConsumerSecret = "c7amwRvOgb4icJArTRfgU8m2G64mbpVioqgmPDT4Yg";
	
	static String[] oAuthAccessTokens = {//"408675230-sa5KQuamRdlYzbipFlnp8ws9OczktPpmk4yXvCM2",
										//	  "410219933-lNpJPQJ6Ag20v4e5i5K9pmLus3DTcEHoc0wGaCtN",
											  "410299637-uSb2d6LJPV9DVsPywRAoNGe5rMNwUufDTXzQwAoA",
											  "408676902-A9m39G29sywUcJUDwPuD5o4gAhCSQynrHQ00v5Pp",
											  "410166511-NyrCv9QLQZr1M7AB6TNqYpSvGh7SRywb56fySyLJ",
											  //"408678509-Oktt4wAMcm4qKQv9evynOlRSFsK9nRVYqRL7JbC3",
											  "408684934-OsVMhvCHw6LM4x8jncNjRP72h3B3pvDFIGWWcW7s",
											  //"410173552-xrvk8VbrKV8Ij4wX5NttCZnolhoFiKnwdSlrXlDP",
											  "408687707-CVKvMm9EXlT72RoB2nYkrTiibURSetEK4dnfhVvS",
											  "410187731-owwoEKLKfJ5XuicwMOVwYgFiDRJ829vgYvZFiMPI",
											  "419652434-MnBzj1x2zmtZXNFTBpFSgbMCate5smjlbF3IQXSr",
											  //"419677600-jyQEoPoxhVfbgxDW88RlXABeR4IQeukiT3Zjkq1w",
											  "419683691-9iIjTNUjc2gJyPn9nHubB3HVFTIBgCWwGeb7W5rT",
											  "419690284-KA936uijr3XK0IO9rxydGu1lyKe2ntzhR1kfRoXJ",
											  "419694762-iUNogUbIHwMKcRzGLCPrAjBUaI4oamWjHhix5r6U",
											  //"419699384-AcaBQAfEOuHgtVYSipSUiOJuq2uNrLPTiG5dBJRe",
											  "419671430-HOElyVfrRF4SfH7YdydWP46aLIr4g1pW2Gxx5QKe",
											  "442255393-EVQHhMlp5elIcEtwiCxeKATwpAKTWS1j6wHi5y9v",
											  "442275710-getADYmwnSn0NW4x2EzZAV3QPFjuH1cciQVxVo40",
											  "442279873-CgeUssD4LKiEPw9tmXA5R5p8CdexawdZX0Z2XpFB",
											  "442287630-OomujukQPwvrIoRxLdRmmCt90o9HP5LKwV1N2tih"};
	
	static String[] oAUthAccessTokenSecrets = {//"gdCxpHa9CmXEgmrXeySAUChK9oRbUgSUuAMIMAIlXg",
											//		"ZhvlSxSXXB2ytqCqrMUnJZRsjZBhg02DmyQ26fTNtG8",
													"p0nwp2bfrFl9zQ5w65GB7SXqCh24uCjE8J87VYfNk",
													"3vGMmKY5xfHIV1GOSenDAD6gT3FnLkzEa0CWJnBT7cM",
													"WWbzINkH3nfmm2VPVTfGGeQTWauKy0N4VXEAEdWKAec",
													//"fgl4yaJM7y1Q21uHDqpYtVGxa40yEdgCa1LRSzXrk",
													"0TWCBUq0uwbO3g4qiIOjN3ogM2AopBj8iOftqbpsk",
													//"iMGJQpPOKPLBNyggm0VDGvJmSs5esiHHkZFv8MBk6Z4",
													"z5DqJyHxUTAKhsG4jrGCn47SwqPWinvsarh8RX2ebM",
													"0fffVUKqpiiUuBYn7warXf5DAYf8l6GpmyQzchBY",
													"64WrYvNioVAOVhvC2XNcjaFRaL81BnEXRjndyRuT8M",
													//"A0dPkY1paPZ0sseFMpKR00j1pkbaQd1j88nwXCJ4PA",
													"WRgrUHWYZ19lV0qhUB1WsMo2s46RjO2W6mWLLekY",
													"HEcRyIf45somY2asBPs6lP1J4fKl4hNLuf5Dc2pHk",
													"nnO0RY4ocys9uNSN6J30TDmpmazR2CirvnDBUsdKY",
													//"qIm63O3h4QzyXa5HoOzZwVRbSxakG9J3zqc85NHCs8",
													"kwuVsO8LHmIMfUy8RvTG3zIBouOL5qPsP28VGziSsw",
													"LmbAr4xRJdYN1QAkUQMVPieFCK5Jql59a35QMSTth4",
													"mw2nzItwCJ5qd4lNlljGUWdwu5Wj1R7X0L7hh8ZNk",
													"AxpXYhvEu2lI9UAVEq2SS35Sa8WbPSwvMnlbTurH8",
													"Ee2ZpTp9I2qENy3Ol614wMSaClwjpfZ4V5IhGwcTg"};
	
	HashMap<String, String> oAuthAccessTokenMap = new HashMap<String, String>();
	HashMap<String, String> oAuthAccessTokenSecretMap = new HashMap<String, String>();

	public OAuthConfig()
	{
		for (int i = 0; i < numberOfConfig; i ++){
			oAuthAccessTokenMap.put(twitterScreenNames[i], oAuthAccessTokens[i]);
			oAuthAccessTokenSecretMap.put(twitterScreenNames[i], oAUthAccessTokenSecrets[i]);
		}
	}
	
	/*
	 * Singleton construction method
	 */
	public static OAuthConfig getInstance(){
		return new OAuthConfig();
	}
	
	public String getOAuthConsumerKey(){
		return oAuthConsumerKey;
	}
	
	public String getOAuthConsumerSecret(){
		return oAuthConsumerSecret;
	}
	
	public String getoAuthAccessTokenByScreenName(String sreenName){
		return oAuthAccessTokenMap.get(sreenName);
	}
	
	public String getoAuthAccessTokenSecretByScreenName(String sreenName){
		return oAuthAccessTokenSecretMap.get(sreenName);
	}
	
	public int getNumber(){
		return numberOfConfig;
	}
	
	public String[] getScreenNumber(){
		return twitterScreenNames;
	}
	
	// Transfer the tokens to XML file
	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException{
    	String tokenFile = "Early-AccessToken.xml";

		// Store the results in a XML file
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// root elements
		org.w3c.dom.Document doc = docBuilder.newDocument();
		org.w3c.dom.Element rootElement = doc.createElement("UserAccessTokens");
		doc.appendChild(rootElement);

		for (int i = 0; i < numberOfConfig; i++){
			// Finally, write them to the XMl file
			// Store the accessToken;
			org.w3c.dom.Element token = doc.createElement("UserAccessToken");
			rootElement.appendChild(token);
				
			org.w3c.dom.Element usernameXML = doc.createElement("username");
			usernameXML.appendChild(doc.createTextNode(twitterScreenNames[i]));
			token.appendChild(usernameXML);
			
			org.w3c.dom.Element passwordXML = doc.createElement("password");
			passwordXML.appendChild(doc.createTextNode("TestZhang123"));
			token.appendChild(passwordXML);

			org.w3c.dom.Element tokenXML = doc.createElement("AccessToken");
			tokenXML.appendChild(doc.createTextNode(oAuthAccessTokens[i]));
			token.appendChild(tokenXML);
			
			org.w3c.dom.Element tokenSecretXML = doc.createElement("AccessTokenSecret");
			tokenSecretXML.appendChild(doc.createTextNode(oAUthAccessTokenSecrets[i]));
			token.appendChild(tokenSecretXML);
		}
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(tokenFile));
		 
		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);
		 
		transformer.transform(source, result);
		 
		System.out.println("Record " + numberOfConfig + " items. File saved!");
	}
}
