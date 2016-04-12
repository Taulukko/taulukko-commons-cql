package cql.lexicalparser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import cql.Token;
import cql.TokenType;

/*
 * That class implements a Lexical Analyzer and a simple Parser (Sintatical Analyzer).
 *
 * Lexical Analyzer : Scanner the string and split in a set of tokens.
 * 
 * Parser : Sort the tokens in a hierarquecal tree.
 * 
 *
 * */

public class LexicalParser {

	private static final Set<String> RESERVED_WORDS = new HashSet<String>(
			Arrays.asList("ADD", "ALL", "ALLOW", "ALTER", "AND", "ANY",
					"APPLY", "AS", "ASC", "ASCII", "AUTHORIZE", "BATCH",
					"BEGIN", "BIGINT", "BLOB", "BOOLEAN", "BY", "CLUSTERING",
					"COLUMNFAMILY", "COMPACT", "COUNT", "COUNTER",
					"CONSISTENCY", "CREATE", "DECIMAL", "DELETE", "DESC",
					"DOUBLE", "DROP", "EACH_QUORUM", "FILTERING", "FLOAT",
					"FROM", "GRANT", "IN", "INDEX", "INET", "INSERT", "INT",
					"INTO", "KEY", "KEYSPACE", "KEYSPACES", "LEVEL", "LIMIT",
					"LIST", "LOCAL_ONE", "LOCAL_QUORUM", "MAP", "MODIFY",
					"RECURSIVE", "SUPERUSER", "OF", "ON", "ONE", "ORDER",
					"PERMISSION", "PERMISSIONS", "PRIMARY", "QUORUM", "RENAME",
					"REVOKE", "SCHEMA", "SELECT", "SET", "STORAGE",
					"SUPERUSER", "TABLE", "TEXT", "TIMESTAMP", "TIMEUUID",
					"TO", "TOKEN", "THREE", "TRUNCATE", "TTL", "TWO", "TYPE",
					"UNLOGGED", "UPDATE", "USE", "USER", "USING", "UUID",
					"VALUES", "VARCHAR", "VARINT", "WITH", "WRITETIME", "WHERE"));

	// <SYMBOL> ::= = | + | - | / | * | ( | ) | { | } | , [ | ]
	private static final Set<Character> SYMBOLS = new HashSet<Character>(
			Arrays.asList('=', '+', '-', '/', '*', '(', ')', '{', '}', ',',
					'[', ']'));

	public LexicalParser() {
	}

	private String buildDoubleQuotedScape(String text) {
		return (text.startsWith("\"\"")) ? "\"\"" : null;
	}

	private void buildLexicalParserException(Token token, String text)
			throws LexicalParserException {
		throw new LexicalParserException("Invalid "
				+ token.getType().getName().toUpperCase() + " in [" + text
				+ "]");
	}

	private void buildLexicalParserException(Token token, String text,
			String reason) throws LexicalParserException {
		reason = (reason == null) ? "" : ":" + reason;
		throw new LexicalParserException("Invalid "
				+ token.getType().getName().toUpperCase() + " in [" + text
				+ "] " + reason);
	}

	private String buildSingleQuotedScape(String text) {
		return (text.startsWith("''")) ? "''" : null;
	}

	private String consume(String text, char... stop) {
		StringBuffer ret = new StringBuffer();
		for (int index = 0; index < text.length(); index++) {
			for (char stopItem : stop) {
				if (text.charAt(index) == stopItem) {
					return ret.toString();
				}
			}
			ret.append(text.charAt(index));
		}
		return ret.toString();
	}

	// <ACESSOR> ::= .
	public Token isAcessor(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.ACESSOR);

