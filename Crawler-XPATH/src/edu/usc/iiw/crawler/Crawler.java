/**
 * 
 */
package edu.usc.iiw.crawler;

import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.usc.iiw.crawler.info.RobotsTxtInfo;
import edu.usc.iiw.storage.Channel;
import edu.usc.iiw.storage.CrawledURL;
import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;
import edu.usc.iiw.storage.Serializer;
import edu.usc.iiw.xpathengine.XPathEngine;
import edu.usc.iiw.xpathengine.XPathEngineFactory;

/**
 * Crawler thread which crawls the web
 * 
 * @author 
 *
 */
public class Crawler  {

	String seedURL;
	DBEnvWrapper dbew;
	BlockingDiskQueue urlQueue;
	private DBWrapper userDB;
	private DBWrapper channelDB;
	private DBWrapper crawledURLDB;
	private DBWrapper robotsTxtDB;
	private DBWrapper seenURLDB;
	private long maxDownloadSize;
	private long maxFilesToCrawl;
	HTTPClient client;
	Integer noFilesCrawled;
	XPathEngine xpei;
	

	/**
	 * Crawl the web starting from the seedURL and using the URLQueue stored on
	 * disk given in the database environment
	 * 
	 * @param m
	 * @param l
	 */
	public Crawler(String seedURL, DBEnvWrapper dbew, long maxDownloadSize,
			long maxFilesToCrawl) {
		this.seedURL = seedURL;
		this.dbew = dbew;
		
		this.maxDownloadSize = maxDownloadSize;
		this.maxFilesToCrawl = maxFilesToCrawl;
		this.xpei = XPathEngineFactory.getXPathEngine();

		// Initialize the database connections
		this.urlQueue = new BlockingDiskQueue(dbew);
		this.urlQueue.enqueue(seedURL);
		this.userDB = dbew.getUserDatabase();
		this.channelDB = dbew.getChannelDatabase();
		this.crawledURLDB = dbew.getCrawledDatbase();
		this.seenURLDB = dbew.getSeenURLDatabase();
		this.robotsTxtDB = dbew.getRobotsTxtDatabase();
		this.noFilesCrawled = 0;
		
	}

