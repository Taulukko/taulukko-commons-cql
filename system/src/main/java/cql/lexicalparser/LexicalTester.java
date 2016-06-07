package cql.lexicalparser;

import cql.Token;
import cql.lexicalparser.exceptions.LexicalParserException;

public interface LexicalTester {
	public Token is(String text, boolean required) throws LexicalParserException;	
}
 