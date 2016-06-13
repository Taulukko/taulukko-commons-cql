package cql.lexicalparser;

import cql.Token;

public interface ConsumerToken {

	void accept(Token t) throws Exception;
}