	//@Override
	public void run() {
		String url = null;
		//System.out.println("Crawler started");
		while ( !urlQueue.isEmpty() && noFilesCrawled < maxFilesToCrawl) {
			try {
				url = urlQueue.dequeue();
				if(url == null) {
					break;
				} else if(url.trim().length() == 0) {
					break;
				}
				//System.out.println("---------------------------------------");
				//System.out.println("[DEBUG] URL Dequeued: "+url);
				URL urlObj = new URL(url);
				RobotsTxtInfo robot = (RobotsTxtInfo) robotsTxtDB.get(urlObj
						.getHost());
				if (robot == null) {
					// send robots.txt get request and create robots
					client = new HTTPClient(urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt",
							"GET");
					client.sendRequest();
					if (client.statusCode == 200) {
						robot = new RobotsTxtInfo(client.getReader());
						//System.out.println("[DEBUG]Fetched robot");
					}
					else {
						// create Robot with default rules if robots.tx it's not present
						robot = new RobotsTxtInfo();
						//System.out.println("[DEBUG]Creating robot with default rules");
					}
					//System.out.println("[DEBUG]Inserting robot "+robot+" for host "+urlObj.getHost());
					robotsTxtDB.insert(urlObj.getHost(), robot);
				} else {
					//System.out.println(urlObj.getHost()+" robot.txt found");
				}

				// TO DO: change
				if (checkLinks(robot.getAllowedLinks(), urlObj) == false) {
					if (checkLinks(robot.getDisallowedLinks(), urlObj) == true) {
						System.out.println("Not allowed in robots "+url);
						continue;
					}
				}
				
				if(! checkCrawlDelay(robot)) {
					urlQueue.enqueue(url);
					continue;
				}

				// get the last seen time in the current crawl job
				Date lastSeenTime = (Date) seenURLDB.get(url);
				client = new HTTPClient(url, "HEAD");
				
				CrawledURL crawledURL = (CrawledURL) crawledURLDB.get(url);
				String contentType = "html";
				
				if(lastSeenTime != null) {
					client.setRequestHeader("If-Modified-Since", formatHTTPDate(lastSeenTime.getTime()));
				} else {
					lastSeenTime = new Date(Long.MIN_VALUE);
					// check if URL is already in database
					if(crawledURL != null) {
						lastSeenTime = crawledURL.getLastCrawlTime();
						contentType = crawledURL.getContentType();
						client.setRequestHeader("If-Modified-Since", formatHTTPDate(lastSeenTime.getTime()));
					}
				}
				client.sendRequest();
				// enqueue redirect requests
				String location = client.getRedirectLink(); 
				if(location != null) {
					urlQueue.enqueue(location);
					System.out.println("Redirected to "+location);
					continue;
				}
				
							
				Date lastModifiedDate = new Date();
				boolean hasLastModified = false;
				if(crawledURL == null) {
					// If the file has not been modified, continue. It will be updated in the next crawl job
					if(client.containsHeaderField("last-modified")) {
						hasLastModified = true;
						String lastModifiedString = client.getHeaderValue("last-modified");
						lastModifiedDate = getParsedDate(lastModifiedString);
						if(lastModifiedDate.getTime() <= lastSeenTime.getTime()) {
							System.out.println("Not Modified..   "+url);
							continue;
						}
					}
					if(client.statusCode == 304) {
						System.out.println("Not Modified..   "+url);
						continue;
					}
				}
					
				int contentLength = 0;
				if (client.containsHeaderField("content-length")) {
					contentLength = Integer.parseInt(client
							.getHeaderValue("content-length"));
				}
				
				if(client.containsHeaderField("content-type"))	{
					contentType = client.getHeaderValue("content-type")
						.toLowerCase();
				}
				
				boolean isFetchedFromServer = false;
				if((contentType.contains("html") || contentType.contains("xml"))
						&& contentLength < (maxDownloadSize * (long) Math.pow(2, 20))
						&& client.statusCode == 200) {
					
					// fetch file from Server and update CrawledURLDB
					if(crawledURL == null) {
						isFetchedFromServer = true;
						
						client = new HTTPClient(url,"GET");
						client.sendRequest();
						String urlContent = client.getBody();
						
						crawledURL = new CrawledURL();
						
						// update crawledURL and store it in the BerkleyDB
						crawledURL.setUrl(url);
						crawledURL.setLastCrawlTime(new Date());
						crawledURL.setContent(urlContent);
						crawledURL.setContentType(client.contentType);
						crawledURLDB.insert(url, crawledURL);
						System.out.println("Downloading....   "+url);
					} else {
							if(hasLastModified) {
								isFetchedFromServer = true;
								// fetch file from Server and update CrawledURLDB
								if(crawledURL.getLastCrawlTime().getTime() > lastModifiedDate.getTime()) {
									client = new HTTPClient(url,"GET");
									client.sendRequest();
									String urlContent = client.getBody();
									
									crawledURL = new CrawledURL();
									
									// update crawledURL and store it in the BerkleyDB
									crawledURL.setUrl(url);
									crawledURL.setLastCrawlTime(new Date());
									crawledURL.setContent(urlContent);
									crawledURL.setContentType(client.contentType);
									crawledURLDB.insert(url, crawledURL);
									System.out.println("Downloading....   "+url);
								}
							}
					}
						
					// update seenURL
					seenURLDB.insert(url, new Date());
					
					synchronized (noFilesCrawled) {
						noFilesCrawled += 1;
					}
					 
					// update last access of domain in the robot database
					robot.setLastAccessTime(new Date());
					robotsTxtDB.insert(urlObj.getHost(), robot);
			}
	
			// update the url in all the appropriate channels 
			if(contentType.contains("xml")) {
				try {
					Document doc = null;
					DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					InputSource is = new InputSource();
					//if( doc == null && crawledURL != null) {
					is.setCharacterStream(new StringReader(crawledURL.getContent()));
					doc = db.parse(is);
					//}				
					 matchedAnyXpath(url, doc);
				}catch(SAXParseException e) {
					//System.out.println(e+":"+ url);
					continue;
				}
			    /*Iterate over all xpaths and evaluate*/
				
			}
				
			if(isFetchedFromServer || (client.statusCode == 304 && crawledURL != null)) {
				// add all children to queue
				if(contentType.contains("html")) {
					ArrayList<String> links = fetchURLs(crawledURL.getContent(), urlObj);
					for(String link: links) {
						//System.out.println(link);
						urlQueue.enqueue(link);
					}
				}
			}
			}catch (SAXParseException e) {
				//System.out.println(url+" has invalid xml");
				//System.out.println(e);
				e.printStackTrace();
			} 
			catch (Exception e) {
				//System.out.println(e);
				e.printStackTrace();
			}
		}
		try {
			System.out.println("Number of files crawled:" +noFilesCrawled);
			System.out.println("Clearing Cache Tables in BerkleyDB....");
			urlQueue.urlDiskQueue.removeDatabase();
			seenURLDB.removeDatabase();
			robotsTxtDB.removeDatabase();	
		} catch(Exception e) {
			
		}
	}
	
	
	

