package test.edu.usc.iiw;

import org.junit.Before;
import org.junit.Test;

import edu.usc.iiw.crawler.Crawler;
import edu.usc.iiw.crawler.XPathCrawler;
import edu.usc.iiw.storage.Channel;
import edu.usc.iiw.storage.CrawledURL;
import edu.usc.iiw.storage.DBEnvWrapper;
import edu.usc.iiw.storage.DBWrapper;
import junit.framework.TestCase;

public class TestXPathCrawler extends TestCase {

	XPathCrawler xpc;
	DBEnvWrapper dbew;
	DBWrapper crawledURLDB;
	DBWrapper channelDB;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSeedURLInDatabase() {
		dbew = new DBEnvWrapper("testdatabase1");
		String [] args = {"https://dbappserv.cis.usc.edu/crawltest/misc/weather.xml", "testdatabase1", "50"};
		try {
			XPathCrawler.main(args);
			crawledURLDB = dbew.getCrawledDatbase();
			CrawledURL crawledURL = (CrawledURL) crawledURLDB.get("https://dbappserv.cis.usc.edu/crawltest/misc/weather.xml");
			assertEquals(crawledURL.getUrl(),"https://dbappserv.cis.usc.edu/crawltest/misc/weather.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPoliteness() {
		dbew = new DBEnvWrapper("testdatabase2");
		String [] args = {"https://dbappserv.cis.usc.edu/crawltest/marie/private/middleeast.xml", "testdatabase2", "50"};
		try {
			XPathCrawler.main(args);
			crawledURLDB = dbew.getCrawledDatbase();
			CrawledURL crawledURL = (CrawledURL) crawledURLDB.get("https://dbappserv.cis.usc.edu/crawltest/marie/private/middleeast.xml");
			assertEquals(crawledURL.getUrl(),"https://dbappserv.cis.usc.edu/crawltest/marie/private/middleeast.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testChannel() {
		Crawler crawler;
		dbew = new DBEnvWrapper("testdatabase3");
		channelDB = dbew.getChannelDatabase();
		Channel channel = new Channel();
		try {
			channel.setName("mychannel");
			String [] xpaths = {"/rss/channel/title"};
			channel.setXpath(xpaths);
			channelDB.insert("mychannel", channel);			
			crawler = new Crawler("https://dbappserv.cis.usc.edu/crawltest/nytimes/Africa.xml", dbew, 1000, 40);
			crawler.run();
			
			Channel temp = (Channel) channelDB.get("mychannel");
			assertEquals(temp.getUrlMatch().get(0),"https://dbappserv.cis.usc.edu/crawltest/nytimes/Africa.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
