/**
 * 
 */
package edu.usc.iiw.xpathengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/** HTTPClient which opens connection to a remote host and fetches the requested resource using HTTP Get request
 * @author iiw
 *
 */
public class HTTPClient {
	
	String host;
	URL url;
	PrintWriter s_out = null;
    BufferedReader s_in = null;
    String responseHeader = null;
    String responseBody = null;
    int statusCode;
    String contentType = "";
    String urlString;
    String method = "GET";
	
    /**
     * Constructor initializes the Streams of the socket
     * @param urlString
     * @throws IOException
     */
	public HTTPClient(String urlString) throws IOException {
		// use HTTP as the default protocol
		if(!urlString.startsWith("http")) {
			urlString = "http://"+urlString;
		}
		this.urlString = urlString;
		this.url = new URL(urlString);
		// handle HTTPS using openconnection()
		if(url.getProtocol().equalsIgnoreCase("https")) {
			HttpsURLConnection httpsCon = (HttpsURLConnection)url.openConnection();
			httpsCon.setRequestMethod(method);
			httpsCon.setRequestProperty("User-Agent:", "iiwCrawler");
			//s_out =  new PrintWriter( httpsCon.getOutputStream(), true);
			statusCode = httpsCon.getResponseCode();
			contentType = httpsCon.getContentType();
			s_in = new BufferedReader(new InputStreamReader(httpsCon.getInputStream()));
		} else {
			try {
				int port = url.getPort();
				if(port<=0)
					port=80;
				Socket clientSocket = new Socket(url.getHost(), port);
				// set socket timeout to avoid blocking the servlet
				clientSocket.setSoTimeout(10000);
				//writer for socket
				s_out =  new PrintWriter(clientSocket.getOutputStream(), true);
	            //reader for socket
	            s_in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	            String message = "GET "+urlString+" HTTP/1.0\r\nHost: "+url.getHost()+"\r\nUser-agent: iiwCrawler\r\n\r\n";
	    	    s_out.println(message);
	    	    //	Get response line and check status
	    	    String responseLine = s_in.readLine();
	    	    StringTokenizer tokens = new StringTokenizer(responseLine);
	    	    String httpversion = tokens.nextToken();
	    	    statusCode = Integer.parseInt(tokens.nextToken());
	    	    //Get response headers from server
	    	    while((responseLine = s_in.readLine()).length() != 0) {
	    	    	if(responseLine.toLowerCase().contains("content-type")) {
	    	    		contentType = responseLine.split(":")[1].split(";")[0];
	    	    	}
	    	    	responseHeader += responseLine + "\n";
	    	    }
			} catch (IOException e) {
				//System.out.println("Error creating ClientSocket to remote host "+urlString);
				e.printStackTrace();
			}
		}
		//System.out.println("Client connected to "+url.getHost());
	    //System.out.println("Message sent to "+url.getHost());
	}
	
	public void setMethod(String method) {
		
	}
	public String getHeaders() {
		return responseHeader;
	}
	
	public BufferedReader getReader() {
		return s_in;
	}
	
	public String getBody() throws Exception {
		if(statusCode == 200)
			return responseBody;
		else
			throw new Exception("Invalid request. Status code: "+statusCode);
	}
	
	/**
	 * Creates Document Object from the Reader of the socket
	 * @return DOM Object
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Document getDocument() throws ParserConfigurationException, SAXException, IOException {
		Document doc = null;
		String resourcetype = null;
		if(contentType != null)
			resourcetype = contentType;
		else if(urlString.endsWith("html") || urlString.endsWith("htm")) {
			resourcetype = "text/html";
		} else if(urlString.endsWith("xml")) {
			resourcetype = "text/xml";
		}
		// Use JTidy for Malformed HTML Files
		if(resourcetype.contains("html")) {
			//System.out.println("html type");
			Tidy tidy=new Tidy();
    	    tidy.setXHTML(true);
    	    tidy.setTidyMark(false);
    	    tidy.setShowWarnings(false);
    	    tidy.setQuiet(true);
    	    doc = tidy.parseDOM(new BufferedReader(getReader()), null);
		} else if(resourcetype.contains("xml")) {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new BufferedReader(getReader()));
			doc = db.parse(is);
		}
		return doc;
	}
	

}
