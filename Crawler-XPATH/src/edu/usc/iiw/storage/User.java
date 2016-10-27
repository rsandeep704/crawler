/**
 * 
 */
package edu.usc.iiw.storage;

import java.io.Serializable;
import java.util.ArrayList;

/** Class which encapsulates information about the user. Objects of this class are stored in Berkley DB 
 * @author 
 *
 */
public class User implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String username;
	String password;
	String name;
	ArrayList<String> channelNames = new ArrayList<String>();
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getChannelNames() {
		return channelNames;
	}
	
	public void addChannelName(String channelName) {
		channelNames.add(channelName);
	}

	public void removeChannelName(String channelName) {
		channelNames.remove(channelName);
	}

}
