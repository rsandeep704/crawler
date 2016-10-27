package edu.usc.iiw.crawler.info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;

import edu.usc.iiw.crawler.HTTPClient;

/**
 * Parses the robots.txt and stores the information specific to iiwCrawler
 * @author 
 *
 */
public class RobotsTxtInfo implements Serializable{
	
	private String domainName;
	private ArrayList<String> disallowedLinks;
	private ArrayList<String> allowedLinks;	
	private Integer crawlDelay;
	private String userAgent;
	Date lastAccessTime;
	
	public RobotsTxtInfo(){
		disallowedLinks = new ArrayList<String>();
		allowedLinks = new ArrayList<String>();
		this.userAgent = "iiwcrawler";
		this.crawlDelay = 0;
		 this.lastAccessTime = null;
	}
	
	public RobotsTxtInfo(BufferedReader br) throws IOException {
		
		disallowedLinks = new ArrayList<String>();
		allowedLinks = new ArrayList<String>();
		this.userAgent = "iiwcrawler";
		this.crawlDelay = 0;
		this.lastAccessTime = null;
		
		String responseLine = "";
		boolean isValidUserAgent = false;
		boolean isiiwCrawler = false;
		while((responseLine = br.readLine())!=null) {
			responseLine = responseLine.trim();
			if(!responseLine.startsWith("#")) {
				String [] temp = responseLine.split(":",2);
				if(temp.length == 2) {
					for(int i=0; i<temp.length; i++) {
						temp[i] = temp[i].trim();
					}
					if(temp[0].equalsIgnoreCase("user-agent")) {
						if(isiiwCrawler == false) {
							if(temp[1].equalsIgnoreCase("iiwCrawler")) {
								clear();
								isiiwCrawler = true;
							} else if(temp[1].equals("*")) {
								isValidUserAgent = true;
								isiiwCrawler = false;
							}
							userAgent = temp[1];	
						}else {
							break;
						}
					} else if(isValidUserAgent == true || isiiwCrawler == true) {
						if(temp[0].equalsIgnoreCase("disallow")) {
							temp[1] = URLDecoder.decode(temp[1], "UTF-8");
							if(temp[1].matches("[$?*]"))
								continue;
							/*if(temp[1].lastIndexOf("/") == temp[1].length() - 1)
								temp[1] = temp[1].substring(0,temp[1].length() - 1);*/
							disallowedLinks.add(temp[1]);
						}else if(temp[0].equalsIgnoreCase("allow")) {
							temp[1] = URLDecoder.decode(temp[1], "UTF-8");
							if(temp[1].matches("[$?*]"))
								continue;
							/*if(temp[1].lastIndexOf("/") == temp[1].length() - 1)
								temp[1] = temp[1].substring(0,temp[1].length() - 1);*/
							allowedLinks.add(temp[1]);
						} else if(temp[0].equalsIgnoreCase("crawl-delay")) {
							crawlDelay = Integer.parseInt(temp[1]);
						}
					}
				}
			}
		}
	}

	public String getPrefix(String link) {
		int starIndex = Integer.MAX_VALUE;
		int questionIndex = Integer.MAX_VALUE;
		int dollarIndex = Integer.MAX_VALUE;
		if(link.contains("*")) 
			starIndex = link.indexOf("*");
		if(link.contains("?"))
			questionIndex = link.indexOf("?");
		if(link.contains("$"))
			dollarIndex = link.indexOf("$");
		int minIndex = starIndex;
		if(minIndex > questionIndex)
			minIndex = questionIndex;
		if(minIndex > dollarIndex)
			minIndex = dollarIndex;
		int lastSlash = link.indexOf("/", minIndex);
		return link.substring(0, lastSlash + 1);
	}
	
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public Date getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(Date lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public void setCrawlDelay(Integer crawlDelay) {
		this.crawlDelay = crawlDelay;
	}
	
	public ArrayList<String> getDisallowedLinks(){
		return disallowedLinks;
	}
	
	public ArrayList<String> getAllowedLinks(){
		return allowedLinks;
	}
	
	public int getCrawlDelay(){
		return crawlDelay;
	}

	/**
	 * Clears the array lists
	 */
	public void clear() {
		disallowedLinks.clear();
		allowedLinks.clear();
	}
	
	public void print() {
		System.out.println("User-Agent:" +userAgent);
		System.out.println("Crawl Delay:"+crawlDelay);
		System.out.println("Allowed Links:");
		System.out.println(allowedLinks.size());
		System.out.println("Disallowed Links:");
		System.out.println(disallowedLinks);
		System.out.println(disallowedLinks.size());
	}	
	
	public static void main(String []args) throws Exception {
		HTTPClient client = new HTTPClient("http://en.wikipedia.org/robots.txt","GET");
		client.sendRequest();
		RobotsTxtInfo robot = new RobotsTxtInfo(client.getReader());
		robot.print();
	}
}