		if (text.length() == 0 || !text.startsWith(".")) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(".");
		token.setPosContent(text.substring(1));
		return token;
	}

	// <AND> ::= u(AND)
	public Token isAnd(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.AND, "AND", text, false, null, required);

	}

	// <ASTERISK> ::= *
	private Token isAsterisk(String text, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.ASTERISK, "*", text, required);

	}

	// <CHARS> ::= ^<EMPTY>[<CHARS>](a-Z0-9_)
	public Token isChars(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.CHARS);

		StringBuffer content = new StringBuffer();

		if (text.isEmpty()) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		for (int index = 0; index < text.length(); index++) {
			Character character = text.charAt(index);
			boolean valid = Character.isAlphabetic(text.codePointAt(index))
					|| Character.isDigit(text.codePointAt(index))
					|| character == '_';

			if (!valid) {
				if (content.length() > 0) {
					break;
				}
				if (required) {
					buildLexicalParserException(token, text);
				}
				return null;
			}

			content.append(character);
		}

		String contentStr = content.toString();

		token.setContent(contentStr);
		token.setPosContent(text.substring(contentStr.length()));
		return token;
	}

	// <DOUBLE DOT> ::= :
	public Token isDoubleDot(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.DOUBLE_DOT, ":", text, required);

	}

	// <COMA> ::= ,
	public Token isComma(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.COMMA, ",", text, required);

	}

	// <COMMAND> ::= <INSERT COMMAND> | <OTHER COMMAND>
	public Token isCommand(final String text, final boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.COMMAND);

		Token leftToken = null;

		Token insertCommand = isInsertCommand(text, false);
		if (insertCommand == null) {
			Token otherCommands = isOtherCommands(text, required);
			if (otherCommands == null) {
				return null;
			}

			updateNeighbors(leftToken, otherCommands);
			leftToken = otherCommands;
			token.getSubTokens().add(leftToken);

		} else {

			updateNeighbors(leftToken, insertCommand);
			leftToken = insertCommand;
			token.getSubTokens().add(leftToken);

		}

		String content = text.substring(0, text.length()
				- leftToken.getPosContent().length());

		token.setContent(content);

		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <OTHER COMMAND> ::= ^<WHERE> <RESERVED WORDS> [<SPACES>] (<SELECTOR
	// BLOCK> | ( [<SYMBOL>] [<SPACES>] [<LITERAL>] )[<SPACES>] )
	// [[<SPACES>]<OTHER COMMAND>]
	public Token isOtherCommands(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.OTHER_COMMAND);

		Token leftToken = null;

		Token where = isWhere(text, false);
		if (where != null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token insertCommand = isReservedWord(text, required);
		if (insertCommand == null) {
			return null;

		}

		updateNeighbors(leftToken, insertCommand);
		leftToken = insertCommand;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		// <OTHER COMMAND> ::= <RESERVED WORDS> [<SPACES>] (<SELECTOR BLOCK> | (
		// [<SYMBOL>] [<SPACES>] [<LITERAL>] [<SPACES>] ) [<OTHER COMMAND>]

		Token tokenSelectorBlock = isSelectorBlock(leftToken.getPosContent(),
				false);
		if (tokenSelectorBlock == null) {
			Token tokenSymbol = isSymbol(leftToken.getPosContent(), false);
			if (tokenSymbol != null) {
				updateNeighbors(leftToken, tokenSymbol);
				leftToken = tokenSymbol;
				token.getSubTokens().add(leftToken);
			}

			tokenSpaces = isSpaces(leftToken.getPosContent(), false);
			if (tokenSpaces != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);
			}

			Token tokenLiteral = isLiteral(leftToken.getPosContent(), false);
			if (tokenLiteral != null) {
				updateNeighbors(leftToken, tokenLiteral);
				leftToken = tokenLiteral;
				token.getSubTokens().add(leftToken);
			}

			tokenSpaces = isSpaces(leftToken.getPosContent(), false);
			if (tokenSpaces != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);
			}
		} else {
			updateNeighbors(leftToken, tokenSelectorBlock);
			leftToken = tokenSelectorBlock;
			token.getSubTokens().add(leftToken);
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token otherCommands = isOtherCommands(tokenSpaces.getPosContent(),
					false);
			if (otherCommands != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, otherCommands);
				leftToken = otherCommands;
				token.getSubTokens().add(leftToken);
			}
		}

		String content = text.substring(0, text.length()
				- leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <INSERT COMMAND> ::= <INSERT> <SPACES> [<INTO> <SPACES>] <SELECTOR ITEM>
	// [<SPACES>] <START_PARAMETERS> [<SPACES>] <SELECTOR BLOCK> [<SPACES>]
	// <END_PARAMETERS> [<SPACES>] <VALUES> [<SPACES>] <START_PARAMETERS>
	// [<SPACES>] [<SELECTOR BLOCK>] [<SPACES>] <END_PARAMETERS>
	public Token isInsertCommand(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.INSERT_COMMAND);

		Token leftToken = null;

		Token tokenInsert = isInsert(text, required);
		if (tokenInsert == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenInsert);
		leftToken = tokenInsert;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenInto = isInto(leftToken.getPosContent(), false);
		if (tokenInto != null) {
			updateNeighbors(leftToken, tokenInto);
			leftToken = tokenInto;
			token.getSubTokens().add(tokenInto);

			tokenSpaces = isSpaces(leftToken.getPosContent(), false);
			if (tokenSpaces == null) {
				return null;
			}
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token selectorBlock = isSelectorItem(leftToken.getPosContent(),
				required);
		if (selectorBlock == null) {
			return null;
		}

		updateNeighbors(leftToken, selectorBlock);
		leftToken = selectorBlock;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token startParams = isStartParameters(leftToken.getPosContent(),
				required);
		if (startParams == null) {
			return null;
		}

		updateNeighbors(leftToken, startParams);
		leftToken = startParams;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		selectorBlock = isSelectorBlock(leftToken.getPosContent(), required);
		if (selectorBlock == null) {
			return null;
		}
		updateNeighbors(leftToken, selectorBlock);
		leftToken = selectorBlock;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token endParams = isEndParameters(leftToken.getPosContent(), required);
		if (endParams == null) {
			return null;
		}
		updateNeighbors(leftToken, endParams);
		leftToken = endParams;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenValues = isValues(leftToken.getPosContent(), required);
		if (tokenValues == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenValues);
		leftToken = tokenValues;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		startParams = isStartParameters(leftToken.getPosContent(), required);
		if (startParams == null) {
			return null;
		}
		updateNeighbors(leftToken, startParams);
		leftToken = startParams;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		selectorBlock = isSelectorBlock(leftToken.getPosContent(), required);
		if (selectorBlock == null) {
			return null;
		}
		updateNeighbors(leftToken, selectorBlock);
		leftToken = selectorBlock;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		endParams = isEndParameters(leftToken.getPosContent(), required);
		if (endParams == null) {
			return null;
		}
		updateNeighbors(leftToken, endParams);
		leftToken = endParams;
		token.getSubTokens().add(leftToken);

		String content = text.substring(0, text.length()
				- leftToken.getPosContent().length());
		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <COMMAND> ::= <RESERVED WORDS> [<SPACES>] [<START_PARAMETERS><SPACES>]¹
	// [<SELECTOR BLOCK>] [<SPACES><END_PARAMETERS>]¹ [<SPACES>] [<SYMBOL>]
	// [<SPACES>] [<LITERAL>] [<SPACES>] [<COMMAND>]
	public Token isOldCommand(final String text, final boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.COMMAND);

		Token leftToken = null;

		Token tokenReservedWords = isReservedWords(text, required);
		if (tokenReservedWords == null) {
			return null;
		}

		leftToken = tokenReservedWords;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token selectorBlock = isSelectorBlock(leftToken.getPosContent(), false);
		if (selectorBlock != null) {
			updateNeighbors(leftToken, selectorBlock);
			leftToken = selectorBlock;
			token.getSubTokens().add(leftToken);
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenSymbol = isSymbol(leftToken.getPosContent(), false);
		if (tokenSymbol != null) {
			updateNeighbors(leftToken, tokenSymbol);
			leftToken = tokenSymbol;
			token.getSubTokens().add(leftToken);
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenLiteral = isLiteral(leftToken.getPosContent(), false);
		if (tokenLiteral != null) {
			updateNeighbors(leftToken, tokenLiteral);
			leftToken = tokenLiteral;
			token.getSubTokens().add(leftToken);
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenAnotherComand = isCommand(leftToken.getPosContent(), false);
		if (tokenAnotherComand != null) {
			updateNeighbors(leftToken, tokenAnotherComand);
			leftToken = tokenAnotherComand;
			token.getSubTokens().add(leftToken);
		}

		String content = text.substring(0, text.length()
				- leftToken.getPosContent().length());
		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <CONDITION> ::= <WHERE> <CONDITIONS>
	public Token isCondition(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.CONDITION);
		Token left = null;
		String content = text;

		Token tokenWhere = isWhere(text, required);
		left = tokenWhere;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		Token tokenConditions = isConditions(left.getPosContent(), required);
		updateNeighbors(left, tokenConditions);
		left = tokenConditions;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <CONDITION-ITEM>::= <SELECTOR ITEM>[<SPACES>]<SYMBOL>[<SPACES>]<SELECTOR
	// ITEM>
	public Token isConditionItem(final String text, final boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.CONDITION_ITEM);

		Token tokenSelectorItem = isSelectorItem(text, required);

		String content = text;

		if (tokenSelectorItem == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token left = tokenSelectorItem;
		token.getSubTokens().add(left);

		Token tokenSpaces = isSpaces(left.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;
			token.getSubTokens().add(left);
		}

		Token tokenSymbol = isSymbol(left.getPosContent(), false);
		if (tokenSymbol == null) {
			if (required) {
				buildLexicalParserException(token, left.getPosContent(),
						"Expected SYMBOL");
			}
			return null;
		}

		updateNeighbors(left, tokenSymbol);
		left = tokenSymbol;
		token.getSubTokens().add(left);

		tokenSpaces = isSpaces(left.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;

			token.getSubTokens().add(left);
		}

		Token tokenSelectorItem2 = isSelectorItem(left.getPosContent(), false);
		if (tokenSelectorItem2 == null) {
			return null;
		}

		updateNeighbors(left, tokenSelectorItem2);
		left = tokenSelectorItem2;
		token.getSubTokens().add(left);

		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <CONDITIONS> ::= <SPACES><CONDITION-ITEM>[<SPACES> <JOIN
	// CONDITION><CONDITIONS>]
	public Token isConditions(final String text, final boolean required)
			throws LexicalParserException {
		Token left = null;
		Token token = new Token(TokenType.CONDITIONS);
		String content = text;

		Token tokenOptionalSpace = null;
		Token tokenOptionalJoin = null;
		Token tokenOptionalConditions = null;

		Token tokenSpaces = isSpaces(text, required);
		left = tokenSpaces;

		if (left == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.getSubTokens().add(left);

		Token tokenConditionItem = isConditionItem(left.getPosContent(),
				required);
		updateNeighbors(left, tokenConditionItem);
		left = tokenConditionItem;

		if (left == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.getSubTokens().add(left);

		tokenOptionalSpace = isSpaces(left.getPosContent(), false);

		if (tokenOptionalSpace != null) {
			tokenOptionalJoin = isJoinCondition(
					tokenOptionalSpace.getPosContent(), false);

			if (tokenOptionalJoin != null) {
				tokenOptionalConditions = isConditions(
						tokenOptionalJoin.getPosContent(), false);

				if (tokenOptionalConditions != null) {

					updateNeighbors(left, tokenOptionalSpace);
					left = tokenOptionalSpace;
					token.getSubTokens().add(tokenOptionalSpace);

					updateNeighbors(left, tokenOptionalJoin);
					left = tokenOptionalJoin;
					token.getSubTokens().add(tokenOptionalJoin);

					updateNeighbors(left, tokenOptionalConditions);
					left = tokenOptionalConditions;
					token.getSubTokens().add(tokenOptionalConditions);

					content = content.substring(0, content.length()
							- tokenOptionalConditions.getPosContent().length());
					token.setContent(content);
					token.setPosContent(tokenOptionalConditions.getPosContent());
					return token;
				}
			}

		}

		content = content.substring(0, left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <CQL> ::= [<SPACES>] <COMMAND> [ [<SPACES>] <CONDITION> ] [ <SPACES>]
	// [<COMMA> [ <SPACES>]]
	public Token isCQL(String cql) throws LexicalParserException {

		Token tokenCQL = new Token(TokenType.CQL);
		tokenCQL.setContent(cql);
		Token left = null;

		Token tokenSpace = isSpaces(cql, false);
		if (tokenSpace != null) {
			tokenCQL.getSubTokens().add(tokenSpace);
			left = tokenSpace;
		}

		Token tokenCommand = isCommand(
				(left == null) ? cql : left.getPosContent(), true);
		if (tokenCommand == null) {
			throw new LexicalParserException("Token command not found");
		}

		updateNeighbors(left, tokenCommand);
		left = tokenCommand;
		tokenCQL.getSubTokens().add(left);

		tokenSpace = isSpaces(left.getPosContent(), false);
		if (tokenSpace != null) {

			updateNeighbors(left, tokenSpace);
			left = tokenSpace;
			tokenCQL.getSubTokens().add(left);

		}

		Token tokenCondition = isCondition(left.getPosContent(), false);
		if (tokenCondition != null) {
			updateNeighbors(left, tokenCondition);
			left = tokenCondition;
			tokenCQL.getSubTokens().add(left);
		}

		Token tokenComma = isComma(left.getPosContent(), false);

		if (tokenComma != null) {

			updateNeighbors(left, tokenComma);
			left = tokenComma;
			tokenCQL.getSubTokens().add(left);
		}

		tokenSpace = isSpaces(left.getPosContent(), false);
		if (tokenSpace != null) {

			updateNeighbors(left, tokenSpace);
			left = tokenSpace;
			tokenCQL.getSubTokens().add(left);
		}

		if (!left.getPosContent().isEmpty()) {
			throw new LexicalParserException("CEU Lexical Error near ["
					+ left.getPosContent() + "]");
		}

		return tokenCQL;
	}

	// <DOUBLE QUOTED> ::= "^"
	public Token isDoubleQuoted(String text, boolean required)
			throws LexicalParserException {
		return isQuoted(TokenType.DOUBLE_QUOTED, "\"", text, "\"\"", required);
	}

	// <END PARAMETERS>::=)
	public Token isEndParameters(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.END_PARAMETERS, ")", text, required);

	}

	// <FUNCTION>::= <ITEM NAME>[<SPACES>]<START_PARAMETERS>[<SPACES>][<SELECTOR
	// ITEM>][<SPACES>]<END_PARAMETERS>
	public Token isFunction(String content, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.FUNCTION);

		if (content.length() < 3) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;
		}

		Token left = null;

		Token itemName = isItemName(content, required);

		if (itemName == null) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;
		}

		left = itemName;
		token.getSubTokens().add(left);

		Token spaces = isSpaces(left.getPosContent(), required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token startParameters = isStartParameters(left.getPosContent(),
				required);

		if (startParameters == null) {
			if (required) {
				buildLexicalParserException(token, left.getPosContent());
			}
			return null;
		}
		updateNeighbors(left, startParameters);
		left = startParameters;
		token.getSubTokens().add(left);

		spaces = isSpaces(left.getPosContent(), required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token selectorItem = isSelectorItem(left.getPosContent(), required);

		if (selectorItem != null) {
			updateNeighbors(left, selectorItem);
			left = selectorItem;
			token.getSubTokens().add(left);
		}

		spaces = isSpaces(left.getPosContent(), required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token endParameters = isEndParameters(left.getPosContent(), required);

		if (endParameters == null) {
			if (required) {
				buildLexicalParserException(token, left.getPosContent());
			}
			return null;
		}

		updateNeighbors(left, endParameters);
		left = endParameters;
		token.getSubTokens().add(left);

		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <HEXA> ::= (a-f)|(A-F)|<DIGIT>|-[<HEXA>]
	// Notes: The position of the hyphen will not be validated
	public Token isHexa(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.HEX);

		StringBuffer content = new StringBuffer();

		if (text.length() < 3 || !text.toUpperCase().startsWith("0X")) {
			if (required) {
				buildLexicalParserException(token, text);
			} else {
				return null;
			}
		}

		content.append(text.substring(0, 2));

		for (int index = 2; index < text.length(); index++) {
			char character = text.charAt(index);
			boolean characterSmall = character >= 'a' && character <= 'f';
			boolean characterBig = character >= 'A' && character <= 'F';
			boolean digit = character >= '0' && character <= '9';
			boolean hifen = character == '-';

			if (!characterSmall && !characterBig && !digit && !hifen) {
				if (content.length() > 0) {
					break;
				}
				if (required) {
					throw new LexicalParserException("Expected hex in '" + text
							+ "'");
				}
				return null;
			}

			content.append(character);
		}

		String contentStr = content.toString();

		token.setContent(contentStr);
		token.setPosContent(text.substring(contentStr.length()));
		return token;
	}

	// <INJECT> ::= ?
	public Token isInject(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.INJECT);

		if (text.length() == 0 || text.charAt(0) != '?') {
			if (required) {
				buildLexicalParserException(token, text);

			}
			return null;
		}

		token.setContent(String.valueOf('?'));
		token.setPosContent(text.substring(1));

		return token;
	}

	public Token isInputCharacter(Function<String, String> testerException,
			Function<String, Token> testerBase, TokenType type, String text,
			boolean required) throws LexicalParserException {

		Token token = new Token(type);

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text, "empty text []");
			}
			return null;
		}

		String tokenTester = testerException.apply(text);
		String content = null;
		String subText = null;

		if (tokenTester != null) {
			content = tokenTester;
			subText = text.substring(tokenTester.length());
			Token subToken = new Token(type);
			subToken.setContent(content);
			subToken.setPosContent(text.substring(content.length()));

			token.getSubTokens().add(subToken);
		} else {

			if (testerBase.apply(text) != null) {
				return null;
			}
			subText = (text.length() > 1) ? text.substring(1) : "";
			content = text.substring(0, 1);
			//
			// tokenTester = testerException.is(text.substring(0, 1), false);
			//
			// boolean nextIsWrongScape = tokenTester != null;
			//
			// if (nextIsWrongScape) {
			//
			// // nextIsWrongScape,
			// boolean scapeNext = true;
			//
			// while (scapeNext) {
			// content += subText.substring(0, 1);
			// subText = (subText.length() > 1) ? subText.substring(1)
			// : "";
			// tokenTester = testerException.is(subText.substring(0, 1),
			// false);
			// scapeNext = tokenTester != null;
			// }
			//
			// content += subText.substring(0, 1);
			// subText = (subText.length() > 1) ? subText.substring(1) : "";
			//
			// }
		}

		Token subtoken = isInputCharacter(testerException, testerBase, type,
				subText, false);

		if (subtoken == null) {
			token.setContent(content);
			token.setPosContent(text.substring(1));
			return token;
		}

		content += subtoken.getContent();
		token.setContent(content);
		token.setPosContent(text.substring(content.length()));
		token.getSubTokens().add(subtoken);
		return token;

	}

	/*
	 * 
	 * <INPUT CHARACTER EXCEPT DOUBLE> ::= (^<SINGLE QUOTED>%s)[<INPUT CHARACTER
	 * EXCEPT DOUBLE>]
	 */
	public Token isInputCharacterExceptDouble(String text, boolean required)
			throws LexicalParserException {

		Function<String, String> testerException = (t) -> buildDoubleQuotedScape(t);
		Function<String, Token> testerBase = (t) -> {
			try {
				return isDoubleQuoted(t, false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		};

		return isInputCharacter(testerException, testerBase,
				TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE, text, required);

	}

	/*
	 * 
	 * <INPUT CHARACTER EXCEPT SINGLE> ::= (^<SINGLE QUOTED>%s)[<INPUT CHARACTER
	 * EXCEPT SINGLE>]
	 */
	public Token isInputCharacterExceptSingle(String text, boolean required)
			throws LexicalParserException {

		Function<String, String> testerException = (t) -> buildSingleQuotedScape(t);
		Function<String, Token> testerBase = (t) -> {
			try {
				return isSingleQuoted(t, false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		};

		return isInputCharacter(testerException, testerBase,
				TokenType.INPUT_CHARACTER_EXCEPT_SINGLE, text, required);

	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE
	// INSENSITIVE> | <ASTERISK>
	public Token isItemName(final String content, final boolean required)
			throws LexicalParserException {

		Token tokenItemName = new Token(TokenType.ITEMNAME);

		Token tokenCaseSensitive = isItemNameCaseSensitive(content, false);

		if (tokenCaseSensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseSensitive);
			tokenItemName.setContent(tokenCaseSensitive.getContent());
			tokenItemName.setPosContent(content.substring(tokenItemName
					.getContent().length()));
			return tokenItemName;
		}

		Token tokenCaseInsensitive = isItemNameCaseInsensitive(content, false);
		if (tokenCaseInsensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseInsensitive);
			tokenItemName.setContent(tokenCaseInsensitive.getContent());
			tokenItemName.setPosContent(content.substring(tokenItemName
					.getContent().length()));
			return tokenItemName;
		}

		Token asterisc = isAsterisk(content, required);
		if (asterisc == null) {
			return null;
		}

		tokenItemName.getSubTokens().add(asterisc);
		tokenItemName.setContent(asterisc.getContent());
		tokenItemName.setPosContent(content.substring(asterisc.getContent()
				.length()));
		return tokenItemName;
	}

	// <ITEM NAME CASE INSENSITIVE> ::= ^<NUMBER> <CHARS>
	public Token isItemNameCaseInsensitive(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.ITEM_NAME_CASE_INSENSITIVE);

		Token numberToken = isNumber(text, false);
		if (numberToken != null) {
			if (required) {
				buildLexicalParserException(token, text,
						"Expected not number in [" + text + "]");
			}
			return null;
		}

		Token charToken = isChars(text, required);
		if (charToken == null) {
			return null;
		}

		token.getSubTokens().add(charToken);
		token.setContent(charToken.getContent());
		token.setPosContent(text.substring(charToken.getContent().length()));
		return token;

	}

	// <ITEM NAME CASE SENSITIVE> ::= "<CHARS>"
	public Token isItemNameCaseSensitive(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.ITEM_NAME_CASE_SENSITIVE);

		String tokenContent = consume(text, ' ', '.');

		if (!tokenContent.startsWith("\"")) {
			if (required) {
				buildLexicalParserException(token, text,
						"Expected start with '\"' in [" + text + "]");
			}
			return null;
		}

		if (!tokenContent.endsWith("\"")) {
			if (required) {
				buildLexicalParserException(token, text,
						"Expected end with '\"' in [" + text + "]");
			}
			return null;
		}

		String posibleCharsContent = tokenContent.substring(1,
				tokenContent.length() - 1);
		Token tokenChars = isChars(posibleCharsContent, required);
		if (tokenChars == null) {
			if (required) {
				buildLexicalParserException(token, text,
						"Expected characters in [" + text + "]");
			}
			return null;
		}

		if (tokenChars.getPosContent().length() > 0) {
			if (required) {
				buildLexicalParserException(token, text,
						"Expected end with '\"' in [" + text + "]");
			}
			return null;
		}

		token.setContent(tokenContent);
		token.setPosContent(text.substring(tokenContent.length()));
		token.getSubTokens().add(tokenChars);

		return token;
	}

	// <JOIN CONDITION>::= <AND> | <OR>
	public Token isJoinCondition(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.JOIN_CONDITION);

		Token left = isAnd(text, false);
		if (left == null) {
			left = isOr(text, required);
		}

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);
		token.setPosContent(left.getPosContent());
		token.setContent(left.getContent());

		return token;

	}

	/**
	 * <LITERAL> ::= (<NUMBER> | <STRING> | <INJECT> | <HEXA>)^<CHARS>
	 */

	public Token isLiteral(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.LITERAL);

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			} else {
				return null;
			}
		}

		Token tokenNumber = isNumber(text, false);
		if (tokenNumber != null) {

			token.setContent(tokenNumber.getContent());
			token.setPosContent(tokenNumber.getPosContent());
			token.getSubTokens().add(tokenNumber);

			if (isChars(token.getPosContent(), false) != null) {
				if (required) {
					buildLexicalParserException(token, text);
				} else {
					return null;
				}
			}
			return token;
		}

		Token tokenString = isString(text, false);
		if (tokenString != null) {

			token.setContent(tokenString.getContent());
			token.setPosContent(tokenString.getPosContent());
			token.getSubTokens().add(tokenString);
			if (isChars(token.getPosContent(), false) != null) {
				if (required) {
					buildLexicalParserException(token, text);
				} else {
					return null;
				}
			}
			return token;
		}

		Token tokenInject = isInject(text, false);
		if (tokenInject != null) {
			token.getSubTokens().add(tokenInject);
			token.setContent(tokenInject.getContent());
			token.setPosContent(tokenInject.getPosContent());

			if (isChars(token.getPosContent(), false) != null) {
				if (required) {
					buildLexicalParserException(token, text);
				} else {
					return null;
				}
			}

			return token;
		}

		Token tokenHexa = isHexa(text, required);
		if (tokenHexa == null) {
			return null;
		}

		token.setContent(tokenHexa.getContent());
		token.setPosContent(tokenHexa.getPosContent());
		token.getSubTokens().add(tokenHexa);
		if (isChars(token.getPosContent(), false) != null) {
			if (required) {
				buildLexicalParserException(token, text);
			} else {
				return null;
			}
		}
		return token;
	}

	// <NUMBER> ::= <DIGIT>[<NUMBER>]
	public Token isNumber(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.NUMBER);

		Token digit = isDigit(text, required);

		if (digit == null) {
			return null;
		}

		Token left = digit;

		token.getSubTokens().add(digit);

		Token anotherNumber = isNumber(digit.getPosContent(), false);

		if (anotherNumber != null) {
			token.getSubTokens().add(anotherNumber);

			updateNeighbors(left, anotherNumber);

			left = anotherNumber;
		}

		token.setContent(text.substring(0, text.length()
				- left.getPosContent().length()));

		token.setPosContent(left.getPosContent());

		return token;
	}

	// <OR> ::= u(OR)
	public Token isOr(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.OR, "OR", text, false, null, required);

	}

	private Token isQuoted(TokenType type, String quote, String text,
			String scape, boolean required) throws LexicalParserException {
		Token token = new Token(type);

		if (text.length() < quote.length()) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		if (scape != null && text.startsWith(scape)) {
			if (required) {
				buildLexicalParserException(token, text, " found [" + scape
						+ "]");
			}
			return null;
		}

		if (!text.startsWith(quote)) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(quote);
		String postContent = (text.length() > quote.length()) ? text
				.substring(quote.length()) : "";
		token.setPosContent(postContent);

		return token;
	}

	// <RESERVED WORD> ::= SELECT,INSERT,...
	public Token isReservedWord(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.RESERVED_WORD);

		String word = consume(text, ' ');
		if (!RESERVED_WORDS.contains(word.toUpperCase())) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(word);
		token.setPosContent(text.substring(word.length()));
		return token;

	}

	// <RESERVED WORDS>: <RESERVED WORD> [<SPACES> <RESERVED WORDS>]
	public Token isReservedWords(String command, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.RESERVED_WORDS);

		token.setContent(command);

		Token tokenReservedWord = isReservedWord(command, required);

		if (tokenReservedWord == null) {
			return null;
		}

		StringBuffer content = new StringBuffer();

		token.getSubTokens().add(tokenReservedWord);
		content.append(tokenReservedWord.getContent());
		token.setContent(content.toString());

		Token spaces = isSpaces(tokenReservedWord.getPosContent(), false);

		while (spaces != null) {
			Token tokenReservedWordPrevious = tokenReservedWord;

			tokenReservedWord = isReservedWord(spaces.getPosContent(), false);
			if (tokenReservedWord == null) {
				break;
			}
			token.getSubTokens().add(spaces);
			updateNeighbors(tokenReservedWordPrevious, spaces);
			content.append(spaces.getContent());

			token.getSubTokens().add(tokenReservedWord);
			updateNeighbors(spaces, tokenReservedWord);
			content.append(tokenReservedWord.getContent());
			token.setContent(content.toString());

			spaces = isSpaces(tokenReservedWord.getPosContent(), false);
		}

		token.setContent(content.toString());
		token.setPosContent(command.substring(content.length()));
		return token;
	}

	// <SELECTOR BLOCK> ::= ^<WHERE> <SELECTOR ITEM> [[<SPACES>] <COMMA>
	// [<SPACES>] <SELECTOR BLOCK>]
	public Token isSelectorBlock(final String text, final boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_BLOCK);
		String content = text;
		Token leftToken = null;

		Token spaces = isSpaces(text, false);
		if (spaces != null) {
			leftToken = spaces;
			token.getSubTokens().add(leftToken);
		}

		if (isWhere(content.trim(), false) != null) {
			return null;
		}

		Token selectorItem = isSelectorItem(content, required);
		if (selectorItem == null) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;
		}

		updateNeighbors(leftToken, selectorItem);

		leftToken = selectorItem;
		token.getSubTokens().add(leftToken);

		// <SELECTOR BLOCK> ::= ^<WHERE> <SELECTOR ITEM> [[<SPACES>] <COMMA>
		// [<SPACES>] <SELECTOR BLOCK>]

		Token tokenOptional = token.clone();
		Token tokenLeftOptional = leftToken.clone();

		spaces = isSpaces(tokenLeftOptional.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(tokenLeftOptional, spaces);
			tokenLeftOptional = spaces;
			tokenOptional.getSubTokens().add(tokenLeftOptional);
		}

		Token comma = isComma(tokenLeftOptional.getPosContent(), false);
		if (comma != null) {

			updateNeighbors(tokenLeftOptional, comma);
			tokenLeftOptional = comma;
			tokenOptional.getSubTokens().add(tokenLeftOptional);

			spaces = isSpaces(tokenLeftOptional.getPosContent(), false);
			if (spaces != null) {
				updateNeighbors(tokenLeftOptional, spaces);
				tokenLeftOptional = spaces;
				tokenOptional.getSubTokens().add(tokenLeftOptional);
			}

			Token anotherSelectorBlock = isSelectorBlock(
					tokenLeftOptional.getPosContent(), false);

			if (anotherSelectorBlock != null) {

				updateNeighbors(tokenLeftOptional, anotherSelectorBlock);
				tokenLeftOptional = anotherSelectorBlock;
				tokenOptional.getSubTokens().add(tokenLeftOptional);

				token = tokenOptional;
				leftToken = tokenLeftOptional;
			}

		}
		content = content.substring(0, content.length()
				- leftToken.getPosContent().length());

		token.setPosContent(leftToken.getPosContent());
		token.setContent(content);
		return token;

	}

	/**
	 * <SELECTOR ITEM> ::= ^<RESERVED WORD> (<FUNCTION> | <LITERAL> |<ITEM NAME>
	 * [<ACESSOR> <FIELD NAME>] | <ARRAY> | <MAP>)[<SPACES><ALIAS>]
	 */
	public Token isSelectorItem(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_ITEM);
		Token left = null;
		String content = text;

		// ^<RESERVED WORD>
		Token reservedWord = isReservedWord(text, false);

		if (reservedWord != null) {
			if (required) {
				buildLexicalParserException(token, text);
			} else {
				return null;
			}
		}

		boolean found = false;

		Token tokenFunction = isFunction(text, false);

		if (tokenFunction != null) {
			found = true;
			updateNeighbors(left, tokenFunction);
			left = tokenFunction;
			token.getSubTokens().add(left);
		}

		if (!found) {
			Token tokenLiteral = isLiteral(text, false);

			if (tokenLiteral != null) {
				found = true;
				left = tokenLiteral;
				token.getSubTokens().add(left);
			}
		}

		if (!found) {
			Token tokenItemName = isItemName(text, false);

			if (tokenItemName != null) {
				found = true;
				left = tokenItemName;
				token.getSubTokens().add(left);

				Token tokenoriginal = token;
				Token leftoriginal = left;

				token = token.clone();
				left = left.clone();

				Token tokenAcessor = isAcessor(left.getPosContent(), false);

				if (tokenAcessor != null) {

					updateNeighbors(left, tokenAcessor);
					left = tokenAcessor;

					Token tokenFieldName = isFieldName(left.getPosContent(),
							false);

					if (tokenFieldName == null) {
						token = tokenoriginal;
						left = leftoriginal;

					} else {
						token.getSubTokens().add(left);

						updateNeighbors(left, tokenFieldName);
						left = tokenFieldName;
						token.getSubTokens().add(left);
					}

				}
			}

		}

		if (!found) {
			Token tokenarray = isArray(text, false);
			if (tokenarray != null) {
				found = true;

				updateNeighbors(left, tokenarray);
				left = tokenarray;
				token.getSubTokens().add(left);

			}
		}

		if (!found) {
			Token tokenmap = isMap(text, required);
			if (tokenmap != null) {
				found = true;

				updateNeighbors(left, tokenmap);
				left = tokenmap;
				token.getSubTokens().add(left);

			}
		}

		if (!found) {
			return null;
		}

		Token spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			Token alias = isAlias(spaces.getPosContent(), false);
			if (alias != null) {

				updateNeighbors(left, spaces);
				left = spaces;
				token.getSubTokens().add(left);

				updateNeighbors(left, alias);
				left = alias;
				token.getSubTokens().add(left);

			}
		}

		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;

	}

	// <SINGLE QUOTED> ::= '^'
	public Token isSingleQuoted(String text, boolean required)
			throws LexicalParserException {
		return isQuoted(TokenType.SINGLE_QUOTED, "'", text, "''", required);
	}

	private Token isSingleText(TokenType tokenType, String singleText,
			String text, boolean required) throws LexicalParserException {
		return isSingleText(tokenType, singleText, text, true, null, required);
	}

	private Token isSingleText(TokenType tokenType, String singleText,
			String text, boolean caseSensitive, String scape, boolean required)
			throws LexicalParserException {

		Token token = new Token(tokenType);
		String originalText = text;

		if (text.length() < singleText.length()) {
			if (required) {

				buildLexicalParserException(token, text);

			}
			return null;
		}

		String candidateTokenContent = text.substring(0, singleText.length());

		if (!caseSensitive) {
			text = text.toUpperCase();
			singleText = singleText.toUpperCase();
		}

		if (scape != null) {
			if (text.startsWith(scape)) {
				if (required) {

					buildLexicalParserException(token, text);

				}
				return null;
			}
		}

		if (!text.startsWith(singleText)) {
			if (required) {

				buildLexicalParserException(token, text);

			}
			return null;
		}

		token.setContent(candidateTokenContent);
		token.setPosContent(originalText.substring(singleText.length()));
		return token;
	}

	// <SPACES> ::= <SPACE> [<SPACES>]
	public Token isSpaces(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.SPACES);

		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			if (text.charAt(index) == ' ') {
				content.append(' ');
			} else {
				break;
			}
		}

		if (content.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(content.toString());
		token.setPosContent(text.substring(content.length()));
		return token;
	}

	// <START_PARAMETERS>::=(
	public Token isStartParameters(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.START_PARAMETERS, "(", text, required);

	}

	// <STRING> ::= (<SINGLE QUOTED>[<INPUT CHARACTER EXCEPT SINGLE>]<SINGLE
	// QUOTED>) | (<DOUBLE QUOTED>[<INPUT CHARACTER EXCEPT DOUBLE>]<DOUBLE
	// QUOTED>)
	public Token isString(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.STRING);
		Token left = null;
		String content = text;

		Token singleQuoted = isSingleQuoted(text, false);

		if (singleQuoted == null) {
			content = "";
			Token doubleQuoted = isDoubleQuoted(text, required);
			if (doubleQuoted == null) {
				return null;
			}
			updateNeighbors(left, doubleQuoted);
			left = doubleQuoted;
			content += left.getContent();
			token.getSubTokens().add(left);
			Token inputToken = isInputCharacterExceptDouble(
					left.getPosContent(), required);
			if (inputToken != null) {
				updateNeighbors(left, inputToken);
				left = inputToken;
				token.getSubTokens().add(left);
				content += left.getContent();
			}

			doubleQuoted = isDoubleQuoted(left.getPosContent(), required);
			if (doubleQuoted == null) {
				if (required) {
					buildLexicalParserException(token, text);
				}
				return null;
			}
			updateNeighbors(left, doubleQuoted);
			left = doubleQuoted;
			content += left.getContent();
			token.getSubTokens().add(left);
			token.setContent(content);
			token.setPosContent(text.substring(content.length()));
			return token;
		} else {
			left = singleQuoted;
		}
		token.getSubTokens().add(left);

		Token inputToken = isInputCharacterExceptSingle(left.getPosContent(),
				required);
		if (inputToken != null) {
			updateNeighbors(left, inputToken);
			left = inputToken;
			token.getSubTokens().add(left);
		}

		singleQuoted = isSingleQuoted(left.getPosContent(), required);
		if (singleQuoted == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}
		updateNeighbors(left, singleQuoted);
		left = singleQuoted;
		token.getSubTokens().add(left);
		content = text.substring(0, text.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <SYMBOL> ::= = | + | - | / | * | ( | ) | { | } | , [ | ]
	public Token isSymbol(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.SYMBOL);

		if (text.length() == 0 || !SYMBOLS.contains(text.charAt(0))) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(String.valueOf(text.charAt(0)));

		token.setPosContent(text.substring(1));

		return token;
	}

	// <INSERT> ::= u(INSERT)
	public Token isInsert(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.INSERT, "INSERT", text, false, null,
				required);

	}

	// <WHERE> ::= u(WHERE)
	public Token isWhere(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.WHERE, "WHERE", text, false, null,
				required);

	}

	// <VALUES> ::= u(VALUES)
	public Token isValues(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.VALUES, "VALUES", text, false, null,
				required);

	}

	private void updateNeighbors(Token before, Token after) {
		if (before != null) {
			before.setAfter(after);
		}
		if (after != null) {
			after.setBefore(before);
		}
	}

	// <INTO> ::= u(INTO)
	public Token isInto(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.INTO, "INTO", text, false, null, required);

	}

	// <START ARRAY>::=[
	public Token isStartArray(String cql, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.START_ARRAY, "[", cql, required);
	}

	// <END ARRAY>::=]
	public Token isEndArray(String cql, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.END_ARRAY, "]", cql, required);
	}

	// <START MAP>::={
	public Token isStartMap(String cql, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.START_MAP, "{", cql, required);
	}

	public Token isEndMap(String cql, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.END_MAP, "}", cql, required);
	}

	// <ARRAY> ::= <START ARRAY>[<SPACES>][<SELECTOR BLOCK>][<SPACES>]<END
	// ARRAY>
	public Token isArray(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.ARRAY);
		Token left = null;
		String content = text;

		// START ARRAY
		Token startArray = isStartArray(text, required);

		if (startArray == null) {
			return null;
		}

		left = startArray;
		token.getSubTokens().add(startArray);

		Token spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token selectorBlock = isSelectorBlock(left.getPosContent(), false);
		if (selectorBlock != null) {
			updateNeighbors(left, selectorBlock);
			left = selectorBlock;
			token.getSubTokens().add(left);
		}

		spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		// END ARRAY
		Token endArray = isEndArray(left.getPosContent(), required);

		if (endArray == null) {
			return null;
		}

		updateNeighbors(left, endArray);
		left = endArray;
		token.getSubTokens().add(endArray);
		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <MAP> ::= <START MAP>[<SPACES>][<PROPERTIES>][<SPACES>]<END MAP>
	public Token isMap(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.MAP);
		Token left = null;
		String content = text;

		// START MAP
		Token startMap = isStartMap(text, required);

		if (startMap == null) {
			return null;
		}

		left = startMap;
		token.getSubTokens().add(startMap);

		Token spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token properties = isProperties(left.getPosContent(), false);
		if (properties != null) {
			updateNeighbors(left, properties);
			left = properties;
			token.getSubTokens().add(left);
		}

		spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		// END MAP
		Token endMap = isEndMap(left.getPosContent(), required);

		if (endMap == null) {
			return null;
		}

		updateNeighbors(left, endMap);
		left = endMap;
		token.getSubTokens().add(endMap);
		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <PROPERTIES> ::= <PROPERTY> [[<SPACES>]<COMMA>[<SPACES>] <PROPERTIES>]
	public Token isProperties(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.PROPERTIES);
		Token left = null;
		String content = text;

		// START ARRAY
		Token property = isProperty(text, required);

		if (property == null) {
			return null;
		}

		left = property;
		token.getSubTokens().add(property);

		Token originalToken = token.clone();
		Token originalLeft = left.clone();

		Token spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token commaToken = isComma(left.getPosContent(), false);
		if (commaToken != null) {

			updateNeighbors(left, commaToken);
			left = commaToken;
			token.getSubTokens().add(left);

			spaces = isSpaces(left.getPosContent(), false);
			if (spaces != null) {
				updateNeighbors(left, spaces);
				left = spaces;
				token.getSubTokens().add(left);
			}

			Token otherProperties = isProperties(left.getPosContent(), false);
			if (otherProperties != null) {

				updateNeighbors(left, otherProperties);
				left = otherProperties;
				token.getSubTokens().add(left);

				originalToken = token;
				originalLeft = left;
			}
		}

		content = content.substring(0, content.length()
				- originalLeft.getPosContent().length());
		originalToken.setContent(content);
		originalToken.setPosContent(originalLeft.getPosContent());
		return originalToken;
	}

	// <PROPERTY> ::= <KEY>[<SPACES>]<DOUBLE DOT>[<SPACES>]<LITERAL>
	public Token isProperty(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.PROPERTY);
		Token left = null;

		Token key = isKey(text, required);

		if (key == null) {
			return null;
		}
		updateNeighbors(null, key);
		left = key;
		token.getSubTokens().add(left);

		Token spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token doubleDot = isDoubleDot(left.getPosContent(), required);
		if (doubleDot == null) {
			return null;
		}

		updateNeighbors(left, doubleDot);
		left = doubleDot;
		token.getSubTokens().add(left);

		spaces = isSpaces(left.getPosContent(), false);
		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token literal = isLiteral(left.getPosContent(), required);
		if (literal == null) {
			return null;
		}

		updateNeighbors(left, literal);
		left = literal;
		token.getSubTokens().add(left);

		String content = text.substring(0, text.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <KEY> ::= <CHARS>|<LITERAL>
	public Token isKey(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.KEY);
		Token left = null;

		Token chars = isChars(text, false);

		if (chars == null) {
			Token literal = isLiteral(text, required);
			if (literal == null) {
				return null;
			}
			left = literal;
		} else {
			left = chars;
		}

		token.getSubTokens().add(left);
		String content = text.substring(0, text.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <AS> :: = AS
	public Token isAs(String text, boolean required)
			throws LexicalParserException {
		return isSingleText(TokenType.AS, "AS", text, false, null, required);
	}

	// <ALIAS> ::= [<AS> <SPACES>] <FIELD NAME>
	public Token isAlias(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.ALIAS);
		Token left = null;

		Token as = isAs(text, false);

		Token tokenOriginal = token;
		token = token.clone();

		boolean backToOrigin = true;

		if (as != null) {
			updateNeighbors(left, as);
			left = as;
			token.getSubTokens().add(left);

			Token spaces = isSpaces(left.getPosContent(), false);
			if (spaces != null) {
				updateNeighbors(left, spaces);
				left = spaces;
				token.getSubTokens().add(left);
				backToOrigin = false;
			}
		}

		if (backToOrigin) {
			token = tokenOriginal;
		}

		String content = (left == null) ? text : left.getPosContent();
		Token fieldNameToken = isFieldName(content, required);
		if (fieldNameToken == null) {
			return null;
		}
		updateNeighbors(left, fieldNameToken);
		left = fieldNameToken;
		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length()
				- left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <FIELD NAME> ::= ^<RESERVED WORD> (<ITEM NAME CASE SENSITIVE> | <ITEM
	// NAME CASE INSENSITIVE>)
	public Token isFieldName(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.FIELDNAME);

		Token reservedWord = isReservedWord(text, false);

		if (reservedWord != null) {

			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		Token itemNameCaseSensitive = isItemNameCaseSensitive(text, false);
		if (itemNameCaseSensitive != null) {

			token.getSubTokens().add(itemNameCaseSensitive);
			token.setPosContent(itemNameCaseSensitive.getPosContent());
			token.setContent(itemNameCaseSensitive.getContent());
			return token;
		}

		Token itemNameCaseInsensitive = isItemNameCaseInsensitive(text,
				required);
		if (itemNameCaseInsensitive != null) {

			token.getSubTokens().add(itemNameCaseInsensitive);
			token.setPosContent(itemNameCaseInsensitive.getPosContent());
			token.setContent(itemNameCaseInsensitive.getContent());
			return token;
		}

		return null;
	}

	public Token isDigit(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.DIGIT);

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		char character = text.charAt(0);

		boolean number = character >= '0' && character <= '9';

		if (!number) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(String.valueOf(character));
		token.setPosContent(text.substring(1));

		return token;
	}
}
