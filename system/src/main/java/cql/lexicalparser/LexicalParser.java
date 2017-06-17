package cql.lexicalparser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import cql.Token;
import cql.TokenType;
import cql.lexicalparser.exceptions.LexicalParserException;

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

	private String timeZoneGMT = "GMT-00";

	private static final Set<String> RESERVED_WORDS = new HashSet<String>(Arrays.asList("ADD", "ALL", "ALTER", "AND",
			"ANY", "APPLY", "AS", "ASC", "ASCII", "AUTHORIZE", "BATCH", "BEGIN", "BIGINT", "BLOB", "BOOLEAN", "BY",
			"CLUSTERING", "COLUMNFAMILY", "COMPACT", "COUNT", "COUNTER", "CONSISTENCY", "CREATE", "DECIMAL", "DELETE",
			"DESC", "DOUBLE", "DROP", "EACH_QUORUM", "FLOAT", "FROM", "GRANT", "IN", "INDEX", "INET", "INSERT", "INT",
			"INTO", "KEYSPACE", "KEYSPACES", "LEVEL", "LIMIT", "LIST", "LOCAL_ONE", "LOCAL_QUORUM", "MAP", "MODIFY",
			"RECURSIVE", "SUPERUSER", "OF", "ON", "ONE", "ORDER", "PERMISSION", "PERMISSIONS", "PRIMARY", "QUORUM",
			"RENAME", "REVOKE", "SCHEMA", "SELECT", "SET", "STORAGE", "SUPERUSER", "TABLE", "TEXT", "TIMESTAMP",
			"TIMEUUID", "TO", "TOKEN", "THREE", "TRUNCATE", "TTL", "TWO", "TYPE", "UNLOGGED", "UPDATE", "USE", "USER",
			"USING", "UUID", "VALUES", "VARCHAR", "VARINT", "WITH", "WRITETIME", "WHERE"));

	// <SYMBOL> ::= = | + | - | / | * | ( | ) | { | } | , [ | ]
	private static final Set<Character> SYMBOLS = new HashSet<>(
			Arrays.asList('=', '+','<','>','!', '-', '/', '*', '(', ')', '{', '}', ',', '[', ']'));

	public LexicalParser() {
	}

	public String getTimeZoneGMT() {
		return timeZoneGMT;
	}

	public void setTimeZoneGMT(String timeZoneGMT) {
		this.timeZoneGMT = timeZoneGMT;
	}

	private void buildLexicalParserException(Token token, String text) throws LexicalParserException {
		throw new LexicalParserException("Invalid " + token.getType().getName().toUpperCase() + " in [" + text + "]");
	}

	private void buildLexicalParserException(Token token, String text, String reason) throws LexicalParserException {
		reason = (reason == null) ? "" : ":" + reason;
		throw new LexicalParserException(
				"Invalid " + token.getType().getName().toUpperCase() + " in [" + text + "] " + reason);
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
	public Token isAcessor(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ACESSOR, this.timeZoneGMT);

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
	public Token isAnd(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.AND, "AND", text, false, null, required);

	}

	// <ASTERISK> ::= *
	private Token isAsterisk(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.ASTERISK, "*", text, required);

	}

	// <CHARS> ::= ^<EMPTY>[<CHARS>](a-Z0-9_)
	public Token isChars(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.CHARS, this.timeZoneGMT);

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
					|| Character.isDigit(text.codePointAt(index)) || character == '_';

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
	public Token isDoubleDot(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.DOUBLE_DOT, ":", text, required);

	}

	// <COMA> ::= ,
	public Token isComma(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.COMMA, ",", text, required);

	}

	// <COMMAND>::=<CREATE COMMAND> | <DROP COMMAND> | <INSERT COMMAND> |
	// <CONDITIONAL COMMAND> [<SPACES> <LIMIT OPTION>] [<SPACES> <ALLOW
	// PARAMETER>]
	public Token isCommand(final String text, final boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token createCommand = isCreateCommand(text, false);

		leftToken = createCommand;

		if (leftToken == null) {
			Token dropCommand = isDropCommand(text, false);
			leftToken = dropCommand;
		}

		if (leftToken == null) {
			Token insertCommand = isInsertCommand(text, false);
			leftToken = insertCommand;
		}

		if (leftToken == null) {
			Token conditionalCommand = isConditionalCommand(text, required);
			leftToken = conditionalCommand;
		}

		if (leftToken == null) {
			return null;
		}

		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token tokenLimitOption = isLimitOption(tokenSpaces.getPosContent(), false);
			if (tokenLimitOption != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenLimitOption);
				leftToken = tokenLimitOption;
				token.getSubTokens().add(leftToken);
			}
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {

			Token tokenAllowParameter = isAllowParameter(tokenSpaces.getPosContent(), false);
			if (tokenAllowParameter != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenAllowParameter);
				leftToken = tokenAllowParameter;
				token.getSubTokens().add(leftToken);

			}
		}

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);

		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <LIMIT OPTION> ::= <LIMIT> <SPACES> <SPACES> (<NUMBER>|<INJECTION>)
	public Token isLimitOption(String content, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.LIMIT_OPTION, this.timeZoneGMT);

		Token limit = isLimit(content, required);
		Token left = null;

		if (limit == null) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;
		}

		left = limit;
		token.getSubTokens().add(left);

		Token spaces = isSpaces(left.getPosContent(), required);

		if (spaces == null) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;

		}

		updateNeighbors(left, spaces);
		left = spaces;
		token.getSubTokens().add(left);

		Token number = isNumber(left.getPosContent(), false);

		if (number != null) {

			updateNeighbors(left, number);
			left = number;
			token.getSubTokens().add(left);
		} else {
			Token inject = isInject(left.getPosContent(), required);
			if (inject == null) {
				if (required) {
					buildLexicalParserException(token, content);
				}
				return null;
			}
			updateNeighbors(left, inject);
			left = inject;
			token.getSubTokens().add(left);

		}
		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <LIMIT> ::= u(LIMIT)
	public Token isLimit(String content, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.LIMIT, "LIMIT", content, false, null, required);

	}

	// <OTHER COMMAND> ::= ^<WHERE> <RESERVED WORDS> [<SPACES>] (<SELECTOR
	// BLOCK> | ( [<SYMBOL>] [<SPACES>] [<LITERAL>] )[<SPACES>] )
	// [[<SPACES>]<OTHER COMMAND>]
	public Token isOtherCommands(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.OTHER_COMMAND, this.timeZoneGMT);

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

		Token tokenSelectorBlock = isSelectorBlock(leftToken.getPosContent(), false);
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
			Token otherCommands = isOtherCommands(tokenSpaces.getPosContent(), false);
			if (otherCommands != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, otherCommands);
				leftToken = otherCommands;
				token.getSubTokens().add(leftToken);
			}
		}

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <INSERT COMMAND> ::= <INSERT> <SPACES> [<INTO> <SPACES>] <<TABLE NAME
	// DECLARATION>>
	// [<SPACES>] <START_PARAMETERS> [<SPACES>] <FIELD LIST> [<SPACES>]
	// <END_PARAMETERS> [<SPACES>] <VALUES> [<SPACES>] <START_PARAMETERS>
	// [<SPACES>] [<SELECTOR BLOCK>] [<SPACES>] <END_PARAMETERS>

	/**
	 * @param text
	 * @param required
	 * @return
	 * @throws LexicalParserException
	 */
	public Token isInsertCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.INSERT_COMMAND, this.timeZoneGMT);

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

			tokenSpaces = isSpaces(leftToken.getPosContent(), required);
			if (tokenSpaces == null) {
				return null;
			}
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenTableNameDeclaration = isTableNameDeclaration(leftToken.getPosContent(), required);
		if (tokenTableNameDeclaration == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenTableNameDeclaration);
		leftToken = tokenTableNameDeclaration;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token startParams = isStartParameters(leftToken.getPosContent(), required);
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

		Token tokenFieldList = isFieldList(leftToken.getPosContent(), required);
		if (tokenFieldList == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenFieldList);
		leftToken = tokenFieldList;
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

		Token tokenSelectorBlock = isSelectorBlock(leftToken.getPosContent(), required);
		if (tokenSelectorBlock == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSelectorBlock);
		leftToken = tokenSelectorBlock;
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

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());
		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <CONDITION>::=<WHERE><SPACES><CONDITIONS>
	public Token isCondition(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.CONDITION, this::isWhere, this::isConditions);
	}

	//<CONDITION-ITEM>::= <SELECTOR ITEM>[<SPACES>]<OPTIONAL PAIR SYMBOL>[<SPACES>]<SELECTOR ITEM>


	public Token isConditionItem(final String text, final boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.CONDITION_ITEM, this.timeZoneGMT);

		Token tokenSelectorItem = isSelectorItemStrict(text, required);

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

		Token tokenSymbol = isOptionalPairSymbol(left.getPosContent(), false);
		if (tokenSymbol == null) {
			if (required) {
				buildLexicalParserException(token, left.getPosContent(), "Expected OPTIONAL PAIR SYMBOL");
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

		Token tokenSelectorItem2 = isSelectorItemStrict(left.getPosContent(), false);
		if (tokenSelectorItem2 == null) {
			return null;
		}

		updateNeighbors(left, tokenSelectorItem2);
		left = tokenSelectorItem2;
		token.getSubTokens().add(left);

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <CONDITIONS>::=<CONDITION-ITEM>[<SPACES> <JOIN CONDITION> <SPACES>
	// <CONDITIONS>]

	public Token isConditions(final String text, final boolean required) throws LexicalParserException {
		Token left = null;
		Token token = new Token(TokenType.CONDITIONS, this.timeZoneGMT);
		String content = text;

		Token tokenOptionalSpace = null;
		Token tokenOptionalJoin = null;
		Token tokenOptionalConditions = null;

		Token tokenConditionItem = isConditionItem(text, required);
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
			tokenOptionalJoin = isJoinCondition(tokenOptionalSpace.getPosContent(), false);

			if (tokenOptionalJoin != null) {
				Token tokenOptionalSpace2 = isSpaces(tokenOptionalJoin.getPosContent(), false);

				if (tokenOptionalSpace2 != null) {
					tokenOptionalConditions = isConditions(tokenOptionalSpace2.getPosContent(), false);

					if (tokenOptionalConditions != null) {

						updateNeighbors(left, tokenOptionalSpace);
						left = tokenOptionalSpace;
						token.getSubTokens().add(tokenOptionalSpace);

						updateNeighbors(left, tokenOptionalJoin);
						left = tokenOptionalJoin;
						token.getSubTokens().add(tokenOptionalJoin);

						updateNeighbors(left, tokenOptionalSpace2);
						left = tokenOptionalSpace2;
						token.getSubTokens().add(tokenOptionalSpace2);

						updateNeighbors(left, tokenOptionalConditions);
						left = tokenOptionalConditions;
						token.getSubTokens().add(tokenOptionalConditions);

						content = content.substring(0,
								content.length() - tokenOptionalConditions.getPosContent().length());
						token.setContent(content);
						token.setPosContent(tokenOptionalConditions.getPosContent());
						return token;
					}
				}
			}

		}


		content = content.substring(0,
				content.length() - left.getPosContent().length());

		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <CQL>::= [<SPACES>] <COMMAND> [ <SPACES>] [<USING OPTION>] [<DOT COMMA>
	// [<SPACES>]]
	public Token isCQL(String cql) throws LexicalParserException {

		Token tokenCQL = new Token(TokenType.CQL, this.timeZoneGMT);
		tokenCQL.setContent(cql);
		Token left = null;

		Token tokenSpace = isSpaces(cql, false);
		if (tokenSpace != null) {
			tokenCQL.getSubTokens().add(tokenSpace);
			left = tokenSpace;
		}

		Token tokenCommand = isCommand((left == null) ? cql : left.getPosContent(), true);
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

		Token tokenUsingOption = isUsingOption(left.getPosContent(), false);

		if (tokenUsingOption != null) {

			updateNeighbors(left, tokenUsingOption);
			left = tokenUsingOption;
			tokenCQL.getSubTokens().add(left);
		}

		Token tokenDotcomma = isDotComma(left.getPosContent(), false);

		if (tokenDotcomma != null) {

			updateNeighbors(left, tokenDotcomma);
			left = tokenDotcomma;
			tokenCQL.getSubTokens().add(left);
		}

		tokenSpace = isSpaces(left.getPosContent(), false);
		if (tokenSpace != null) {

			updateNeighbors(left, tokenSpace);
			left = tokenSpace;
			tokenCQL.getSubTokens().add(left);
		}

		if (!left.getPosContent().isEmpty()) {
			throw new LexicalParserException("CEU Lexical Error near [" + left.getPosContent() + "]");
		}

		return tokenCQL;
	}

	// <DOUBLE QUOTED> ::= "
	public Token isDoubleQuoted(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.DOUBLE_QUOTED, "\"", text, required);
	}

	// <END PARAMETERS>::=)
	public Token isEndParameters(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.END_PARAMETERS, ")", text, required);

	}

	// <FUNCTION>::=<ITEM NAME>[<SPACES>]<START_PARAMETERS>[<SPACES>][<SELECTOR
	// BLOCK>][<SPACES>]<END_PARAMETERS>
	public Token isFunction(String content, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.FUNCTION, this.timeZoneGMT);

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

		Token spaces = isSpaces(left.getPosContent(), false);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token startParameters = isStartParameters(left.getPosContent(), required);

		if (startParameters == null) {
			if (required) {
				buildLexicalParserException(token, left.getPosContent());
			}
			return null;
		}
		updateNeighbors(left, startParameters);
		left = startParameters;
		token.getSubTokens().add(left);

		spaces = isSpaces(left.getPosContent(), false);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
		}

		Token selectorBlock = isSelectorBlock(left.getPosContent(), required);

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

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <HEXA>::= [<SIGN>] [<START HEX>] <ABSOLUTE HEXA>
	public Token isHexa(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.HEX, this.timeZoneGMT);
		Token left = null;

		Token sign = isSign(text, false);
		if (sign != null) {

			updateNeighbors(left, sign);

			left = sign;

			token.getSubTokens().add(left);

		}

		String posContent = (left == null) ? text : left.getPosContent();

		Token startHex = isStartHexa(posContent, false);
		if (startHex != null) {

			updateNeighbors(left, startHex);

			left = startHex;

			token.getSubTokens().add(left);

		}

		posContent = (left == null) ? text : left.getPosContent();

		Token absoluteHex = isAbsoluteHexa(posContent, required);

		if (absoluteHex == null) {
			return null;
		}

		updateNeighbors(left, absoluteHex);
		left = absoluteHex;
		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <INJECT> ::= ?
	public Token isInject(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.INJECT, this.timeZoneGMT);

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

	public Token isInputCharacter(Function<String, String> testerException, Function<String, Token> testerBase,
			TokenType type, String text, boolean required) throws LexicalParserException {

		Token token = new Token(type, this.timeZoneGMT);

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
			Token subToken = new Token(type, this.timeZoneGMT);
			subToken.setContent(content);
			subToken.setPosContent(text.substring(content.length()));

			token.getSubTokens().add(subToken);
		} else {

			if (testerBase.apply(text) != null) {
				return null;
			}
			subText = (text.length() > 1) ? text.substring(1) : "";
			content = text.substring(0, 1);

		}

		Token subtoken = isInputCharacter(testerException, testerBase, type, subText, false);

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
	 * <INPUT CHARACTER EXCEPT DOUBLE>::= (<DOUBLE QUOTED><DOUBLE QUOTED> |
	 * (^<DOUBLE QUOTED><ANY>))[<INPUT CHARACTER EXCEPT DOUBLE>]
	 */
	public Token isInputCharacterExceptDouble(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE, this.timeZoneGMT);

		Token left = null;

		Token tokenQuoted = isDoubleQuoted(text, false);

		if (tokenQuoted != null) {
			Token anotherTokenQuoted = isDoubleQuoted(tokenQuoted.getPosContent(), false);
			if (anotherTokenQuoted != null) {
				left = tokenQuoted;
				token.getSubTokens().add(left);
				updateNeighbors(left, anotherTokenQuoted);
				left = anotherTokenQuoted;
				token.getSubTokens().add(left);
			} else {
				if (required) {
					buildLexicalParserException(token, text);
				}
				return null;
			}
		}

		if (left == null) {
			Token tokenAny = isAny(text, required);

			if (tokenAny == null) {
				return null;
			}

			left = tokenAny;
			token.getSubTokens().add(left);
		}

		Token tokenAnotherInputCharacterExceptDouble = isInputCharacterExceptDouble(left.getPosContent(), false);

		if (tokenAnotherInputCharacterExceptDouble != null) {
			updateNeighbors(left, tokenAnotherInputCharacterExceptDouble);

			left = tokenAnotherInputCharacterExceptDouble;
			token.getSubTokens().add(left);
		}

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;

	}

	/*
	 * 
	 * <INPUT CHARACTER EXCEPT SINGLE>::= (<SINGLE QUOTED><SINGLE QUOTED> |
	 * (^<SINGLE QUOTED><ANY>))[<INPUT CHARACTER EXCEPT SINGLE>]
	 */
	public Token isInputCharacterExceptSingle(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE, this.timeZoneGMT);

		Token left = null;

		Token tokenSingleQuoted = isSingleQuoted(text, false);

		if (tokenSingleQuoted != null) {
			Token anotherTokenSingleQuoted = isSingleQuoted(tokenSingleQuoted.getPosContent(), false);
			if (anotherTokenSingleQuoted != null) {
				left = tokenSingleQuoted;
				token.getSubTokens().add(left);
				updateNeighbors(left, anotherTokenSingleQuoted);
				left = anotherTokenSingleQuoted;
				token.getSubTokens().add(left);
			} else {
				if (required) {
					buildLexicalParserException(token, text);
				}
				return null;
			}
		}

		if (left == null) {
			Token tokenAny = isAny(text, required);

			if (tokenAny == null) {
				return null;
			}

			left = tokenAny;
			token.getSubTokens().add(left);
		}

		Token tokenAnotherInputCharacterExceptSingle = isInputCharacterExceptSingle(left.getPosContent(), false);

		if (tokenAnotherInputCharacterExceptSingle != null) {
			updateNeighbors(left, tokenAnotherInputCharacterExceptSingle);

			left = tokenAnotherInputCharacterExceptSingle;
			token.getSubTokens().add(left);
		}

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;

	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE
	// INSENSITIVE> | <ASTERISK>
	public Token isItemName(final String content, final boolean required) throws LexicalParserException {

		Token tokenItemName = new Token(TokenType.ITEMNAME, this.timeZoneGMT);

		Token tokenCaseSensitive = isItemNameCaseSensitive(content, false);

		if (tokenCaseSensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseSensitive);
			tokenItemName.setContent(tokenCaseSensitive.getContent());
			tokenItemName.setPosContent(content.substring(tokenItemName.getContent().length()));
			return tokenItemName;
		}

		Token tokenCaseInsensitive = isItemNameCaseInsensitive(content, false);
		if (tokenCaseInsensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseInsensitive);
			tokenItemName.setContent(tokenCaseInsensitive.getContent());
			tokenItemName.setPosContent(content.substring(tokenItemName.getContent().length()));
			return tokenItemName;
		}

		Token asterisc = isAsterisk(content, required);
		if (asterisc == null) {
			return null;
		}

		tokenItemName.getSubTokens().add(asterisc);
		tokenItemName.setContent(asterisc.getContent());
		tokenItemName.setPosContent(content.substring(asterisc.getContent().length()));
		return tokenItemName;
	}

	// <ITEM NAME CASE INSENSITIVE> ::= ^<NUMBER> <CHARS>
	public Token isItemNameCaseInsensitive(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ITEM_NAME_CASE_INSENSITIVE, this.timeZoneGMT);

		Token numberToken = isNumber(text, false);
		if (numberToken != null) {
			if (required) {
				buildLexicalParserException(token, text, "Expected not number in [" + text + "]");
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

	// <ITEM NAME CASE SENSITIVE>::= <DOUBLE QUOTED><CHARS><DOUBLE QUOTED>
	public Token isItemNameCaseSensitive(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.ITEM_NAME_CASE_SENSITIVE, this.timeZoneGMT);
		Token left = null;

		Token startDoubleQuoted = isDoubleQuoted(text, required);

		if (startDoubleQuoted == null) {
			return null;
		}

		left = startDoubleQuoted;
		token.getSubTokens().add(left);

		Token tokenChars = isChars(left.getPosContent(), required);
		if (tokenChars == null) {

			return null;
		}

		updateNeighbors(left, tokenChars);
		left = tokenChars;
		token.getSubTokens().add(left);

		Token stopDoubleQuoted = isDoubleQuoted(left.getPosContent(), required);

		if (stopDoubleQuoted == null) {
			return null;
		}

		updateNeighbors(left, stopDoubleQuoted);
		left = stopDoubleQuoted;
		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <JOIN CONDITION>::= <AND> | <OR>
	public Token isJoinCondition(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.JOIN_CONDITION, this.timeZoneGMT);

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

	public Token isLiteral(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.LITERAL, this.timeZoneGMT);

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
	public Token isNumber(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.NUMBER, this.timeZoneGMT);

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

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));

		token.setPosContent(left.getPosContent());

		return token;
	}

	// <OR> ::= u(OR)
	public Token isOr(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.OR, "OR", text, false, null, required);

	}

	// <RESERVED WORD> ::= SELECT,INSERT,...
	public Token isReservedWord(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.RESERVED_WORD, this.timeZoneGMT);

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
	public Token isReservedWords(String command, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.RESERVED_WORDS, this.timeZoneGMT);

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

	// <SELECTOR BLOCK> ::= <FIELD VALUE> [[<SPACES>] <COMMA> [<SPACES>]
	// <SELECTOR BLOCK>]
	public Token isSelectorBlock(final String text, final boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_BLOCK, this.timeZoneGMT);

		Token leftToken = null;

		Token fieldValue = isFieldValue(text, required);
		if (fieldValue == null) {
			return null;
		}

		updateNeighbors(leftToken, fieldValue);

		leftToken = fieldValue;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);

		String content = (tokenSpaces == null) ? fieldValue.getPosContent() : tokenSpaces.getPosContent();

		Token comma = isComma(content, false);
		if (comma != null) {

			Token tokenSpaces2 = isSpaces(comma.getPosContent(), false);

			content = (tokenSpaces2 == null) ? comma.getPosContent() : tokenSpaces2.getPosContent();

			Token anotherSelectorBlock = isSelectorBlock(content, false);

			if (anotherSelectorBlock != null) {

				if (tokenSpaces != null) {
					updateNeighbors(leftToken, tokenSpaces);
					leftToken = tokenSpaces;
					token.getSubTokens().add(leftToken);
				}

				updateNeighbors(leftToken, comma);
				leftToken = comma;
				token.getSubTokens().add(leftToken);

				if (tokenSpaces2 != null) {
					updateNeighbors(leftToken, tokenSpaces2);
					leftToken = tokenSpaces2;
					token.getSubTokens().add(leftToken);
				}

				updateNeighbors(leftToken, anotherSelectorBlock);
				leftToken = anotherSelectorBlock;
				token.getSubTokens().add(leftToken);

			}

		}

		content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setPosContent(leftToken.getPosContent());
		token.setContent(content);
		return token;

	}

	/**
	 * <SELECTOR ITEM>::= <SELECTOR ITEM STRICT> [<SPACES><ALIAS>]
	 */
	public Token isSelectorItem(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_ITEM, this.timeZoneGMT);
		Token left = null;
		String content = text;

		Token selectorItemStrict = isSelectorItemStrict(text, required);

		if (selectorItemStrict == null) {
			return null;
		}

		left = selectorItemStrict;
		token.getSubTokens().add(selectorItemStrict);

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

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;

	}

	// <SELECTOR ITEM STRICT> ::= ^<RESERVED WORD> <FIELD VALUE>
	public Token isSelectorItemStrict(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_ITEM_STRICT, this.timeZoneGMT);
		Token left = null;
		String content = text;

		// ^<RESERVED WORD>
		Token reservedWord = isReservedWord(text, false);

		if (reservedWord != null) {
			if (required) {
				buildLexicalParserException(token, "Unexpected reserved word in " + text);
			} else {
				return null;
			}
		}

		Token tokenFieldValue = isFieldValue(text, required);

		if (tokenFieldValue == null) {
			return null;
		}

		updateNeighbors(left, tokenFieldValue);
		left = tokenFieldValue;

		token.getSubTokens().add(left);

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;

	}

	// <SINGLE QUOTED> ::= '
	public Token isSingleQuoted(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.SINGLE_QUOTED, "'", text, required);
	}

	private Token isSingleText(TokenType tokenType, String singleText, String text, boolean required)
			throws LexicalParserException {
		return isSingleText(tokenType, singleText, text, true, null, required);
	}

	private Token isSingleText(TokenType tokenType, String singleText, String text, boolean caseSensitive, String scape,
			boolean required) throws LexicalParserException {

		Token token = new Token(tokenType, this.timeZoneGMT);
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
	public Token isSpaces(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.SPACES, this.timeZoneGMT);

		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			char c = text.charAt(index);
			if (c == ' ' || c == '\n' || c == '\t') {
				content.append(c);
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
	public Token isStartParameters(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.START_PARAMETERS, "(", text, required);

	}

	// <STRING> ::= (<SINGLE QUOTED>[<INPUT CHARACTER EXCEPT SINGLE>]<SINGLE
	// QUOTED>) | (<DOUBLE QUOTED>[<INPUT CHARACTER EXCEPT DOUBLE>]<DOUBLE
	// QUOTED>)
	public Token isString(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.STRING, this.timeZoneGMT);
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
			Token inputToken = isInputCharacterExceptDouble(left.getPosContent(), required);
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

		Token inputToken = isInputCharacterExceptSingle(left.getPosContent(), required);
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
		content = text.substring(0, text.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	//<SYMBOL> ::= = | < | > | ! | + | - | / | * | ( | ) | { | } | , [ | ]
	public Token isSymbol(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.SYMBOL, this.timeZoneGMT);

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

	// <OPTIONAL PAIR SIMBOL> ::= <SYMBOL>[<SYMBOL>]
	public Token isOptionalPairSymbol(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.OPTIONAL_PAIR_SYMBOL, this.timeZoneGMT);
		Token left = null;
		String content = text;

		Token symbolA = isSymbol(text, required);

		if (symbolA == null) {
			return null;
		}

		left = symbolA;
		token.getSubTokens().add(symbolA);
		

		Token symbolB = isSymbol(left.getPosContent(), false);

		if (symbolB == null) {			 
			content = content.substring(0, content.length() - left.getPosContent().length());
			token.setContent(content);
			token.setPosContent(left.getPosContent());
			return token;
		}

		left = symbolB;
		token.getSubTokens().add(symbolB);
		
		updateNeighbors(left, symbolB);
		
		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token; 
	}

	// <INSERT> ::= u(INSERT)
	public Token isInsert(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.INSERT, "INSERT", text, false, null, required);

	}

	// <WHERE> ::= u(WHERE)
	public Token isWhere(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.WHERE, "WHERE", text, false, null, required);

	}

	// <VALUES> ::= u(VALUES)
	public Token isValues(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.VALUES, "VALUES", text, false, null, required);

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
	public Token isInto(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.INTO, "INTO", text, false, null, required);

	}

	// <START BRACKET>::=[
	public Token isStartBracket(String cql, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.START_BRACKET, "[", cql, required);
	}

	// <END BRACKET>::=]
	public Token isEndBracket(String cql, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.END_BRACKET, "]", cql, required);
	}

	// <START BRACE>::={
	public Token isStartBrace(String cql, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.START_BRACE, "{", cql, required);
	}

	// <END BRACE>::= }
	public Token isEndBrace(String cql, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.END_BRACE, "}", cql, required);
	}

	// <ARRAY>::= <ARRAY BRACKET> | <ARRAY BRACE>
	public Token isArray(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.ARRAY, this.timeZoneGMT);
		Token left = null;

		Token arrayBracket = isArrayBracket(text, false);

		if (arrayBracket != null) {
			left = arrayBracket;
		} else {
			Token arrayBrace = isArrayBrace(text, required);

			if (arrayBrace == null) {
				return null;
			}

			left = arrayBrace;
		}

		token.getSubTokens().add(left);
		token.setContent(left.getContent());
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <MAP>::= <START BRACE>[<SPACES>][<PROPERTIES>][<SPACES>]<END BRACE>
	public Token isMap(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.MAP, this.timeZoneGMT);
		Token left = null;
		String content = text;

		// START MAP
		Token startMap = isStartBrace(text, required);

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
		Token endBrace = isEndBrace(left.getPosContent(), required);

		if (endBrace == null) {
			return null;
		}

		updateNeighbors(left, endBrace);
		left = endBrace;
		token.getSubTokens().add(endBrace);
		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <PROPERTIES> ::= <PROPERTY> [[<SPACES>]<COMMA>[<SPACES>] <PROPERTIES>]
	public Token isProperties(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.PROPERTIES, this.timeZoneGMT);
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

		content = content.substring(0, content.length() - originalLeft.getPosContent().length());
		originalToken.setContent(content);
		originalToken.setPosContent(originalLeft.getPosContent());
		return originalToken;
	}

	// <PROPERTY> ::= <KEY>[<SPACES>]<DOUBLE DOT>[<SPACES>]<LITERAL>
	public Token isProperty(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.PROPERTY, this.timeZoneGMT);
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

		String content = text.substring(0, text.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <KEY> ::= <CHARS>|<LITERAL>
	public Token isKey(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.KEY, this.timeZoneGMT);
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
		String content = text.substring(0, text.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <AS> :: = AS
	public Token isAs(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.AS, "AS", text, false, null, required);
	}

	// <ALIAS>::=[<AS> <SPACES>] <ENTITY NAME>
	public Token isAlias(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.ALIAS, this.timeZoneGMT);
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
		Token fieldNameToken = isEntityName(content, required);
		if (fieldNameToken == null) {
			return null;
		}
		updateNeighbors(left, fieldNameToken);
		left = fieldNameToken;
		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <FIELD NAME> ::= [<TABLE NAME>[<SPACES>]<ACESSOR>[<SPACES>]] (<ENTITY
	// NAME>|<ASTERISK>)
	public Token isFieldName(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.FIELD_NAME, this.timeZoneGMT);

		Token tokenTableName = isTableName(text, false);
		Token left = null;
		Token tokenAcessor = null;

		if (tokenTableName != null) {

			left = tokenTableName;

			Token tokenSpaces = isSpaces(left.getPosContent(), false);

			if (tokenSpaces != null) {
				updateNeighbors(left, tokenSpaces);
				left = tokenSpaces;

			}

			tokenAcessor = isAcessor(left.getPosContent(), false);

			if (tokenAcessor != null) {
				updateNeighbors(left, tokenAcessor);
				left = tokenAcessor;

				token.getSubTokens().add(tokenTableName);
				if (tokenSpaces != null) {
					token.getSubTokens().add(tokenSpaces);
				}
				token.getSubTokens().add(tokenAcessor);

				tokenSpaces = isSpaces(left.getPosContent(), false);

				if (tokenSpaces != null) {
					updateNeighbors(left, tokenSpaces);
					left = tokenSpaces;
					token.getSubTokens().add(left);

				}

			}
		}

		String content = (tokenAcessor == null) ? text : tokenAcessor.getPosContent();

		Token tokenEntityName = isEntityName(content, false);

		updateNeighbors(tokenAcessor, tokenEntityName);

		left = tokenEntityName;

		if (left == null) {

			Token tokenAsterisk = isAsterisk(text, required);
			if (tokenAsterisk == null) {
				return null;
			}

			updateNeighbors(left, tokenAsterisk);
			left = tokenAsterisk;

		}

		token.getSubTokens().add(left);
		token.setPosContent(left.getPosContent());
		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		return token;
	}

	public Token isDigit(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.DIGIT, this.timeZoneGMT);

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

	// <END CREATE TABLE>::=^<DOT COMMA> <ANY> [<END CREATE TABLE>]
	public Token isEndCreateTable(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.END_CREATE_TABLE, this.timeZoneGMT);
		Token left = null;

		Token dotComma = isDotComma(text, false);

		if (dotComma != null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token anyToken = isAny(text, required);
		if (anyToken == null) {
			return null;
		}

		left = anyToken;
		token.getSubTokens().add(left);

		Token nextEndCreateTable = isEndCreateTable(text.substring(1), false);
		if (nextEndCreateTable != null) {
			updateNeighbors(left, nextEndCreateTable);
			left = nextEndCreateTable;
			token.getSubTokens().add(left);
		}

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <DOT COMMA> :: = ;
	public Token isDotComma(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.DOT_COMMA, ";", text, false, null, required);
	}

	// <ALLOW PARAMETER> ::= <ALLOW><SPACES><FILTERING>
	public Token isAllowParameter(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.ALLOW_PARAMETER, this::isAllow, this::isFiltering);
	}

	// <X> ::= <A> <SPACES> <B>
	public Token isDoubleTokensSpaceded(String text, boolean required, TokenType type, LexicalTester testerA,
			LexicalTester testerB) throws LexicalParserException {

		Token token = new Token(type, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token a = testerA.is(text, required);
		left = a;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		Token spaces = isSpaces(left.getPosContent(), required);
		updateNeighbors(left, spaces);
		left = spaces;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		Token b = testerB.is(left.getPosContent(), required);
		updateNeighbors(left, b);
		left = b;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <START CREATE TABLE> ::= <CREATE> <SPACES> <TABLE>
	public Token isStartCreateTable(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.START_CREATE_TABLE, this::isCreate, this::isTable);

	}

	// <CREATE> :: = CREATE
	public Token isCreate(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.CREATE, "CREATE", text, false, null, required);
	}

	// <TABLE> :: = TABLE
	public Token isTable(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.TABLE, "TABLE", text, false, null, required);
	}

	// <CREATE TABLE COMMAND> ::= <START CREATE TABLE> <END CREATE TABLE>
	public Token isCreateTableCommand(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.CREATE_TABLE_COMMAND, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token startCreateTable = isStartCreateTable(text, required);
		left = startCreateTable;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		Token endCreateTable = isEndCreateTable(left.getPosContent(), required);
		updateNeighbors(left, endCreateTable);
		left = endCreateTable;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <SIGN>::=+|-
	public Token isSign(String text, boolean required) throws LexicalParserException {
		Token plus = isSingleText(TokenType.SIGN, "+", text, false);
		if (plus != null) {
			return plus;
		}
		return isSingleText(TokenType.SIGN, "-", text, required);

	}

	// <ABSOLUTE HEXA>::= (<HEXA CHAR>|<DIGIT>)[<ABSOLUTE HEXA>]
	public Token isAbsoluteHexa(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ABSOLUTE_HEX, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token hexaChar = isHexaChar(text, false);

		if (hexaChar != null) {
			left = hexaChar;
			token.getSubTokens().add(left);
		}

		if (left == null) {
			Token digit = isDigit(text, false);
			if (digit != null) {
				left = digit;
				token.getSubTokens().add(left);
			}
		}

		if (left == null) {
			if (required) {
				buildLexicalParserException(token, text);

			}
			return null;
		}

		Token anotherAbsoluteHexa = isAbsoluteHexa(left.getPosContent(), false);
		if (anotherAbsoluteHexa != null) {
			updateNeighbors(left, anotherAbsoluteHexa);
			left = anotherAbsoluteHexa;
			token.getSubTokens().add(left);
		}

		token.setPosContent(left.getPosContent());
		token.setContent(text.substring(0, text.length() - token.getPosContent().length()));

		return token;
	}

	// //<ARRAY BRACE> ::= <START BRACE>[<SPACES>][<SELECTOR
	// BLOCK>][<SPACES>]<END BRACE>
	public Token isArrayBrace(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.ARRAY_BRACE, this.timeZoneGMT);
		Token left = null;
		String content = text;

		// START
		Token startArray = isStartBrace(text, required);

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

		// END
		Token endArray = isEndBrace(left.getPosContent(), required);

		if (endArray == null) {
			return null;
		}

		updateNeighbors(left, endArray);
		left = endArray;
		token.getSubTokens().add(endArray);
		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <ARRAY BRACKET>::= <START BRACKET>[<SPACES>][<SELECTOR
	// BLOCK>][<SPACES>]<END BRACKET>
	public Token isArrayBracket(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ARRAY_BRACKET, this.timeZoneGMT);
		Token left = null;
		String content = text;

		// START ARRAY
		Token startArray = isStartBracket(text, required);

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
		Token endArray = isEndBracket(left.getPosContent(), required);

		if (endArray == null) {
			return null;
		}

		updateNeighbors(left, endArray);
		left = endArray;
		token.getSubTokens().add(endArray);
		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <TTL PARAMETER>::=<TTL> <SPACES> (<NUMBER> | <INJECT> )
	public Token isTTLParameter(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.TTL_PARAMETER, this.timeZoneGMT);
		Token left = null;

		Token startTtl = isTTL(text, required);
		if (startTtl == null) {
			return null;
		}

		left = startTtl;
		token.getSubTokens().add(left);

		Token spaces = isSpaces(text.substring(3), required);

		if (spaces == null) {
			return null;
		}

		updateNeighbors(left, spaces);
		left = spaces;
		token.getSubTokens().add(left);

		Token number = isNumber(left.getPosContent(), required);

		if (number == null) {
			Token inject = isInject(spaces.getPosContent(), required);
			if (inject == null) {
				return null;
			}
			left = inject;
		} else {
			left = number;
		}

		updateNeighbors(spaces, left);
		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <USING OPTION>::=<START USING> <SPACES> <END USING>
	public Token isUsingOption(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.USING_OPTION, this::isStartUsing, this::isEndUsing);

	}

	// <END CREATE INDEX COMMAND>::=^<DOT COMMA> <ANY> [<END CREATE INDEX
	// COMMAND>]
	public Token isEndCreateIndexCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.END_CREATE_INDEX_COMMAND, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token dotComma = isDotComma(text, false);

		if (dotComma != null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token anyToken = isAny(text, required);

		if (anyToken == null) {
			return null;
		}

		left = anyToken;
		token.getSubTokens().add(left);

		Token nextEndCreateTable = isEndCreateIndexCommand(left.getPosContent(), false);
		if (nextEndCreateTable != null) {
			updateNeighbors(left, nextEndCreateTable);
			left = nextEndCreateTable;
			token.getSubTokens().add(left);
		}

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <INDEX> ::= u(INDEX)
	public Token isIndex(String text, boolean required) throws LexicalParserException {
		return isSingleText(TokenType.INDEX, "INDEX", text, false, null, required);
	}

	// <START CREATE INDEX COMMAND> ::= <CREATE> <SPACES> <INDEX>
	public Token isStartCreateIndexCommand(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.START_CREATE_INDEX_COMMAND, this::isCreate,
				this::isIndex);

	}

	// <CREATE INDEX COMMAND> ::= <START CREATE INDEX COMMAND> <END CREATE INDEX
	// COMMAND>
	public Token isCreateIndexCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.CREATE_INDEX_COMMAND, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token startCreateIndex = isStartCreateIndexCommand(text, required);
		left = startCreateIndex;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		Token endCreateIndex = isEndCreateIndexCommand(left.getPosContent(), required);
		updateNeighbors(left, endCreateIndex);
		left = endCreateIndex;

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <CREATE COMMAND> ::= <CREATE TABLE COMMAND> | <CREATE INDEX COMMAND>
	public Token isCreateCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.CREATE_COMMAND, this.timeZoneGMT);
		Token left = null;

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token createTableCommand = isCreateTableCommand(text, false);
		left = createTableCommand;

		if (left == null) {
			Token createIndexCommand = isCreateIndexCommand(text, required);
			left = createIndexCommand;
			if (left == null) {
				return null;
			}

		}

		token.getSubTokens().add(left);

		token.setContent(text.substring(0, text.length() - left.getPosContent().length()));
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <HEXA CHAR> :: = u(a-f)
	public Token isHexaChar(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.HEXA_CHAR, this.timeZoneGMT);

		char character = text.charAt(0);
		boolean characterSmall = character >= 'a' && character <= 'f';
		boolean characterBig = character >= 'A' && character <= 'F';
		if (!characterSmall && !characterBig) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		token.setContent(String.valueOf(character));
		token.setPosContent(text.substring(1));
		return token;
	}

	// <START HEX> ::= u(0X)
	public Token isStartHexa(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.START_HEX, this.timeZoneGMT);

		if (!text.toLowerCase().startsWith("0x")) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		token.setContent(text.substring(0, 2));
		token.setPosContent(text.substring(2));
		return token;
	}

	// <ANY> ::= ?
	public Token isAny(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ANY, this.timeZoneGMT);

		if (text.length() == 0) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;

		}

		token.setContent(text.substring(0, 1));
		token.setPosContent(text.substring(1));
		return token;
	}

	// <START USING> ::= <USING>
	public Token isStartUsing(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.START_USING, this.timeZoneGMT);

		Token left = isUsing(text, required);

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		token.setContent(left.getContent());
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <END USING>::=<TTL PARAMETER>
	public Token isEndUsing(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.END_USING, this.timeZoneGMT);

		Token left = isTTLParameter(text, required);

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);

		token.setContent(left.getContent());
		token.setPosContent(left.getPosContent());

		return token;
	}

	// <SET>::=u(SET)
	public Token isSet(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.SET, this.timeZoneGMT);

		if (!text.toUpperCase().startsWith("SET")) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		token.setContent(text.substring(0, 3));
		token.setPosContent(text.substring(3));
		return token;
	}

	// <FROM>::=u(FROM)
	public Token isFrom(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.FROM, this.timeZoneGMT);

		if (!text.toUpperCase().startsWith("FROM")) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		token.setContent(text.substring(0, 4));
		token.setPosContent(text.substring(4));
		return token;
	}

	// <END COMMON COMMAND>::= (<SELECTOR BLOCK> | <SYMBOL> | <LITERAL> )
	// [[<SPACES>]<END COMMON COMMAND>]]
	public Token isEndCommonCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.END_COMMON_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenSelectorBlock = isSelectorBlock(text, false);
		if (tokenSelectorBlock != null) {
			leftToken = tokenSelectorBlock;
		}

		if (leftToken == null) {

			Token tokenSymbol = isSymbol(text, false);
			leftToken = tokenSymbol;
		}

		if (leftToken == null) {

			Token tokenLiteral = isLiteral(text, required);
			leftToken = tokenLiteral;
		}

		if (leftToken == null) {
			return null;
		}

		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		String content = (tokenSpaces == null) ? leftToken.getPosContent() : tokenSpaces.getPosContent();

		Token tokenEndCommonCommand = isEndCommonCommand(content, false);
		if (tokenEndCommonCommand != null) {
			if (tokenSpaces != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);
			}

			updateNeighbors(leftToken, tokenEndCommonCommand);
			leftToken = tokenEndCommonCommand;
			token.getSubTokens().add(leftToken);
		}
		content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <START UPDATE COMMAND> ::= <UPDATE> <SPACES> <TABLE NAME> <SPACES> <SET>
	public Token isStartUpdateCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.START_UPDATE_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenUpdate = isUpdate(text, required);
		if (tokenUpdate == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenUpdate);
		leftToken = tokenUpdate;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenTableName = isTableName(leftToken.getPosContent(), required);
		if (tokenTableName == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenTableName);
		leftToken = tokenTableName;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenSet = isSet(leftToken.getPosContent(), required);
		if (tokenSet == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenSet);
		leftToken = tokenSet;
		token.getSubTokens().add(leftToken);

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <UPDATE>::=u(UPDATE)
	public Token isUpdate(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.UPDATE, this.timeZoneGMT);

		if (!text.toUpperCase().startsWith("UPDATE")) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		token.setContent(text.substring(0, 6));
		token.setPosContent(text.substring(6));
		return token;

	}

	// <DELETE>::=u(DELETE)
	public Token isDelete(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.DELETE, this.timeZoneGMT);

		if (!text.toUpperCase().startsWith("DELETE")) {
			if (required) {
				buildLexicalParserException(token, text);
			}

			return null;
		}

		token.setContent(text.substring(0, 6));
		token.setPosContent(text.substring(6));
		return token;

	}

	// <START DELETE COMMAND> ::= <DELETE> <SPACES> [<FROM> <SPACES>] <TABLE
	// NAME>

	public Token isStartDeleteCommand(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.START_DELETE_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenDelete = isDelete(text, required);
		if (tokenDelete == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenDelete);
		leftToken = tokenDelete;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenFrom = isFrom(leftToken.getPosContent(), false);
		if (tokenFrom != null) {
			tokenSpaces = isSpaces(tokenFrom.getPosContent(), required);
			if (tokenSpaces == null) {
				return null;
			}
			updateNeighbors(leftToken, tokenFrom);
			leftToken = tokenFrom;
			token.getSubTokens().add(leftToken);

			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
		}

		Token tokenTableName = isTableName(leftToken.getPosContent(), required);
		if (tokenTableName == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenTableName);
		leftToken = tokenTableName;
		token.getSubTokens().add(leftToken);

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;

	}

	// <UPDATE COMMAND>::=<START UPDATE COMMAND> <SPACES> <END COMMON COMMAND>
	public Token isUpdateCommand(String text, boolean required) throws LexicalParserException {
		return isDoubleTokensSpaceded(text, required, TokenType.UPDATE_COMMAND, this::isStartUpdateCommand,
				this::isEndCommonCommand);

	}

	// <DELETE COMMAND>::=<START DELETE COMMAND> [<SPACES> <END COMMON COMMAND>]
	public Token isDeleteCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.DELETE_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenStartDeleteCommand = isStartDeleteCommand(text, required);
		if (tokenStartDeleteCommand == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenStartDeleteCommand);
		leftToken = tokenStartDeleteCommand;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token tokenEndCommonCommand = isEndCommonCommand(tokenSpaces.getPosContent(), false);
			if (tokenEndCommonCommand != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenEndCommonCommand);
				leftToken = tokenEndCommonCommand;
				token.getSubTokens().add(leftToken);

			}
		}

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <CONDITIONAL COMMAND> ::= (<DELETE COMMAND> | <UPDATE COMMAND> | <OTHER
	// COMMAND>) [ <SPACES> <CONDITION> ]
	public Token isConditionalCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.CONDITIONAL_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenDeleteCommand = isDeleteCommand(text, false);
		if (tokenDeleteCommand != null) {
			leftToken = tokenDeleteCommand;
		}

		if (leftToken == null) {
			Token tokenUpdateCommand = isUpdateCommand(text, false);
			if (tokenUpdateCommand != null) {
				leftToken = tokenUpdateCommand;
			}
		}

		if (leftToken == null) {
			Token tokenOtherCommand = isOtherCommands(text, required);
			if (tokenOtherCommand != null) {
				leftToken = tokenOtherCommand;
			}
		}

		if (leftToken == null) {
			return null;
		}

		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token tokenConditional = isCondition(tokenSpaces.getPosContent(), false);

			if (tokenConditional != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenConditional);
				leftToken = tokenConditional;
				token.getSubTokens().add(leftToken);

			}
		}

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());

		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <ENTITY NAME> ::= ^<RESERVED WORD> (<ITEM NAME CASE SENSITIVE> | <ITEM
	// NAME CASE INSENSITIVE>)

	public Token isEntityName(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.ENTITY_NAME, this.timeZoneGMT);

		Token tokenReservedWorld = isReservedWord(text, false);

		if (tokenReservedWorld != null) {
			if (required) {
				buildLexicalParserException(tokenReservedWorld, text);
			}

			return null;
		}

		Token tokenCaseSensitive = isItemNameCaseSensitive(text, false);

		Token left = null;

		if (tokenCaseSensitive != null) {

			left = tokenCaseSensitive;
		}

		if (left == null) {
			Token tokenCaseInsensitive = isItemNameCaseInsensitive(text, required);
			if (tokenCaseInsensitive == null) {
				return null;
			} else {
				left = tokenCaseInsensitive;
			}
		}
		token.getSubTokens().add(left);
		token.setContent(left.getContent());
		token.setPosContent(left.getPosContent());
		return token;

	}

	// <TABLE NAME>::=<ENTITY NAME>|<INJECT>

	public Token isTableName(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.TABLE_NAME, this.timeZoneGMT);
		Token left = null;
		Token tokenEntityName = isEntityName(text, false);

		if (tokenEntityName != null) {

			left = tokenEntityName;
		}

		if (left == null) {
			Token tokenInject = isInject(text, required);

			if (tokenInject != null) {

				left = tokenInject;
			}
		}

		if (left == null) {
			return null;
		}

		token.getSubTokens().add(left);
		token.setContent(left.getContent());
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <TABLE NAME DECLARATION>::= <TABLE NAME> [<SPACES>] <ALIAS>]
	public Token isTableNameDeclaration(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.TABLE_NAME_DECLARATION, this.timeZoneGMT);
		Token leftToken = null;

		Token tableName = isTableName(text, required);

		if (tableName == null) {
			return null;
		}

		leftToken = tableName;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token tokenAlias = isAlias(tokenSpaces.getPosContent(), false);

			if (tokenAlias != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenAlias);
				leftToken = tokenAlias;
				token.getSubTokens().add(leftToken);

			}
		}

		token.setContent(text.substring(0, text.length() - leftToken.getPosContent().length()));
		token.setPosContent(leftToken.getPosContent());
		return token;
	}

	// <FIELD NAME DECLARATION> ::= <FIELD VALUE> [<SPACES>] <ALIAS>]
	public Token isFieldNameDeclaration(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.FIELD_NAME_DECLARATION, this.timeZoneGMT);
		Token leftToken = null;

		Token tokenFieldValue = isFieldValue(text, required);

		if (tokenFieldValue == null) {
			return null;
		}

		leftToken = tokenFieldValue;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			Token tokenAlias = isAlias(tokenSpaces.getPosContent(), false);

			if (tokenAlias != null) {
				updateNeighbors(leftToken, tokenSpaces);
				leftToken = tokenSpaces;
				token.getSubTokens().add(leftToken);

				updateNeighbors(leftToken, tokenAlias);
				leftToken = tokenAlias;
				token.getSubTokens().add(leftToken);

			}
		}

		token.setContent(text.substring(0, text.length() - leftToken.getPosContent().length()));
		token.setPosContent(leftToken.getPosContent());
		return token;
	}

	// <FIELD VALUE> ::= <FUNCTION> | <ARRAY> | <MAP> | <FIELD NAME>,<LITERAL>
	public Token isFieldValue(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.FIELD_VALUE, this.timeZoneGMT);
		Token left = null;
		String content = text;

		if (left == null) {

			Token tokenFunction = isFunction(text, false);

			updateNeighbors(left, tokenFunction);
			left = tokenFunction;
		}

		if (left == null) {
			Token tokenarray = isArray(text, false);
			updateNeighbors(left, tokenarray);
			left = tokenarray;
		}

		if (left == null) {
			Token tokenmap = isMap(text, false);
			updateNeighbors(left, tokenmap);
			left = tokenmap;
		}
		if (left == null) {
			Token tokenFieldName = isFieldName(text, false);
			updateNeighbors(left, tokenFieldName);
			left = tokenFieldName;

		}

		if (left == null) {
			Token tokenLiteral = isLiteral(text, required);
			left = tokenLiteral;

			if (left == null) {
				return null;
			}

		}

		token.getSubTokens().add(left);

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;

	}

	// <FIELD LIST> ::= <FIELD NAME> [[<SPACES>] <COMMA> [<SPACES>] <FIELD
	// LIST>]
	public Token isFieldList(String text, boolean required) throws LexicalParserException {

		Token token = new Token(TokenType.FIELD_LIST, this.timeZoneGMT);
		Token left = null;
		String content = text;

		Token tokenFieldName = isFieldName(text, required);

		if (tokenFieldName == null) {
			return null;
		}

		updateNeighbors(left, tokenFieldName);
		left = tokenFieldName;
		token.getSubTokens().add(left);

		Token tokenSpaces = isSpaces(left.getPosContent(), false);

		Token left2 = left;

		if (tokenSpaces != null) {

			left2 = tokenSpaces;
		}

		Token tokenComma = isComma(left2.getPosContent(), false);

		if (tokenComma != null) {
			left2 = tokenComma;
			Token tokenSpaces2 = isSpaces(left2.getPosContent(), false);

			if (tokenSpaces2 != null) {
				left2 = tokenSpaces2;
			}

			Token tokenFieldList = isFieldList(left2.getPosContent(), false);

			if (tokenFieldList != null) {
				if (tokenSpaces != null) {
					updateNeighbors(left, tokenSpaces);
					left = tokenSpaces;
					token.getSubTokens().add(left);
				}

				updateNeighbors(left, tokenComma);
				left = tokenComma;
				token.getSubTokens().add(left);

				if (tokenSpaces2 != null) {
					updateNeighbors(left, tokenSpaces2);
					left = tokenSpaces2;
					token.getSubTokens().add(left);
				}

				updateNeighbors(left, tokenFieldList);
				left = tokenFieldList;
				token.getSubTokens().add(left);
			}

		}

		content = content.substring(0, content.length() - left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());

		return token;

	}

	// // <DROP> ::= u(DROP)
	public Token isDrop(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.DROP, "DROP", text, false, null, required);

	}

	// // <DROP COMMAND> ::= <DROP><SPACES><RESERVED WORD><SPACES><ENTITY NAME>
	public Token isDropCommand(String text, boolean required) throws LexicalParserException {
		Token token = new Token(TokenType.DROP_COMMAND, this.timeZoneGMT);

		Token leftToken = null;

		Token tokenDrop = isDrop(text, required);
		if (tokenDrop == null) {
			return null;
		}

		updateNeighbors(leftToken, tokenDrop);
		leftToken = tokenDrop;
		token.getSubTokens().add(leftToken);

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenReservedWord = isReservedWord(leftToken.getPosContent(), required);
		if (tokenReservedWord == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenReservedWord);
		leftToken = tokenReservedWord;
		token.getSubTokens().add(leftToken);

		tokenSpaces = isSpaces(leftToken.getPosContent(), required);
		if (tokenSpaces == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenSpaces);
		leftToken = tokenSpaces;
		token.getSubTokens().add(leftToken);

		Token tokenEntityName = isEntityName(leftToken.getPosContent(), required);
		if (tokenEntityName == null) {
			return null;
		}
		updateNeighbors(leftToken, tokenEntityName);
		leftToken = tokenEntityName;
		token.getSubTokens().add(leftToken);

		String content = text.substring(0, text.length() - leftToken.getPosContent().length());
		token.setContent(content);
		token.setPosContent(leftToken.getPosContent());

		return token;
	}

	// <ALLOW>::=u(ALLOW)
	public Token isAllow(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.ALLOW, "ALLOW", text, false, null, required);

	}

	// <FILTERING>::=u(FILTERING)
	public Token isFiltering(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.FILTERING, "FILTERING", text, false, null, required);

	}

	// <USING>::=u(USING)
	public Token isUsing(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.USING, "USING", text, false, null, required);

	}

	// <TTL>::=u(TTL)
	public Token isTTL(String text, boolean required) throws LexicalParserException {

		return isSingleText(TokenType.TTL, "TTL", text, false, null, required);

	}
}