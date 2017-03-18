package cql;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import cql.lexicalparser.ConsumerToken;
import cql.lexicalparser.exceptions.CQLFormatException;
import cql.lexicalparser.exceptions.CQLReplaceException;

public class Token {

	private TokenType type = null;
	private String content = null;
	private String posContent = null;
	private List<Token> subTokens = new ArrayList<>();
	private Token after = null;
	private Token before = null;
	private String timeZoneGMT = "GMT-00";

	public Token(TokenType type) {
		this.type = type;
	}

	public Token(TokenType type, String timeZoneGMT) {
		this.type = type;
		this.timeZoneGMT = timeZoneGMT;
	}

	public String getTimeZoneGMT() {
		return timeZoneGMT;
	}

	public void setTimeZoneGMT(String defaultGMT) {
		this.timeZoneGMT = defaultGMT;
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

	public List<Token> flatTokenList() {
		final List<Token> ret = Arrays.asList(this);
		ret.addAll(subTokens);
		subTokens.forEach(token -> ret.addAll(token.flatTokenList()));
		return ret;
	}

	public Token clone() {

		Token ret = new Token(getType(), getTimeZoneGMT());

		ret.setContent(this.getContent());
		ret.setPosContent(this.getPosContent());

		for (Token token : subTokens) {
			ret.subTokens.add(token.clone());
		}

		return ret;
	}

	private String replace(TokenType type, ConsumerToken process, AtomicInteger index, int indexSearch)
			throws CQLReplaceException {
		CQLReplaceException lastError = new CQLReplaceException("Invalid Type in replace");

		if (type.equals(this.getType())) {

			int get = index.getAndIncrement();
			if (get == indexSearch) {

				try {
					process.accept(this);
				} catch (Exception e) {

					throw new CQLReplaceException(e);
				}

			}

			this.getSubTokens().clear();
			this.rebuild();
			return this.getContent();
		}

		this.getSubTokens().stream().forEach(t -> {

			try {
				t.replace(type, process, index, indexSearch);
			} catch (CQLReplaceException e) {
				lastError.addSuppressed(e);
			}

		});

		if (lastError.getSuppressed().length > 0) {
			throw lastError;
		}

		this.rebuild();
		return this.getContent();
	}

	public int count(TokenType type) {
		return count(this, type);
	}

	private int count(Token token, TokenType type) {
		int count = (token.getType().equals(type)) ? 1 : 0;

		count += token.getSubTokens().stream().mapToInt(t -> count(t, type)).sum();

		return count;
	}

	public String replaceAll(final TokenType token, final ConsumerToken process) throws CQLReplaceException {
		CQLReplaceException lastError = new CQLReplaceException("Invalid Type in replace");

		if (token.equals(this.getType())) {

			if (process != null) {
				try {
					process.accept(this);
				} catch (Exception e) {
					throw new CQLReplaceException(e);
				}
			}
			this.getSubTokens().clear();
			this.rebuild();
			return this.getContent();
		}

		this.getSubTokens().stream().forEach(t -> {

			try {
				t.replaceAll(token, process);
			} catch (CQLReplaceException e) {
				lastError.addSuppressed(e);
			}

		});

		if (lastError.getSuppressed().length > 0) {
			throw lastError;
		}

		this.rebuild();
		return this.getContent();
	}

	public String replace(TokenType token, ConsumerToken process, int index) throws CQLReplaceException {
		if (process == null) {
			return replace(token, (Object) null, index);

		}

		return replace(token, process, new AtomicInteger(0), index);

	}

	public String replace(TokenType token, Object newContent, int index) throws CQLReplaceException {

		return replace(token, t -> {
			try {
				t.setContent(format(t, newContent));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}, new AtomicInteger(0), index);

	}

	public String replace(Object newContent, int index) throws CQLReplaceException {

		return replace(TokenType.INJECT, newContent, index);

	}

	public String format(Token token, Object value) throws CQLFormatException {

		if (value == null) {
			return "NULL";
		}
		if (value instanceof String) {
			value = ((String) value).replace("'", "''");
			return "'" + value + "'";
		}
		if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte
				|| value instanceof Float || value instanceof Double || value instanceof Boolean) {
			return String.valueOf(value);
		}

		if (List.class.isAssignableFrom(value.getClass())) {
			StringBuffer json = new StringBuffer();
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;

			for (Object subvalue : list) {
				if (json.length() == 0) {
					json.append("[");
				} else {
					json.append(",");
				}
				json.append(format(token, subvalue));
			}
			json.append("]");
			return json.toString();
		}

		if (Set.class.isAssignableFrom(value.getClass())) {
			StringBuffer json = new StringBuffer();
			@SuppressWarnings("unchecked")
			Set<Object> list = (Set<Object>) value;

			for (Object subvalue : list) {
				if (json.length() == 0) {
					json.append("{");
				} else {
					json.append(",");
				}
				json.append(format(token, subvalue));
			}
			json.append("}");
			return json.toString();
		}

		if (Map.class.isAssignableFrom(value.getClass())) {
			StringBuffer json = new StringBuffer();
			@SuppressWarnings("unchecked")
			Map<Object, Object> list = (Map<Object, Object>) value;

			Set<Object> keys = list.keySet();
			 
			json.append("{");
			boolean first = true;
			for (Object key : keys) {
				Object subvalue = list.get(key);
				if (first) {
					first = false;
				} else {
					json.append(",");
				}
				json.append(format(token, key));
				json.append(":");
				json.append(format(token, subvalue));
			}
			json.append("}");
			return json.toString();
		}

		if (value instanceof LocalDate) {
			LocalDate dateTime = (LocalDate) value;

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			String formattedDateTime = dateTime.format(formatter);
			return "'" + formattedDateTime + "'";
		}

		if (value instanceof ZonedDateTime) {
			ZonedDateTime dateTime = (ZonedDateTime) value;

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ");

			String formattedDateTime = dateTime.format(formatter);
			return "'" + formattedDateTime + "'";
		}

		if (value instanceof Date) {
			Date date = (Date) value;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			sdf.setTimeZone(TimeZone.getTimeZone(timeZoneGMT));
			return "'" + sdf.format(date) + "'";
		}
		throw new CQLFormatException("Type unknown of " + value.getClass().getCanonicalName());
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
