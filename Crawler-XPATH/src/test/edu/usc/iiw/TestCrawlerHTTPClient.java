/**
 * 
 */
package test.edu.usc.iiw;

import org.junit.Before;
import org.junit.Test;

import edu.usc.iiw.crawler.HTTPClient;
import junit.framework.TestCase;

/**
 * @author iiw
 *
 */
public class TestCrawlerHTTPClient extends TestCase {

	HTTPClient client;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/** Check if autoredirection in https is turned off and check if the redirected link is proper 
	 * Test method for {@link edu.usc.iiw.crawler.HTTPClient#redirectLink()}.
	 * @throws Exception 
	 */
	@Test
	public void testRedirectLink() throws Exception {
		client = new HTTPClient("https://dbappserv.cis.usc.edu/crawltest/marie/private", "HEAD");
		client.sendRequest();
		assertEquals(client.getRedirectLink(),"https://dbappserv.cis.usc.edu/crawltest/marie/private/");
		
	}
	
	@Test
	public void testGetDocumentAfterGetBody() {
		try {
			client = new HTTPClient("https://dbappserv.cis.usc.edu/crawltest/misc/weather.xml", "HEAD");
		} catch (Exception e) {
			assert(false);
		}
		client.sendRequest();
		try {
			client.getBody();
			// can't get document once you have read the body. reader is no longer available
			client.getDocument();
		} catch (Exception e) {
			assert(true);
		}
		
		
	}

}
