/**
 * 
 */
package edu.usc.iiw.xpathengine;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that tokenizes the string for recursive descent parsing as per the
 * given grammar.
 * 
 * @author 
 *
 */
public class Tokenizer {

	/*
	 * Tokenizer for the following Grammar: XPath -> axis step axis -> / step ->
	 * nodename test | nodename test axis step test -> [step] test test ->
	 * text() = "..." test -> contains(text(), "...") test -> @attname = "..."
	 * test -> epsilon
	 */
	/**
	 * Enum describing the various types of tokens in the grammar
	 * 
	 * @author iiw
	 *
	 */
	public enum Type {
		AXIS, STEP, NODENAME, TEST, CONTAINS, ATTNAME, TEXT, EPSILON, OPEN_BRACKET, CLOSE_BRACKET, DOLLAR
	};

	LinkedList<Token> tokens;
	private LinkedList<TokenInfo> tokenInfos;
	int tokenCount;
	private Stack<Token> removedTokens;

	/**
	 * regular expressions for matching the Tokens
	 */
	private void addRegexList() {
		add("/", Type.AXIS);
		add("\\[", Type.OPEN_BRACKET);
		add("\\]", Type.CLOSE_BRACKET);
		add("contains\\(text\\(\\),\"((?:\\\"|[^\"])*?)\"\\)", Type.CONTAINS);
		add("@[a-zA-Z_:][a-zA-Z0-9-_\\.:]*=\"((?:\\\"|[^\"])*?)\"",
				Type.ATTNAME);
		add("text\\(\\)=\"((?:\\\"|[^\"])*?)\"", Type.TEXT);
		// add("[^/\\[\\]]*",Type.NODENAME);
		add("[a-zA-Z_][a-zA-Z0-9-_\\.:]*", Type.NODENAME);
	}

	/**
	 * Copy constructor. This is used when we need a backup of tokens in
	 * Recursive Descent Parser
	 * 
	 * @param tzer
	 */
	public Tokenizer(Tokenizer tzer) {
		this.tokenCount = tzer.tokenCount;
		this.tokenInfos = (LinkedList<TokenInfo>) tzer.tokenInfos.clone();
		this.tokens = (LinkedList<Token>) tzer.tokens.clone();
		this.removedTokens = (Stack<Token>) tzer.removedTokens.clone();
	}

	private class TokenInfo {
		public final Pattern regex;
		public final Type type;

		public TokenInfo(Pattern regex, Type type) {
			super();
			this.regex = regex;
			this.type = type;
		}
	}

	public Tokenizer(String xpath) throws Exception {
		xpath = removeWhiteSpaces(xpath);
		//System.out.println("normalized xpath: " + xpath);
		tokenCount = 0;
		tokenInfos = new LinkedList<TokenInfo>();
		tokens = new LinkedList<Token>();
		removedTokens = new Stack<Token>();
		addRegexList();
		tokenize(xpath);
		tokens.add(new Token("DOLLAR", Type.DOLLAR));
	}

	/**
	 * Remove whitespaces in the xpath at all positions except in quoted strings
	 * 
	 * @param xpath
	 * @return
	 */
	private static String removeWhiteSpaces(String xpath) {
		char[] c = xpath.toCharArray();
		String normalizedString = "";
		boolean isQuotedString = false;
		int i = 0;
		while (i < c.length) {
			if (c[i] == ' ' && isQuotedString == false) {
				// ignore whitespace outside quoted strings
			} else if (c[i] == '\"' && c[i - 1] != '\\') {
				isQuotedString = !isQuotedString;
				normalizedString += c[i];
			} else {
				normalizedString += c[i];
			}
			i++;

		}
		return normalizedString;
	}

	/**
	 * Add the regex to be matched
	 * 
	 * @param regex
	 * @param token
	 */
	public void add(String regex, Type type) {
		tokenInfos
				.add(new TokenInfo(Pattern.compile("^(" + regex + ")"), type));
	}

	/**
	 * Tokenize the XPath as per the given grammar
	 * 
	 * @param str
	 *            - xpath
	 * @throws Exception
	 */
	public void tokenize(String str) throws Exception {
		String s = str.trim();
		tokens.clear();
		while (!s.equals("")) {
			boolean match = false;
			for (TokenInfo info : tokenInfos) {
				// try to match the string with the regex
				// System.out.println("trying to match "+s);
				// System.out.println("regex "+info.regex.pattern());
				Matcher m = info.regex.matcher(s);
				if (m.find()) {
					match = true;
					String tok = m.group(1).trim();
					//System.out.println("Matched: " + tok);
					//System.out.println("Type: " + info.type);
					// remove the token from the string
					s = m.replaceFirst("").trim();

					// check that nodename doesn't start with xml
					if (info.type == Tokenizer.Type.NODENAME) {
						if (tok.length() >= 3) {
							if (tok.substring(0, 3).equalsIgnoreCase("xml")) {
								throw new Exception(
										"Unexpected character in input: " + s);
							}
						}
					}
					tokens.add(new Token(tok, info.type));
					break;
				}
			}
			if (!match) {
				//throw new Exception("Unexpected character in input: " + s);
				return;
			}
		}
	}

	public Queue<Token> getTokens() {
		return tokens;
	}

	/**
	 * Number of tokens read so far. Useful to push back tokens in case of
	 * errors
	 * 
	 * @return
	 */
	public int getTokenCount() {
		return tokenCount;
	}

	/**
	 * TO-DO: HANDLE EPSILON get the next token
	 * 
	 * @return
	 */
	public Token nextToken() {
		tokenCount += 1;
		if (tokens.isEmpty()) {
			return null;
		}
		// push the removed tokens onto a stack
		Token temp = tokens.removeFirst();
		removedTokens.push(temp);
		return temp;
	}

	/**
	 * Pushes last i tokens that were removed back onto the queue.
	 * 
	 * @param i
	 */
	public void pushBack(int i) {
		for (int ctr = 0; ctr < i; ctr++) {
			Token temp = removedTokens.pop();
			tokens.addFirst(temp);
		}

	}
}
