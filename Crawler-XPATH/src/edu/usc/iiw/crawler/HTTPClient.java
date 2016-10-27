/**
 * 
 */
package edu.usc.iiw.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

/** HTTPClient to send http requests to hosts to download urls
 * @author 
 *
 */
public class HTTPClient {

	String host;
	URL url;
	PrintWriter s_out = null;
	BufferedReader s_in = null;
	String responseHeader = null;
	int statusCode;
	String contentType = "";
	String urlString;
	String method = "GET";
	HashMap<String, String> requestHeader = new HashMap<String, String>();
	HashMap<String, String> headerMap = new HashMap<String, String>();
	HttpsURLConnection httpsCon;
	boolean requestSent = false;
	String message;
	boolean isBodyRead = false;
	
	/**
	 * Constructor initializes the Streams of the socket
	 * 
	 * @param urlString
	 * @throws Exception
	 */
	public HTTPClient(String urlString, String method) throws Exception {
		// System.out.println("[DEBUG] "+method+": "+urlString);
		// use HTTP as the default protocol
		if (!urlString.startsWith("http")) {
			urlString = "http://" + urlString;
		}
		
		this.urlString = urlString;
		this.url = new URL(urlString);
		// handle HTTPS using openconnection()
		if (url.getProtocol().equalsIgnoreCase("https")) {
			httpsCon = (HttpsURLConnection) url
					.openConnection();
			httpsCon.setRequestMethod(method);
			// Disable automatic redirects
			httpsCon.setInstanceFollowRedirects(false);
			httpsCon.setRequestProperty("User-Agent:", "iiwCrawler");			
		} else {
			try {
				int port = url.getPort();
				if (port <= 0)
					port = 80;
				Socket clientSocket = new Socket(url.getHost(), port);
				// set socket timeout to avoid blocking the servlet
				clientSocket.setSoTimeout(10000);
				// writer for socket
				s_out = new PrintWriter(clientSocket.getOutputStream(), true);
				// reader for socket
				s_in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				message = method + " " + urlString
						+ " HTTP/1.0\r\nHost: " + url.getHost()
						+ "\r\nUser-agent: iiwCrawler\r\n";
				
			} catch (IOException e) {
				System.out.println("Error creating ClientSocket to remote host "
								+ urlString);
				e.printStackTrace();
			}
		}
		//System.out.println("Connected to "+url.getHost());
	}
	
	/**
	 * Clone the given HTTPClient object
	 * @param redirectedClient
	 */
	public void clone(HTTPClient redirectedClient) {
		host = redirectedClient.host;
		url = redirectedClient.url;
		s_out = redirectedClient.s_out;
		s_in = redirectedClient.s_in;
		responseHeader = redirectedClient.responseHeader;
		statusCode = redirectedClient.statusCode;
		contentType = redirectedClient.contentType;
		urlString = redirectedClient.urlString;
		method = redirectedClient.method;
		requestHeader = redirectedClient.requestHeader;
		headerMap = redirectedClient.headerMap;
		httpsCon = redirectedClient.httpsCon;
		requestSent = redirectedClient.requestSent;
		message = redirectedClient.message;
	}
	
