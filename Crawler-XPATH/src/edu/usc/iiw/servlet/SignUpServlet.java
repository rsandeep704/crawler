/**
 * 
 */
package edu.usc.iiw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;
import edu.usc.iiw.storage.User;

/** SignUpServlet servlet which allows users to sign up with 
 * @author 
 *
 */
public class SignUpServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		String contextParam = getServletContext().getInitParameter("BDBstore");
		System.out.println(contextParam);
		//DBEnvWrapper env = new DBEnvWrapper(contextParam);
		DBEnvWrapper env = (DBEnvWrapper) this.getServletConfig().getServletContext().getAttribute("dbenv");
		PrintWriter pw = response.getWriter();
		String username = request.getParameter("username");
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		DBWrapper userDB = env.getUserDatabase();
		try {
			User userObj = new User();
			userObj.setName(name);
			userObj.setPassword(password);
			User userObjDB = (User) userDB.get(username);
			
			// user doesn't exist in the database
			if( userObjDB == null) {
				userDB.insert(username, userObj);
				pw.write("<html><body>You have successfully signed up. Please login <a href=\"HW2/\">here</a></body></html>");
			} else {
				pw.write("<html><body><h1>Error creating account. Account already exists</h1></body></html>");
			}
		} catch (Exception e) {
			pw.write("<html> <body> Account creation Failure. <a href=\"/HW2/signup\"> Please try again</a> </body> </hmtl>");
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Displays a simple signup form for user.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("text/html");
		String htmlForm = "<!DOCTYPE html> " + "<html> " + "<body> "
				+ "<h3>Sign up for CIS 455 Web Crawler</h3>"
				+ "<form action=\"\" method=\"POST\"> " + "User Name:<br> "
				+ "<input type=\"text\" name=\"username\"> " + "<br>"
				+ "Name:<br> "
				+ "<input type=\"text\" name=\"name\"><br> "
				+ "Password:<br> "
				+ "<input type=\"password\" name=\"password\"><br> "
				+ "<input type=\"submit\" value=\"Create Account\"> " + "</form> "
				+ "</body> " + "</html>";
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(htmlForm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
