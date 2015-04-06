package cql;

import java.util.ArrayList;
import java.util.List;


public class Token {
	private TokenType type = null;
	private String content = null;
	private String posContent = null;
	private List<Token> subTokens = new ArrayList<Token>();
	private Token after = null;
	private Token before = null;
	 
	public TokenType getType() {
		return type;
	}
	public void setType(TokenType type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	 
	public String getPosContent() {
		return posContent;
	}
	public void setPosContent(String posContent) {
		this.posContent = posContent;
	}
	public List<Token> getSubTokens() {
		return subTokens;
	}
 
	public Token getAfter() {
		return after;
	}
	public void setAfter(Token after) {
		this.after = after;
	}
	public Token getBefore() {
		return before;
	}
	public void setBefore(Token before) {
		this.before = before;
	}
}