	/**
	 * Return all the hrefs embedded in the string
	 * @param urlContent 
	 * @return list of absolute URLs
	 */
	private ArrayList<String> fetchURLs(String html, URL urlObj) {
		ArrayList<String> links = new ArrayList<String>();
		Pattern p = Pattern.compile("href=\"(.*?)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(html);
		while (m.find()) {
			String link = m.group(1);
			// relative href link
			if(link.startsWith("/")) {
				String path = urlObj.getPath();
				if(path.endsWith(".html") || path.endsWith(".htm"))
					path = path.substring(0,path.lastIndexOf("/"));
				else if(path.endsWith("/"))
					path = path.substring(0, path.length() - 1);
				link = urlObj.getProtocol() +"://" + urlObj.getHost()+ path  + link;				
			} else if( ! link.startsWith("www.") && ! link.startsWith("http")) {
				String path = urlObj.getPath();
				if(path.endsWith(".html") || path.endsWith(".htm"))
					path = path.substring(0,path.lastIndexOf("/"));
				else if(path.endsWith("/"))
					path = path.substring(0, path.length() - 1);
				link = urlObj.getProtocol() +"://" + urlObj.getHost()+ path  + "/" + link;
			}
			// non-absolute link
			else if(!link.startsWith("http"))
				link = "http://" + link; 
		    links.add(link);
		}		   
		return links;
	}

	/**
	 * Iterate over all the channels in the database and add the matching urls
	 * to corresponding channel
	 * 
	 * @param url
	 * @return true if it matches any of the xpaths in all the channels so that
	 *         it can be read
	 * @throws Exception
	 */
	public void matchedAnyXpath(String url, Document doc) throws Exception {
		Serializer serializer = new Serializer();
		Cursor cursor = channelDB.getDatabase().openCursor(null, null);
		//HTTPClient client = new HTTPClient(url,"GET");
		//client.sendRequest();
		//Document doc = client.getDocument();
        try {
            
            HashMap<String, Channel> map = new HashMap<String, Channel>();
            // Cursors need a pair of DatabaseEntry objects to operate. These
            // hold
            // the key and data found at any given position in the database.
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            // To iterate, just call getNext() until the last database record
            // has been
            // read. All cursor operations return an OperationStatus, so just
            // read
            // until we no longer see OperationStatus.SUCCESS
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                // getData() on the DatabaseEntry objects returns the byte array
                // held by that object. We use this to get a String value. If
                // the
                // DatabaseEntry held a byte array representation of some other
                // data
                // type (such as a complex object) then this operation would
                // look
                // considerably different.
                String channelName = (String) serializer.deserialize(foundKey
                        .getData());
                Channel channel = (Channel) serializer.deserialize(foundData
                        .getData());
                xpei.setXPaths(channel.getXpath());
                boolean [] results = xpei.evaluate(doc);
                for (boolean b : results) {
                    if (b == true) {
                        //System.out.println("[DEBUG] Found matching xml " + url);
                        channel.addUrlMatch(url);
                        map.put(channelName, channel);
                        break;
                    }
                }
            }
            cursor.close();
            for (Entry<String, Channel> entry : map.entrySet()) 
            {
                String key = entry.getKey();
                Channel value = entry.getValue();
                channelDB.insert(key, value);
                //System.out.println("[DEBUG] Matched URL added to Channel database");
                
            }
        } catch (DatabaseException de) {
            System.err.println("Error accessing database." + de);
        } finally {
            // Cursors must be closed.
            cursor.close();
        }

	}

	/**
	 * Convert Date to HTTP formatted string
	 * 
	 * @param date
	 * @return
	 */
	static String formatHTTPDate(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(date);
	}

	/**
	 * Method which tests if the URL can be crawled now
	 * 
	 * @param robot
	 * @param urlObj
	 * @return true if you can crawl the URL right now
	 */
	public boolean checkCrawlDelay(RobotsTxtInfo robot) {
		if(robot.getLastAccessTime() == null)
			return true;
		long diff = new Date().getTime() - robot.getLastAccessTime().getTime();
		if (diff > (robot.getCrawlDelay() * 1000)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method which checks if the url resource path is present in the given list
	 * of links
	 * 
	 * @param links
	 * @param urlObj
	 * @return true if the url is present in the list
	 */
	public boolean checkLinks(ArrayList<String> links, URL urlObj) {
		String resourcePath = urlObj.getPath();
		for (String link : links) {
			
			if (resourcePath.startsWith(link)) {
				// exact match
				if (resourcePath.length() == link.length())
					return true;
				// relative match
				else if (resourcePath.charAt(link.length()) == '/')
					return true;
				// not a match
				else
					return false;
			}
		}
		return false;
	}

	/**
	 * Return the date object from String
	 * 
	 * @param dateString
	 * @return
	 * @throws ParseException
	 */
	Date getParsedDate(String dateString) throws ParseException {
		Date startDate = null;
		if (dateString.contains("-"))
			startDate = new SimpleDateFormat("EEEE, dd-MMM-yy kk:mm:ss zzz",
					Locale.US).parse(dateString);
		else if (dateString.contains("GMT"))
			startDate = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss zzz",
					Locale.US).parse(dateString);
		else if (dateString.contains("GMT") == false)
			startDate = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy",
					Locale.US).parse(dateString);
		return startDate;
	}	
	
}