	/**
	 * Sends a HTTP request with the given message and also parses the response header
	 * @param message
	 * @throws Exception 
	 */
	public void sendRequest() { 
		try {
			requestSent = true;
			if (url.getProtocol().equalsIgnoreCase("https")) {
				httpsCon.connect();
				statusCode = httpsCon.getResponseCode();
				//if(statusCode == )
				contentType = httpsCon.getContentType();
				s_in = new BufferedReader(new InputStreamReader(
						httpsCon.getInputStream()));
				Map<String, List<String>> obj = httpsCon.getHeaderFields();
				//System.out.println(httpsCon.getResponseMessage());
				//System.out.println("HTTPS Response Headers");
				for(String key: obj.keySet()) {
					if(key != null) {
						//System.out.println(key+": "+obj.get(key).get(0));
						headerMap.put(key.trim().toLowerCase(), obj.get(key).get(0).trim());
					}
				}
			} else {
	
				// append the header fields to the request message
				for(String key: requestHeader.keySet()) {
					message += key+": "+requestHeader.get(key) +"\r\n";
				}
				message += "\r\n";
				s_out.println(message);
	
				// Get response line and check status 
				String responseLine;
				//System.out.println("-----------------------");
				//System.out.println("Response Header");
				responseLine = s_in.readLine();
				//System.out.println(responseLine);
				StringTokenizer tokens = new StringTokenizer(responseLine); 
				String httpversion = tokens.nextToken(); 
				statusCode = Integer.parseInt(tokens.nextToken()); 
				//Get response headers from server 
				while((responseLine = s_in.readLine()).length() != 0) {
					//System.out.println(responseLine);
					String [] temp = responseLine.split(":",2);
					temp[0] = temp[0].trim().toLowerCase();
					if(temp.length == 2)
						headerMap.put(temp[0], temp[1].trim());				
				}
			} 
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("Message sent to "+url.getHost());
		//System.out.println("-------------------------------");
		
	}
	
	/**
	 * Redirects the client if the location header field is present
	 * @throws Exception
	 */
	public boolean sendRedirect() throws Exception {
		String location = null;
		boolean isRedirectable = false;
		if(url.getProtocol().equals("https")) {
			location = httpsCon.getHeaderField("Location");
		}
		else if(containsHeaderField("location")) {
			location = getHeaderValue("location");
		}
		if(location != null) {
			// relative redirects
			if(location.startsWith("/"))
				location = url.getHost() + location;
			// System.out.println("Redirecting to "+location);
			HTTPClient redirectedClient = new HTTPClient(location, method);
			redirectedClient.sendRequest();
			this.clone(redirectedClient);
			isRedirectable = true;
		}
		return isRedirectable;
	}
	
	/**
	 * get the redirect link from the response header if present
	 * @return null if location header is not present or absolute URL
	 */
	public String getRedirectLink() {
		String location = null;
		if(containsHeaderField("location")) {
			location = getHeaderValue("location");
			// relative redirects
			if(location.startsWith("/"))
				location = url.getProtocol() + "://"+url.getHost() + location;
			else if( !location.startsWith("www.") && ! location.startsWith("http")) {
				location = url.getProtocol() + "://"+url.getHost() + location;
			} else if(!location.startsWith("http")) {
				location = "http://" + location; 				
			}
		}
		return location;
	}
	
	public HashMap<String, String> getHeaderMap() {
		return headerMap;
	}
	
	/**
	 * returns the value of the header field in the response
	 * @param key
	 * @return
	 */
	public String getHeaderValue(String key) {
		if(headerMap.containsKey(key)) {
			return headerMap.get(key);
		} else {
			return null;
		}
	}
	
	public boolean containsHeaderField(String key) {
		return headerMap.containsKey(key);
	}
	
	/**
	 * Create a key value pair which is to be sent in the request header
	 * @param key
	 * @param value
	 * @throws Exception 
	 */
	public void setRequestHeader(String key, String value) throws Exception {
		if(requestSent == true)
			throw new Exception("HTTP Request already sent. Can't set the header now.");
		if(url.getProtocol().equalsIgnoreCase("https")) {			
			httpsCon.setRequestProperty(key, value);
		}
		else
			requestHeader.put(key, value);
	}

	public BufferedReader getReader() throws Exception {
		if(statusCode == 200 && isBodyRead == false) {
			isBodyRead = true;
			return s_in;
		}
		else {
			throw new Exception("Response code is not 200 or body has already been read. Can't read body");
		}
	}

	/**
	 * Read the body using the socket's reader
	 * @return body as string or null is reader has already been used
	 * @throws Exception
	 */
	public String getBody() throws Exception {
		String responseBody = "";
		if(requestSent == true && statusCode == 200 && isBodyRead == false) {
			isBodyRead = true;
			String responseLine = "";
			while((responseLine = s_in.readLine()) != null) {
				responseBody += responseLine +"\n";
			}
		}
		return responseBody;
	}

	/**
	 * Creates Document Object from the Reader of the socket
	 * 
	 * @return DOM Object
	 * @throws Exception if you try to read from a response whose status code is not 200 OK
	 */
	public Document getDocument() throws Exception {
		Document doc = null;
		String resourcetype = null;
		if (contentType != null)
			resourcetype = contentType;
		else if (urlString.endsWith("html") || urlString.endsWith("htm")) {
			resourcetype = "text/html";
		} else if (urlString.endsWith("xml")) {
			resourcetype = "text/xml";
		}
		// Use JTidy for Malformed HTML Files
		if (resourcetype.contains("html")) {
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setTidyMark(false);
			tidy.setShowWarnings(false);
			tidy.setQuiet(true);
			doc = tidy.parseDOM(new BufferedReader(getReader()), null);
		} else if (resourcetype.contains("xml")) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new BufferedReader(getReader()));
			doc = db.parse(is);
		}
		return doc;
	}
	
	public static void main(String [] args) throws Exception {
		// HTTPClient client = new HTTPClient("https://dbappserv.cis.usc.edu/crawltest.html", "HEAD" );
		/*HTTPClient client = new HTTPClient("https://dbappserv.cis.usc.edu/crawltest.html", "GET");
		client.setRequestHeader("If-Modified-Since", "Sat, 28 Mar 2015 01:54:37 GMT");
		client.sendRequest();*/
		
	}
}
