/**
 * 
 */
package edu.usc.iiw.storage;

import java.io.Serializable;
import java.util.ArrayList;

/** Class which encapsulates information about the Channel. Objects of this class are stored in Berkley DB
 * @author 
 *
 */
public class Channel implements Serializable{
	
	String name;
	String [] xpath;
	ArrayList<String> urlMatch = new ArrayList<String>();
	String xslURL;
	
	
	public String[] getXpath() {
		return xpath;
	}
	public void setXpath(String[] xpath) {
		this.xpath = xpath;
	}
	public ArrayList<String> getUrlMatch() {
		return urlMatch;
	}
	public void setUrlMatch(ArrayList<String> urlMatch) {
		this.urlMatch = urlMatch;
	}
	public String getXslURL() {
		return xslURL;
	}
	public void setXslURL(String xslURL) {
		this.xslURL = xslURL;
	}
	
	public void addUrlMatch(String url) {
		if(! urlMatch.contains(url))
			urlMatch.add(url);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
