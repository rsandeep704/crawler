/**
 * 
 */
package edu.usc.iiw.servlet;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.usc.iiw.storage.Channel;
import edu.usc.iiw.storage.CrawledURL;
import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;

/** Servlet which displays the xml for the corresponding channel with the embedded xsl styleshet
 * @author 
 *
 */
public class DisplayChannel extends HttpServlet {

	DBEnvWrapper env;
	DBWrapper channelDB;
	DBWrapper crawledURLDB;
	
	public void initializeDB() {
		env = (DBEnvWrapper) this.getServletConfig().getServletContext().getAttribute("dbenv");
		// fetch the channel from the database
		channelDB = env.getChannelDatabase();
		crawledURLDB = env.getCrawledDatbase();
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		initializeDB();
		response.setContentType("text/xml");
		try {
			PrintWriter pw = response.getWriter();
			String channelName = request.getParameter("channelname");			
			Channel channel  = (Channel) channelDB.get(channelName);
			if(channel != null) {
				String xml ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
				xml += "<?xml-stylesheet type=\"text/xsl\" href=\""+ channel.getXslURL() +"\"?>\n";
				xml += 	"<documentcollection>\n";
				ArrayList<String> urlMatches = channel.getUrlMatch();
				System.out.println(urlMatches);
				for(String url: urlMatches) {
					CrawledURL crawledURL = (CrawledURL) crawledURLDB.get(url);
					if(crawledURL != null) {
						Date lastCrawlTime = crawledURL.getLastCrawlTime();
						SimpleDateFormat localTimeFormat = new SimpleDateFormat("HH:mm:ss");
					    String time = localTimeFormat.format(lastCrawlTime);
					    SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd");
					    String date = localDateFormat.format(lastCrawlTime);
					    xml += "<document crawled=\"" + time+"T"+date +"\" location=\""+url+"\">\n";
						String xmlContent = crawledURL.getContent();
						xml += xmlContent.replaceFirst("<\\?xml(.*?)>", "");
						xml += "</document>\n";						
					}
				}
				xml += "</documentcollection>\n";
				pw.write(xml);
			} else {
				pw.write("Channel not found :(");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
