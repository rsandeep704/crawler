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

/**
 * @author 
 *
 */
public class LogoutServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession httpSession = request.getSession(false);
		if(httpSession != null) {
			httpSession.invalidate();
			PrintWriter pw = response.getWriter();
			String html = "<html><body>You have been successfully logged out. <a href=\"/HW2/\">Click here!</a></body></html>";
			pw.write(html);
		} else {
			response.sendRedirect("/HW2/");
		}
		
	}
	

}
