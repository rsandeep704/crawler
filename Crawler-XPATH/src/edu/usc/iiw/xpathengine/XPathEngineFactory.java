package edu.usc.iiw.xpathengine;

public class XPathEngineFactory {
	public static XPathEngine getXPathEngine() {
		return new XPathEngineImpl();
	}
}
