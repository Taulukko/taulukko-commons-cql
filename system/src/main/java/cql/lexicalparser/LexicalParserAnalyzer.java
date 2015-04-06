package cql.lexicalparser;

import cql.Token;
import cql.TokenType;

public interface LexicalParserAnalyzer {
	
	public Token check(String text, boolean required) throws LexicalParserException;
	
	public TokenType getType();

}
