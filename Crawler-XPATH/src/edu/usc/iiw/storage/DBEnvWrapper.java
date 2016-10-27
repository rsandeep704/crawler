/**
 * 
 */
package edu.usc.iiw.storage;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/** Wrapper which wraps the Database objects into an environment
 * @author 
 *
 */
public class DBEnvWrapper {
	
	private String envDirectory = null;
	private Environment myEnv;
	private DBWrapper user;
	private DBWrapper channel;
	private DBWrapper crawledURL;
	private DBWrapper urlQueue;
	private DBWrapper robotsTxt;
	private DBWrapper seenURL;
	/**
	 * 
	 */
	public DBEnvWrapper(String envDirectory) {
		this.envDirectory = envDirectory;
		
		File envPath = new File(envDirectory);
		
		// if the directory does not exist, create it
		if (!envPath.exists()) {
			
		    System.out.println("creating directory: " + envDirectory);
		    boolean result = false;

		    try{
		    	envPath.mkdirs();
		        result = true;
		    } 
		    catch(SecurityException se){
		        //handle it
		    }        
		    if(result) {    
		        System.out.println(envDirectory + " created");  
		    }
		}
		
		// Instantiate an environment and database configuration object
        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        
        // Configure the Environment
        myEnvConfig.setTransactional(true);
        myEnvConfig.setAllowCreate(true);
        
        // Instantiate the Environment. This opens it and also possibly
        // creates it.
		this.myEnv = new Environment(envPath, myEnvConfig);
		this.user = new DBWrapper(myEnv, "user");
		this.channel = new DBWrapper(myEnv, "channel");
		this.crawledURL = new DBWrapper(myEnv, "crawled_url");
		this.urlQueue = new DBWrapper(myEnv, "url_queue");
		this.robotsTxt = new DBWrapper(myEnv, "robots_txt");
		this.seenURL = new DBWrapper(myEnv, "seen_url");
	}
	
	public DBWrapper getRobotsTxtDatabase() {
		return robotsTxt;
	}

	public void setRobotsTxtDatabase(DBWrapper robotsTxt) {
		this.robotsTxt = robotsTxt;
	}
	
	public DBWrapper getSeenURLDatabase() {
		return seenURL;
	}

	public void setSeenURLDatabase(DBWrapper seenURL) {
		this.seenURL = seenURL;
	}

	public Environment getMyEnv() {
		return myEnv;
	}

	public DBWrapper getUserDatabase() {
		return user;
	}

	public DBWrapper getChannelDatabase() {
		return channel;
	}

	public DBWrapper getCrawledDatbase() {
		return crawledURL;
	}
	
	public DBWrapper getUrlQueue() {
		return urlQueue;
	}
	
	public void userClose() {
		user.close();
	}
	
	public void crawledURLClose() {
		crawledURL.close();
	}
	
	public void channelClose() {
		channel.close();
	}
	
	public void urlQueueClose() {
		urlQueue.close();
	}
	
	
	// Close the environment and associated Databases
	 public void close() {
		 if (myEnv != null) {
			 try {
				 userClose();
				 crawledURLClose();
				 channelClose();
				 urlQueueClose();
				 robotsTxt.close();
				 myEnv.close();
				 } catch(DatabaseException dbe) {
					 System.err.println("Error closing environment" +
					 dbe.toString());
			 }
		 }
	 }

}
