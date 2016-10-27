/**
 * 
 */
package test.edu.usc.iiw;

import java.net.URLDecoder;

import org.junit.Before;
import org.junit.Test;

import edu.usc.iiw.crawler.HTTPClient;
import edu.usc.iiw.crawler.info.RobotsTxtInfo;
import junit.framework.TestCase;

/** Tests if robots.txt is being parsed appropriately
 * @author 
 *
 */
public class TestRobotsTxtInfo extends TestCase {

	HTTPClient client;
	RobotsTxtInfo robot;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testStarUserAgent() throws Exception {
		client = new HTTPClient("http://en.wikipedia.org/robots.txt","GET");
		client.sendRequest();
		robot = new RobotsTxtInfo(client.getReader());
		assertEquals(robot.getUserAgent(),"*");
		
	}
	
	@Test
	public void testiiwCrawlerUserAgent() throws Exception {
		client = new HTTPClient("https://dbappserv.cis.usc.edu/robots.txt","GET");
		client.sendRequest();
		robot = new RobotsTxtInfo(client.getReader());
		assertEquals(robot.getUserAgent(),"iiwcrawler");
	}
	
	@Test
	public void testNoRobotsTxt() throws Exception {

	}
	
	@Test
	public void checkURLDecoded() throws Exception {
		client = new HTTPClient("http://en.wikipedia.org/robots.txt","GET");
		client.sendRequest();
		robot = new RobotsTxtInfo(client.getReader());
		assertEquals(robot.getAllowedLinks().get(0),URLDecoder.decode(robot.getAllowedLinks().get(0), "UTF-8"));
	}
	@Test
	public void checkNoWildCharacterLinks() throws Exception {
		client = new HTTPClient("https://github.com/robots.txt","GET");
		client.sendRequest();
		robot = new RobotsTxtInfo(client.getReader());
		for(String link: robot.getAllowedLinks())
			assertFalse(link.matches("[$?*]"));
		for(String link: robot.getDisallowedLinks())
			assertFalse(link.matches("[$?*]"));
	}
	
	

}
