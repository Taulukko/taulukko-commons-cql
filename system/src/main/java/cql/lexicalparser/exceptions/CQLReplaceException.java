package cql.lexicalparser.exceptions;

public class CQLReplaceException extends CQLException {

	private static final long serialVersionUID = 3970497070737159900L;

	public CQLReplaceException(Throwable t) {
		super(t);
	}

	public CQLReplaceException(String message) {
		super(message);
	}

}
