/**
 * 
 */
package edu.usc.iiw.xpathengine;

import edu.usc.iiw.xpathengine.Tokenizer.Type;



/**
* class representing a token of the grammar
* @author iiw
*
*/
public class Token {	
	
		String value;
		Type type;
		
		/**
		 * 
		 * @param value Token string
		 * @param type Token type in grammar
		 */
		public Token(String value, Type type) {
			this.value = value;
			this.type = type;			
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}		
}

