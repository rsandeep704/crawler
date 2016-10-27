/**
 * 
 */
package edu.usc.iiw.storage;

import java.io.Serializable;
import java.util.Date;

/** Class which encapsulates information about the Crawled URLs. Objects of this class are stored in Berkley DB
 * @author 
 *
 */
public class CrawledURL implements Serializable{

	String url;
	String content;
	Date lastCrawlTime;
	String contentType;
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public CrawledURL() {
		this.lastCrawlTime = new Date(Long.MIN_VALUE);
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Date getLastCrawlTime() {
		return lastCrawlTime;
	}
	public void setLastCrawlTime(Date lastCrawlTime) {
		this.lastCrawlTime = lastCrawlTime;
	}

	
}
