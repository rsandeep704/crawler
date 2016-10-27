/**
 * 
 */
package edu.usc.iiw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.usc.iiw.storage.Channel;
import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;
import edu.usc.iiw.storage.User;

/**
 * Servlet which allows users to create/ delete/ view channels
 * 
 * @author 
 *
 */
public class UserProfile extends HttpServlet {

	DBEnvWrapper dbew;
	DBWrapper channelDB;
	DBWrapper userDB;

	/**
	 * Setup the database connections
	 */
	public void initialize() {
		dbew = (DBEnvWrapper) this.getServletConfig().getServletContext().getAttribute("dbenv");
		userDB = dbew.getUserDatabase();
		channelDB = dbew.getChannelDatabase();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter pw = response.getWriter();
		// check the appropriate form request to handle
		String createButton = request.getParameter("createchannel");
		String deleteChannel = request.getParameter("deletechannel");
		HttpSession session = request.getSession(false);
		String message = "<html><body><h3>";
		if (session != null) {
			String username = (String) session.getAttribute("username");
			try {
				// fetch user object
				User user = (User) userDB.get(username);

				// check if request is to create a channel
				if (createButton != null) {
					String channelname = request.getParameter("channelname");
					String xslURL = request.getParameter("xslurl");
					String xpath = request.getParameter("xpath");

					if (channelname != "" && xpath != "" && xslURL != "") {
						// initialize the channel object
						Channel channel = new Channel();
						channel.setName(channelname);
						channel.setXslURL(xslURL);
						channel.setXpath(xpath.split(";"));

						// update channel database
						channelDB.insert(channelname, channel);

						// update user database
						user.addChannelName(channelname);
						userDB.insert(username, user);
						message += "Channel has been added successfully";
					} else {
						message += "Please enter all the fields.";
					}
				} else if (deleteChannel != null) { // check if request is to
													// delete the channel
					try {
						if (deleteChannel != "") {
							// delete the entry from channel database
							channelDB.delete(deleteChannel);

							// update user database
							user.removeChannelName(deleteChannel);
							userDB.insert(username, user);
							message += "Channel has been deleted successfully";
						} else {
							message += "Channel name is empty.";
						}
					} catch (Exception e) {
						message += "Something went wrong. Error deleting channel";
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				message += "Something went wrong. Please try again";
				e.printStackTrace();
			}
		} else {
			message += "Something went wrong.";
		}
		message += "<a href=\"HW2/home\">    Click Here!</a></h3>" + "</body></html>";
		pw.write(message);
	}

	/**
	 * Form to create channels
	 * 
	 * @throws IOException
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession(false);
		String username = null;
		if (session != null) {
			username = (String) session.getAttribute("username");
		} else {
			response.sendRedirect("/HW2/");
			return;
		}

		initialize();
		try {
			User user = (User) userDB.get(username);
			response.setContentType("text/html");
			String html = "<!DOCTYPE html> " + "<html> " + "<body> " + "<h1>Welcome " + username.toUpperCase() + "</h1>"
					+ "<hw3> <b>Create Channel</b></h3><br/>" + "<form action=\"\" method=\"POST\"> "
					+ "Channel Name:<br>" + "<input type=\"text\" name=\"channelname\"> " + "<br>" + "XSL URL:<br> "
					+ "<input type=\"text\" name=\"xslurl\"><br> " + "XPaths: ; seperated <br> "
					+ "<input type=\"text\" name=\"xpath\"><br> "
					+ "<input type=\"submit\" name=\"createchannel\" value=\"Create Channel\"> " + "</form> ";

			html += "<h3><b>My Channels: </b></h3><ul>";
			ArrayList<String> userChannels = user.getChannelNames();
			for (String channelName : userChannels) {
				html += "<a href=\"/HW2/displaychannel?channelname=" + channelName + "\">" + channelName + "</a>"
						+ "<form action = \"\" method=\"POST\">"
						+ "<button type=\"submit\" name=\"deletechannel\" value=\"" + channelName + "\">Delete "
						+ channelName + " </button>" + "</form><body>";
			}
			html += "</ul>";
			html += "<br/><a href=\"/HW2/logout\">Logout</a>";
			html += "</body></html>";

			PrintWriter pw;
			pw = response.getWriter();
			pw.write(html);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
