package edu.usc.iiw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import edu.usc.iiw.xpathengine.HTTPClient;
import edu.usc.iiw.xpathengine.XPathEngine;
import edu.usc.iiw.xpathengine.XPathEngineFactory;

/**
 * Displays a form to the end user and takes the XPath and HTML/XML document URL
 * 
 * @author 
 *
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	/* TODO: Implement user interface for XPath engine here */

	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		String xpath = request.getParameter("xpath");
		String url = request.getParameter("url");
		PrintWriter pw = response.getWriter();
		XPathEngine xpei = XPathEngineFactory.getXPathEngine();
		Document doc;
		try {
			// assume that the xpaths are ; separated in the form input text box
			String[] xpaths = xpath.split(";");

			xpei.setXPaths(xpaths);
			pw.write("isValid\n");
			for (int i = 0; i < xpaths.length; i++) {
				boolean isValid = xpei.isValid(i);
				if (!isValid) {
					pw.write("Xpath not valid " + xpaths[i]);
					return;
				}
				pw.write(xpaths[i] + ":" + isValid + "\n");
			}
			// fetch the file using HTTP Client
			System.out.println("Fetching " + url + " from client");
			HTTPClient hcli = new HTTPClient(url);
			doc = hcli.getDocument();
			System.out.println("DOM Object created");
			int i = 0;
			for (boolean b : xpei.evaluate(doc)) {
				pw.write("\n" + xpaths[i++] + ":" + b + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Displays the HTML form and takes the XPath and HTML form as user input.
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		/* TODO: Implement user interface for XPath engine here */
		response.setContentType("text/html");
		String htmlForm = "<!DOCTYPE html> " + "<html> " + "<body> "
				+ "<h3>Use ; as a delimiter to separate multiple XPaths</h3>" + "<form action=\"\" method=\"POST\"> "
				+ "XPath:<br> " + "<input type=\"text\" name=\"xpath\"> " + "<br>" + "HTML/XML URL:<br> "
				+ "<input type=\"text\" name=\"url\"><br> " + "<input type=\"submit\" value=\"Match XPath\"> "
				+ "</form> " + "</body> " + "</html>";
		PrintWriter pw;
		try {
			pw = response.getWriter();
			pw.write(htmlForm);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
