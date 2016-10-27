/**
 * 
 */
package edu.usc.iiw.xpathengine;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author 
 *
 */
public class DOMParser {

	Document doc;
	/**
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * 
	 */
	public DOMParser(String document) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(document));

		this.doc = db.parse(is);
	}
	
	Document getDoc() {
		return doc;
	}
	
	void evaluate(Node node, Token tok) {
		if(tok.type == Tokenizer.Type.AXIS) {
			evaluate(node, tok);
		}
	}
	
	void test(Node node, Token tok) {
		
	}

}
