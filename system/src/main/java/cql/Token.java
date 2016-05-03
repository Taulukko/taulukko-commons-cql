package cql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class Token {

	private TokenType type = null;
	private String content = null;
	private String posContent = null;
	private List<Token> subTokens = new ArrayList<Token>();
	private Token after = null;
	private Token before = null;

	public Token(TokenType type) {
		this.type = type;
	}

	public TokenType getType() {
		return type;
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

	public String toString() {
		return this.type.getName() + " [ " + this.content + " ]";
	}

	public Token clone() {

		Token ret = new Token(getType());

		ret.setContent(this.getContent());
		ret.setPosContent(this.getPosContent());

		for (Token token : subTokens) {
			ret.subTokens.add(token.clone());
		}

		return ret;
	}

	private String format(String value) {

		if (NumberUtils.isDigits(value)) {
			return value;
		} else if (value.startsWith("-") || value.startsWith("+")) {
			if (NumberUtils.isDigits(value.substring(1))) {
				return value;
			}
		}

		value = StringUtils.replace(value, "'", "''");
		
		return "'" + value + "'";
	}

	private String replace(TokenType type, String newContent,
			AtomicInteger index, int indexSearch) {

		if (type.equals(this.getType())) {
			this.setContent(newContent);
			this.getSubTokens().clear();
			this.rebuild();
			return this.getContent();
		}

		this.getSubTokens().stream().forEach(t -> {
			boolean sameType = t.getType().equals(type);

			if (!sameType) {
				t.replace(type, newContent, index, indexSearch);
				return;
			}

			int get = index.get();
			if (get > indexSearch) {
				return;
			} else if (get < indexSearch) {
				return;
			}

			t.setContent(format(newContent));
			index.incrementAndGet();
			return;

		});

		this.rebuild();
		return this.getContent();

	}

	public String replace(TokenType token, String newContent, int index) {

		return replace(token, newContent, new AtomicInteger(0), index);

	}

	public String replace(String newContent, int index) {

		return replace(TokenType.INJECT, newContent, index);

	}

	public String rebuild() {

		if (this.getSubTokens().size() == 0) {
			return this.content;
		}

		String oldContent = this.content;

		StringBuffer retBuffer = new StringBuffer();

		this.getSubTokens().stream().forEach(i -> {
			i.rebuild();
			retBuffer.append(i.getContent());
		});

		this.content = retBuffer.toString();

		return oldContent;

	}

}
