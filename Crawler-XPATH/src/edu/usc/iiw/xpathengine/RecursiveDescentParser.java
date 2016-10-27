package edu.usc.iiw.xpathengine;

import edu.usc.iiw.xpathengine.Tokenizer.Type;

/**
 * Parses the tokenized xpath tokens as per the given grammar
 * 
 * @author 
 *
 */
public class RecursiveDescentParser {

	/*
	 * Tokenizer for the following Grammar: XPath -> axis step axis -> / step ->
	 * nodename [test]* (axis step) ? test -> step test -> text() = "..." test
	 * -> contains(text(), "...") test -> @attname = "..." eg:
	 * /this/that[something/else]
	 */

	/**
	 * The recursive descent parser has a function for every non-terminal
	 */
	public RecursiveDescentParser(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	Tokenizer tokenizer;
	static Token lookAhead;

	/**
	 * Main method which starts parsing the Tokens of the XPath
	 * 
	 * @return true if xpath was conformant to the given Grammar
	 * @throws Exception
	 */
	public boolean parse() throws Exception {
		lookAhead = tokenizer.nextToken();
		xpath();
		if (tokenizer.nextToken().getType() != Tokenizer.Type.DOLLAR)
			return false;
		return true;
	}

	/**
	 * Below are functions for each of the Non-terminals in the grammar which
	 * are called recursively
	 * 
	 * @throws Exception
	 */
	void xpath() throws Exception {
		if (lookAhead.getType() == Tokenizer.Type.AXIS) {
			match(Tokenizer.Type.AXIS);
			step(false);
		}
	}

	void step(boolean fromTest) throws Exception {
		if (lookAhead.getType() == Tokenizer.Type.NODENAME) {
			match(Tokenizer.Type.NODENAME);
			if (fromTest && lookAhead.getType() == Tokenizer.Type.CLOSE_BRACKET) {
				return;
			}
			while (true) {
				if (lookAhead.getType() != Tokenizer.Type.OPEN_BRACKET)
					break;
				match(Tokenizer.Type.OPEN_BRACKET);
				test();
				match(Tokenizer.Type.CLOSE_BRACKET);
			}
			if (fromTest && lookAhead.getType() == Tokenizer.Type.CLOSE_BRACKET) {
				return;
			}
			if (lookAhead != null
					&& lookAhead.getType() != Tokenizer.Type.DOLLAR) {
				match(Tokenizer.Type.AXIS);
				step(fromTest);
			} else if (lookAhead.getType() == Tokenizer.Type.DOLLAR
					&& !fromTest) {
				tokenizer.pushBack(1);
			}
		}
	}

	void test() throws Exception {
		if (lookAhead.getType() == Tokenizer.Type.CONTAINS) {
			match(Tokenizer.Type.CONTAINS);
			return;
		} else if (lookAhead.getType() == Tokenizer.Type.ATTNAME) {
			match(Tokenizer.Type.ATTNAME);
			return;
		} else if (lookAhead.getType() == Tokenizer.Type.TEXT) {
			match(Tokenizer.Type.TEXT);
			return;
		} else {
			step(true);
		}
	}

	/**
	 * Check if the current token type matches the expected Token Type from the
	 * Grammar
	 * 
	 * @param type
	 * @throws Exception
	 */
	void match(Type type) throws Exception {
		if (type == lookAhead.getType()) {
			//System.out.println(" " + lookAhead.getValue());
			lookAhead = tokenizer.nextToken();
		} else {
			throw new Exception("Parse failed; " + type + "and lookAhead: "
					+ lookAhead.getValue());
		}
	}
}
