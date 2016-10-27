package test.edu.usc.iiw;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.usc.iiw.crawler.HTTPClient;
import edu.usc.iiw.xpathengine.XPathEngine;
import edu.usc.iiw.xpathengine.XPathEngineImpl;
import junit.framework.TestCase;

public class TestXPathEngineImpl extends TestCase {

	XPathEngine xpei;
	String[] xpaths;

	@Before
	public void setUp() throws Exception {
		xpei = new XPathEngineImpl();
	}

	@Test
	public void testIsValidBeforeSetPaths() {
		boolean isValid = xpei.isValid(0);
		// isValid returns false if called without setXPaths
		assertFalse(isValid);
	}

	@Test
	public void testIsValid() {
		try {
			String[] xpaths = { "/test[ a/b1[ c1[p]/d[p] ] /n1[a]/n2 [c2/d[p]/e[text()=\"/asp[&123(123*/]\"]]]",
					"/note/hello4/this[@val=\"text1\"]/that[@val=\"text2\"][something/else]" };
			xpei.setXPaths(xpaths);
			for (int i = 0; i < xpaths.length; i++) {
				assertTrue(xpei.isValid(i));
			}

			String[] temp = { /* random ']' */"/catalog]",
					/* random '[' */
					"/catalog[",
					/* missing start quote */
					"/catalog/cd[@title=Empire Burlesque\"]",
					/* missing @ on artist */
					"/catalog/cd[@title=\"Empire Burlesque\"][artist=\"Bob Dylan\"]",
					/* unmatched brackets: extra ']' */
					"/catalog/cd[@title=Empire Burlesque\"]]",
					/* unmatched brackets: extra ']' */
					"/catalog/cd[@year=\"1988\"][@price=\"9.90\"]/country[text()=\"UK\"]]",
					/* unmatched brackets: extra '[' */
					"/catalog/cd[[@title=Empire Burlesque\"]",
					/* unmatched brackets: extra '[' */
					"/catalog/cd[@year=\"1988\"][[@price=\"9.90\"]/country[text()=\"UK\"]",
					/* illegal start character */
					"/catalog/!badelem",
					/* illegal start character */
					"/@frenchbread/unicorns",
					/* illegal start character */
					"/abc/123bad", "/hello world", "/check(these)chars", "/xmlillegal", "/XMLillegal",
					/* illegal attribute name */
					"/abc/ab[@,illegalattribute=\"hello\"]",
					/* illegal attribute name */
					"/abc/ab[@<illegalattribute=\"hello\"]",
					/* text after close quote */
					"/abc/ab[text()=\"abc\"  pqr]",
					/* bad quote placememnt */
					"/abc/ab[@attname\"=\"abc\"]",
					/* no attname */
					"/abc/ab[@=\"hello\"]" };

			xpei.setXPaths(temp);
			for (int i = 0; i < 11; i++) {
				assertFalse(xpei.isValid(i));
			}
		} catch (Exception e) {

		}
	}

	@Test
	public void testEvaluate1() {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<d> " + "<child1>Child1"
				+ "<grandChild>GrandChild</grandChild>" + "</child1>" + "<e>someSu" + "</e>" + "<e>someSubstring"
				+ "<f>aaa" + "<foo>somet" + "</foo>" + "</f>" + "</e>" + "<e>someSubstri"
				+ "<f att=\"\123\">theEntireText" + "<foo>something" + "<anu>" + "<g3 att=\"10:0\">h\"i" + "</g3>"
				+ "</anu>" + "</foo>" + "<bar>" + "</bar>" + "<xyz>" + "</xyz>" + "</f>" + "</e>" + "</d>";
		DocumentBuilder db;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			Document doc = db.parse(is);
			String[] xpaths = {
					"/d/e[f/foo][contains(text(), \"someSub\")]/f[text()=\"theEntireText\"][foo[text()=\"something\"][anu[g3[@att = \"10:0\"][contains(text() , \"h\")]]]][bar]/xyz" };
			xpei.setXPaths(xpaths);
			for (boolean b : xpei.evaluate(doc))
				assertTrue(b);
		} catch (ParserConfigurationException e) {
			assertTrue(false);
		} catch (SAXException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testEvaluate2() {
		HTTPClient hcli;
		try {
			hcli = new HTTPClient("http://static.akame.cdn.moe/public/imas.xml", "GET");
			Document doc = hcli.getDocument();
			String[] xpaths = { "/imas/production[@name=\"765 Production\"]",
					"/imas/production[@name=\"765 Production\"]/idol[fn[text()=\"chihaya\"]]/age[text()=\"16\"]",
					"/imas/production[idol[fn[text()=\"chihaya\"]][ln[contains(text(),\"ki\")]]]/idol[ln[text()=\"hoshii\"]]",
					"/imas/production[idol/ln[text()=\"futami\"]]/idol/age[text() = \"21\"]",
					"/imas/production[idol/ln[text()=\"hidaka\"]]/idol/c" };
			xpei.setXPaths(xpaths);
			int i = 0;
			for (boolean b : xpei.evaluate(doc)) {
				if (i == 2)
					assertFalse(b);
				else
					assertTrue(b);
				i++;
			}
		} catch (IOException e) {
			assertTrue(false);
		} catch (ParserConfigurationException e) {
			assertTrue(false);
		} catch (SAXException e) {
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testXMLURL() throws Exception {
		HTTPClient hcli;
		try {
			hcli = new HTTPClient("http://www.w3schools.com/xpath/books.xml", "GET");
			Document doc = hcli.getDocument();
			String[] xpaths = { "/bookstore/book[@category=\"WEB\"][price[text()=\"39.95\"]]" };
			xpei.setXPaths(xpaths);
			for (boolean b : xpei.evaluate(doc)) {
				assertTrue(b);
			}
		} catch (IOException e) {
			assertTrue(false);
		} catch (ParserConfigurationException e) {
			assertTrue(false);
		} catch (SAXException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testHTMLURL() {
		HTTPClient hcli;
		try {
			// use HTTP as the default HTTP Protocol
			hcli = new HTTPClient("docs.oracle.com/javaee/7/api/javax/servlet/http/HttpServlet.html", "GET");
			Document doc = hcli.getDocument();
			String[] xpaths = {
					"/html/body/div[@class=\"contentContainer\"]/ul/li/a[@title=\"class or interface in java.lang\"][text()=\"java.lang.Object\"]" };
			xpei.setXPaths(xpaths);
			for (boolean b : xpei.evaluate(doc)) {
				assertTrue(b);
			}
		} catch (IOException e) {
			assertTrue(false);
		} catch (ParserConfigurationException e) {
			assertTrue(false);
		} catch (SAXException e) {
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testHTTPSURL() {
		HTTPClient hcli;
		try {
			hcli = new HTTPClient("https://dbappserv.cis.usc.edu/crawltest/nytimes/Africa.xml", "GET");
			Document doc = hcli.getDocument();
			String[] xpaths = {
					"/rss/channel/item[title[contains(text(),\"World\")]][description[text()=\"AFRICA.\"]]" };
			xpei.setXPaths(xpaths);
			for (boolean b : xpei.evaluate(doc)) {
				assertTrue(b);
			}
		} catch (IOException e) {
			assertTrue(false);
		} catch (ParserConfigurationException e) {
			assertTrue(false);
		} catch (SAXException e) {
			assertTrue(false);
		} catch (Exception e) {
			assertTrue(false);
		}
	}

}
