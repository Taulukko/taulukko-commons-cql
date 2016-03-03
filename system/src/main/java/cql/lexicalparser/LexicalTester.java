package cql.lexicalparser;

import cql.Token;

public interface LexicalTester {
	public Token is(String text, boolean required) throws LexicalParserException;	
}
 