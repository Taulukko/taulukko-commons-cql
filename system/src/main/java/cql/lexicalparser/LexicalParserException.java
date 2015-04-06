package cql.lexicalparser;

public class LexicalParserException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3469520316936806194L;
	private int code=0;
	
	public LexicalParserException (String message, int code)
	{
		super(message);
		this.code = code;
	}
	
	public LexicalParserException (String message)
	{
		super(message);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
