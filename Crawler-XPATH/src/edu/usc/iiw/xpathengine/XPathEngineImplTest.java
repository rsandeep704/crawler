/**
 * 
 */
package edu.usc.iiw.xpathengine;

import static org.junit.Assert.fail;

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

/**
 * @author iiw
 *
 */
public class XPathEngineImplTest {
	
	XPathEngineImpl xpei;
	String [] xpaths;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		xpei = new XPathEngineImpl();
		xpaths = new String[10];
		xpaths[0] = "/foo/bar/xyz";
		/*xpaths[1] = "/foo/bar[@att=\"123\"]";
		xpaths[2] = "/d/e/f[foo[text()=\"something\"]][bar]";
		xpaths[3] = "/blah[anotherElement]";
		xpaths[4] = "/a/b/c[text() = \"whiteSpacesShouldNotMatter\"]";
		xpaths[5] = "/a/b/c[text()=\"theEntireText\"]";
		xpaths[6] = "/xyz/abc[contains(text(),\"someSubstring\")]";*/
		xpei.setXPaths(xpaths);
	}

	/**
	 * Test method for {@link edu.usc.iiw.xpathengine.XPathEngineImpl#isValid(int)}.
	 */
	@Test
	public void testIsValid() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link edu.usc.iiw.xpathengine.XPathEngineImpl#evaluate(org.w3c.dom.Document)}.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public void testEvaluate() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		String xml = "<foo> "+
					 	"<child1>Child1</child1>" +
					 		"<grandChild>GrandChild</grandchild>"+
					 	"<bar>Bar"
					 		+ "<xyz>XYZ</xyz>"+
					 	"</bar>" +
					 "</foo>";
		xml = "<this> "+
					 "<that> "+
					 	"<something>Something"+
					 		"<else>Else</else>"+
					 	"</something>"+
					 "</that>" +
			  "</this>";
		
		is.setCharacterStream(new StringReader(xml));
		Document doc = db.parse(is);
		int i = 0;
		for(boolean b: xpei.evaluate(doc))
			System.out.println(xpaths[i] +":"+b+"\n");
	}

}
