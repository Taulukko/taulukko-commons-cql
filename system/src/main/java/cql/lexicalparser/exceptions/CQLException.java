package cql.lexicalparser.exceptions;

public class CQLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8082905735451662886L;

	public CQLException(String message) {
		super(message);
	}

	public CQLException(Throwable t) {
		super(t);
	}

}
