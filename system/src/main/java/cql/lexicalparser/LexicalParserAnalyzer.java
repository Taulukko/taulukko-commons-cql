package cql.lexicalparser;

import cql.Token;
import cql.TokenType;
import cql.lexicalparser.exceptions.LexicalParserException;

public interface LexicalParserAnalyzer {
	
	public Token check(String text, boolean required) throws LexicalParserException;
	
	public TokenType getType();

}
