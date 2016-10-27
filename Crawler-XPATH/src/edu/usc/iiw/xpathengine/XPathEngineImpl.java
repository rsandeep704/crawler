package edu.usc.iiw.xpathengine;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathEngineImpl implements XPathEngine {

	String[] xpaths;
	boolean[] isMatch;
	Tokenizer[] tokenizers;
	ArrayList<ArrayList<Node>> matchingNodes;
	ArrayList<Node> temp;
	Tokenizer tzer;

	public XPathEngineImpl() {
		matchingNodes = new ArrayList<ArrayList<Node>>();
		temp = new ArrayList<Node>();
	}

	/**
	 * Store the xpaths to be validated and evaluated
	 */
	public void setXPaths(String[] s) {
		xpaths = new String[s.length];
		isMatch = new boolean[s.length];
		tokenizers = new Tokenizer[s.length];
		xpaths = s;
	}

	public int numXpaths() {
		return xpaths.length;
	}

	/**
	 * Tokenize the ith xpath and parse it to see if it is valid
	 */
	public boolean isValid(int i) {
		if (xpaths == null)
			return false;
		try {
			if(xpaths[i].equals("//"))
				return false;
			tokenizers[i] = new Tokenizer(xpaths[i]);
			RecursiveDescentParser rdp = new RecursiveDescentParser(
					tokenizers[i]);
			return rdp.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Evaluate the Document against all the "valid" xpaths
	 */
	public boolean[] evaluate(Document d) {
		int i = 0;
		for (String xpath : xpaths) {
			if(isValid(i)){
				try {
					tzer = new Tokenizer(xpath);
					temp.clear();
					if (recursiveParser(d, tzer.nextToken())) {
						isMatch[i] = true;
					}
					matchingNodes.add(temp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			i++;
		}
		return isMatch;
	}

	private boolean recursiveParser(Node parent, Token tok) {
		/*System.out.println("Current token list");
		for (Token t : tzer.getTokens()) {
			System.out.print(t.getValue());
		}
		System.out.println();*/
		if(parent == null && tok != null)
			return false;
		 //System.out.println("Node: " + parent.getNodeName() + " Token: " +  tok.getValue());
		 
		// base condition
		if (tok.type == Tokenizer.Type.DOLLAR) {
			return true;
		} 
		else if (tok.type == Tokenizer.Type.AXIS) {
			Token childNodeToken = tzer.nextToken();
			if (parent.hasChildNodes()) {
				NodeList children = parent.getChildNodes();
				boolean isMatch = false;
				for (int iChild = 0; iChild < children.getLength(); iChild++) {
					/*System.out.println("\n\tTrying to match "
							+ children.item(iChild).getNodeName()
							+ " with token " + childNodeToken.getValue()
							+ " First Child: "
							+ children.item(iChild).getFirstChild());*/
					if (children.item(iChild).getNodeName()
							.equals(childNodeToken.getValue())) {
						// System.out.println("\n\tXpath: "+childNodeToken.value+" Document: "+children.item(iChild).getNodeName());
						//System.out.println("Matched node " + children.item(iChild).getNodeName() + " with " + childNodeToken.getValue());
						isMatch = recursiveParser(children.item(iChild),
								tzer.nextToken());
						if (isMatch) {
							//System.out.println("matched node "+ children.item(iChild).getNodeName());
							temp.add(children.item(iChild));
							return true;
						} else {
							tzer.pushBack(1);
						}
					}
				}
				tzer.pushBack(1);
				return false;
			}
			else {
				tzer.pushBack(1);
			}
		} else if (tok.type == Tokenizer.Type.OPEN_BRACKET) {
			boolean result;
			Tokenizer temp = new Tokenizer(tzer);
			while (true) {
				result = test(parent, tzer.nextToken());
				//System.out.println("test condition result: " + result);
				// Consume close bracket
				/*if (tzer.nextToken().type != Tokenizer.Type.CLOSE_BRACKET)
					tzer.pushBack(1);*/
				// System.out.println("Next token:" +
				// tzer.nextToken().getValue());

				if (result == false) {
					// restore the tokens if the test condition failed
					tzer = new Tokenizer(temp);
					return false;
				}
				/*
				 * if (tzer.nextToken().type != Tokenizer.Type.OPEN_BRACKET) {
				 * tzer.pushBack(1); return false; }
				 */
				Token t = tzer.nextToken();
				switch (t.type) {
				case OPEN_BRACKET: /*
									 * Continue processing the next test
									 * condition
									 */
					break;
				case DOLLAR: /* End of xpath and all test conditions have passed */
					return true;
				/*case AXIS:
					return recursiveParser(parent, t);
				case CLOSE_BRACKET:*/
				default:
					return recursiveParser(parent, t);		
				
				}
			}

		} else if (tok.type == Tokenizer.Type.CLOSE_BRACKET) {
			return true;
		} else if (tok.type == Tokenizer.Type.NODENAME) {
			// check for $
			if (tok.getValue().equals(parent.getNodeName())) {
				//System.out.println("Nodename test Xpath: " + tok.getValue() + " Document: " + parent.getNodeName());
				return recursiveParser(parent, tzer.nextToken());
			} else
				return false;
		}
		return false;
	}

	private boolean test(Node node, Token testCondition) {
		//System.out.println("Test: " + node.getNodeName() + " Condition: " + testCondition.getValue());
		if (testCondition.type == Tokenizer.Type.ATTNAME) {
			String nameValue = testCondition.getValue().split("@")[1];
			String name = nameValue.split("=")[0];
			String value = nameValue.split("=")[1];
			value = value.replaceAll("\"", "");
			NamedNodeMap nnm = node.getAttributes();
			int len = (nnm != null) ? nnm.getLength() : 0;
			Node attr = nnm.getNamedItem(name);
			if (attr != null) {
				if (attr.getNodeValue().equals(value)) {
					//System.out.println("matched " + attr.getNodeName() + " of " + node.getNodeName());
					return true;
				}
			} else
				return false;
		} else if (testCondition.type == Tokenizer.Type.TEXT) {
			String value = testCondition.getValue().split("=")[1];
			value = value.replaceAll("\"", "");
			String text = "";
			if (node.getNodeType() == Node.TEXT_NODE)
				text = normalize(node.getNodeValue());
			else {
				if (node.hasChildNodes()) {
					NodeList children = node.getChildNodes();
					for (int iChild = 0; iChild < children.getLength(); iChild++) {
						Node textNode = children.item(iChild);
						if (textNode.getNodeType() == Node.TEXT_NODE) {
							text = normalize(textNode.getNodeValue());
							break;
						}
					}				
				}
			}
			//System.out.println(text + ":" + value);
			if (text != null && text.length() != 0 && text.equals(value)) {
				//System.out.println("matched node " + node.getNodeName() + " with value " + value);
				return true;
			} else
				return false;
		} else if (testCondition.type == Tokenizer.Type.CONTAINS) {
			String value = testCondition.getValue().split(",")[1];
			value = value.replaceAll("\"", "");
			value = value.replace(")", "");
			String text = "";
			if (node.getNodeType() == Node.TEXT_NODE)
				text = normalize(node.getNodeValue());
			else {
				if (node.hasChildNodes()) {
					NodeList children = node.getChildNodes();
					for (int iChild = 0; iChild < children.getLength(); iChild++) {
						Node textNode = children.item(iChild);
						if (textNode.getNodeType() == Node.TEXT_NODE) {
							text = normalize(textNode.getNodeValue());
							break;
						}
					}				
				}
			}
			//System.out.println(text + ":" + value);
			if (text != null && text.length() != 0 && text.contains(value)) {
				//System.out.println("matched node " + node.getNodeName() + " with value " + node.getNodeValue());
				return true;
			} else
				return false;
		} else if (testCondition.type == Tokenizer.Type.NODENAME) {
			NodeList children = node.getChildNodes();
			boolean isMatch = false;
			for (int iChild = 0; iChild < children.getLength(); iChild++) {
				if (children.item(iChild).getNodeName()
						.equals(testCondition.getValue())) {
					isMatch = recursiveParser(children.item(iChild),
							tzer.nextToken());
					if (isMatch) {
						//System.out.println("matched " + children.item(iChild) + "as child of " + node.getNodeName());
						return true;
					}
					tzer.pushBack(1);
				}
			}
			tzer.pushBack(1);
			return false;
		} else if (testCondition.type == Tokenizer.Type.CLOSE_BRACKET) {
			// all test cases must have passed
			return true;
		}
		return false;

	}

	protected String normalize(String s) {
		StringBuffer str = new StringBuffer();

		int len = (s != null) ? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '<': {
				str.append("&lt;");
				break;
			}
			case '>': {
				str.append("&gt;");
				break;
			}
			case '&': {
				str.append("&amp;");
				break;
			}
			case '"': {
				str.append("&quot;");
				break;
			}
			case '\'': {
				str.append("&apos;");
				break;
			}
			default: {
				str.append(ch);
			}
			}
		}

		return (str.toString());

	} // normalize(String):String

}
