/**
 * 
 */
package edu.usc.iiw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import edu.usc.iiw.storage.Channel;
import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;
import edu.usc.iiw.storage.Serializer;
import edu.usc.iiw.storage.User;

/**
 * Servlet which is responsible for login/ sign up of user
 * 
 * @author 
 *
 */
public class LoginServlet extends HttpServlet {
	
	DBEnvWrapper env;
	DBWrapper userDB;
	DBWrapper channelDB;

	
	public void init() {		
		env = new DBEnvWrapper(getServletContext()
				.getInitParameter("BDBstore"));
		this.getServletConfig().getServletContext().setAttribute("dbenv",env);
		userDB = env.getUserDatabase();
		channelDB = env.getChannelDatabase();
		System.out.println("Login Servlet Initialized succcessfully. BDB Store has been setup");
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		
		try {
			User userObj = (User) userDB.get(username);

			if (userObj == null) {
				pw.write("<html> <body> Account doesn't exist. <a href=\"/HW2/signup\"> Please sign up here!! </a></body></html>");
			}
			// successful login
			else if (userObj.getPassword().equals(password)) {
				// initialize session
				request.getSession().setAttribute("username", username);
				// pw.write("Successful Login");
				response.sendRedirect("/HW2/home");
				return;
			} else {
				pw.write("<html> <body> Login Failure. <a href=\"/HW2\"> Please try again</a></body></html>");
			}
		} catch (Exception e) {
			pw.write("<html> <body> <h1>Something went wrong. :( iiwCrawler is still in Beta</h1> </body></html>");
			e.printStackTrace();
		}

	}

	/**
	 * Displays a simple login form for user.
	 * @throws IOException 
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		response.setContentType("text/html");
		
		HttpSession session = request.getSession(false);
		if(session != null) {
			System.out.println("Redirecting to home");
			response.sendRedirect("/HW2/home");
			return;
		}
			
		//fetch all channel names from database
		String channels = "<ul>";
		try {
			channels += iterateChannels();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		channels += "</ul>";
		
		String htmlForm = "<!DOCTYPE html> " + "<html> " + "<body> "
				+ "<h3>CIS 455 Web Crawler</h3>"
				+ "<form action=\"\" method=\"POST\"> " + "User Name:<br> "
				+ "<input type=\"text\" name=\"username\"> " + "<br>"
				+ "Password:<br> "
				+ "<input type=\"password\" name=\"password\"><br> "
				+ "<input type=\"submit\" value=\"Login\"> "
				+ "</form><br><br> " + "<a href=\"/HW2/signup\"> Sign Up</a>"
				+ "<br/><br/><br/><h2>Channels</h2>"
				+ channels
				+ "</body> " + "</html>";
		
		
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(htmlForm);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public String iterateChannels() throws Exception {
		Serializer serializer = new Serializer();
		Cursor cursor = channelDB.getDatabase().openCursor(null, null);
		String result ="";
        try {

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
                Channel channelObj = (Channel) serializer.deserialize(foundData.getData());
                result += "<li><a target=\"_blank\" href=\"/HW2/displaychannel?channelname="+channelName+"\">"+channelName+"</a></li>"; 
            }
            cursor.close();            
        } catch (DatabaseException de) {
            System.err.println("Error accessing database." + de);
        } finally {
            // Cursors must be closed.
            cursor.close();
        }
        return result;
	}

}
