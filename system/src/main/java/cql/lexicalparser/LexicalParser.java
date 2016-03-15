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
					"APPLY", "ASC", "ASCII", "AUTHORIZE", "BATCH", "BEGIN",
					"BIGINT", "BLOB", "BOOLEAN", "BY", "CLUSTERING",
					"COLUMNFAMILY", "COMPACT", "COUNT", "COUNTER",
					"CONSISTENCY", "CREATE", "DECIMAL", "DELETE", "DESC",
					"DOUBLE", "DROP", "EACH_QUORUM", "FILTERING", "FLOAT",
					"FROM", "GRANT", "IN", "INDEX", "INET", "INSERT", "INT",
					"INTO", "KEY", "KEYSPACE", "KEYSPACES", "LEVEL", "LIMIT",
					"LIST", "LOCAL_ONE", "LOCAL_QUORUM", "MAP", "MODIFY",
					"RECURSIVE", "SUPERUSER", "OF", "ON", "ONE", "ORDER",
					"PASSWORD", "PERMISSION", "PERMISSIONS", "PRIMARY",
					"QUORUM", "RENAME", "REVOKE", "SCHEMA", "SELECT", "SET",
					"STORAGE", "SUPERUSER", "TABLE", "TEXT", "TIMESTAMP",
					"TIMEUUID", "TO", "TOKEN", "THREE", "TRUNCATE", "TTL",
					"TWO", "TYPE", "UNLOGGED", "UPDATE", "USE", "USER",
					"USERS", "USING", "UUID", "VALUES", "VARCHAR", "VARINT",
					"WITH", "WRITETIME"));

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

	// <CHARS> ::= ^<EMPTY>[<CHARS>](a-Z0-9)
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
					|| Character.isDigit(text.codePointAt(index));

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

	// <COMA> ::= ,
	public Token isComma(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.COMMA, ",", text, required);

	}

	// <COMMAND> ::= <RESERVED WORDS> [<SELECTOR BLOCK>] [<SPACES>] [<SYMBOL>]
	// [<SPACES>] [<LITERAL>] [<SPACES>] [<COMMAND>]
	public Token isCommand(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.COMMAND);
		String content = text;

		Token leftToken = null;

		Token tokenReservedWords = isReservedWords(text, required);
		if (tokenReservedWords == null) {
			return null;
		}

		leftToken = tokenReservedWords;
		token.getSubTokens().add(leftToken);
		text = leftToken.getPosContent();

		Token selectorBlock = isSelectorBlock(leftToken.getPosContent(), false);
		if (selectorBlock != null) {

			leftToken = selectorBlock;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		Token tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		Token tokenSymbol = isSymbol(leftToken.getPosContent(), false);
		if (tokenSymbol != null) {

			leftToken = tokenSymbol;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		Token tokenLiteral = isLiteral(leftToken.getPosContent(), false);
		if (tokenLiteral != null) {
			leftToken = tokenLiteral;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		tokenSpaces = isSpaces(leftToken.getPosContent(), false);
		if (tokenSpaces != null) {
			leftToken = tokenSpaces;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		Token tokenAnotherComand = isCommand(text, false);
		if (tokenAnotherComand != null) {
			leftToken = tokenAnotherComand;
			token.getSubTokens().add(leftToken);
			updateNeighbors(leftToken, selectorBlock);
			text = leftToken.getPosContent();
		}

		content = content.substring(0, content.length() - text.length());
		token.setContent(content);
		token.setPosContent(text);

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
	public Token isConditionItem(String text, boolean required)
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
		text = left.getPosContent();

		Token tokenSpaces = isSpaces(tokenSelectorItem.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;
			text = left.getPosContent();
			token.getSubTokens().add(left);
		}

		Token tokenSymbol = isSymbol(left.getPosContent(), false);
		if (tokenSymbol == null) {
			if (required) {
				buildLexicalParserException(token, text, "Expected SYMBOL");
			}
			return null;
		}

		updateNeighbors(left, tokenSymbol);
		left = tokenSymbol;
		text = left.getPosContent();
		token.getSubTokens().add(left);

		tokenSpaces = isSpaces(left.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;
			text = left.getPosContent();
			token.getSubTokens().add(left);
		}

		Token tokenSelectorItem2 = isSelectorItem(left.getPosContent(), false);
		if (tokenSelectorItem2 == null) {
			return null;
		}

		updateNeighbors(left, tokenSelectorItem2);
		left = tokenSelectorItem2;
		text = left.getPosContent();
		token.getSubTokens().add(left);

		content = content.substring(0, content.length()
				- left.getPosContent().length());
		token.setContent(content);
		token.setPosContent(left.getPosContent());
		return token;
	}

	// <CONDITIONS> ::= <SPACES><CONDITION-ITEM>[<SPACES> <JOIN
	// CONDITION><CONDITIONS>]
	public Token isConditions(String text, boolean required)
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

	// <CQL> ::= [<SPACES>] <COMMAND> [ <SPACES> <CONDITION> ] [ <SPACES>]
	// [<COMMA> [ <SPACES>]]
	public Token isCQL(String cql) throws LexicalParserException {

		String left = cql;

		Token tokenCQL = new Token(TokenType.CQL);

		Token tokenSpace = isSpaces(cql, false);
		if (tokenSpace != null) {
			tokenCQL.setContent(cql);
			tokenCQL.getSubTokens().add(tokenSpace);
			left = tokenSpace.getPosContent();
		}

		Token tokenCommand = isCommand(left, true);
		if (tokenCommand == null) {
			throw new LexicalParserException("Token command not found");
		}

		left = tokenCommand.getPosContent();

		if (tokenSpace != null) {
			updateNeighbors(tokenSpace, tokenCommand);
		}

		tokenCQL.getSubTokens().add(tokenCommand);

		tokenSpace = isSpaces(left, false);
		if (tokenSpace != null) {
			tokenCQL.getSubTokens().add(tokenSpace);
			left = tokenSpace.getPosContent();
			updateNeighbors(tokenCommand, tokenSpace);

			Token tokenCondition = isCondition(left, false);
			if (tokenCondition != null) {
				tokenCQL.getSubTokens().add(tokenCondition);
				left = tokenSpace.getPosContent();
				updateNeighbors(tokenCommand, tokenSpace);

			}

		}

		// TODO: verifica se tem espaços
		// TODO: verifica se tem coma
		// TODO: verifica se tem espaços

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
	public Token isFunction(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.FUNCTION);
		String content = text;

		if (text.length() < 3) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		Token left = null;

		Token itemName = isItemName(text, required);

		if (itemName == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		left = itemName;
		token.getSubTokens().add(left);

		text = itemName.getPosContent();
		Token spaces = isSpaces(text, required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
			text = spaces.getPosContent();
		}

		Token startParameters = isStartParameters(text, required);

		if (startParameters == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}
		updateNeighbors(left, startParameters);
		left = startParameters;
		token.getSubTokens().add(left);
		text = left.getPosContent();

		spaces = isSpaces(text, required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
			text = left.getPosContent();
		}

		Token selectorItem = isSelectorItem(text, required);

		if (selectorItem != null) {
			updateNeighbors(left, selectorItem);
			left = selectorItem;
			token.getSubTokens().add(left);
			text = left.getPosContent();
		}

		spaces = isSpaces(text, required);

		if (spaces != null) {
			updateNeighbors(left, spaces);
			left = spaces;
			token.getSubTokens().add(left);
			text = left.getPosContent();
		}

		Token endParameters = isEndParameters(text, required);

		if (endParameters == null) {
			if (required) {
				buildLexicalParserException(token, text);
			}
			return null;
		}

		updateNeighbors(left, endParameters);
		left = endParameters;
		token.getSubTokens().add(left);
		text = left.getPosContent();

		String postContent = text.substring(1);

		content = content.substring(0, content.length() - postContent.length());
		token.setContent(content);
		token.setPosContent(postContent);
		return token;
	}

	// <HEXA> ::= (a-f)|(A-F)|(0-9)|-[<HEXA>]
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
			boolean number = character >= '0' && character <= '9';
			boolean hifen = character == '-';

			if (!characterSmall && !characterBig && !number && !hifen) {
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

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE INSENSITIVE>
	// | <ASTERISK>
	public Token isItemName(String posContent, boolean required)
			throws LexicalParserException {

		Token tokenItemName = new Token(TokenType.ITEMNAME);

		Token tokenCaseSensitive = isItemNameCaseSensitive(posContent, false);

		if (tokenCaseSensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseSensitive);
			tokenItemName.setContent(tokenCaseSensitive.getContent());
			tokenItemName.setPosContent(posContent.substring(tokenItemName
					.getContent().length()));
			return tokenItemName;
		}

		Token tokenCaseInsensitive = isItemNameCaseInsensitive(posContent,
				false);
		if (tokenCaseInsensitive != null) {

			tokenItemName.getSubTokens().add(tokenCaseInsensitive);
			tokenItemName.setContent(tokenCaseInsensitive.getContent());
			tokenItemName.setPosContent(posContent.substring(tokenItemName
					.getContent().length()));
			return tokenItemName;
		}

		Token asterisc = isAsterisk(posContent, required);
		if (asterisc == null) {
			return null;
		}

		tokenItemName.getSubTokens().add(asterisc);
		tokenItemName.setContent(asterisc.getContent());
		tokenItemName.setPosContent(posContent.substring(asterisc.getContent()
				.length()));
		return tokenItemName;
	}

	// <ITEM NAME CASE INSENSITIVE> ::= <CHARS>
	public Token isItemNameCaseInsensitive(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token(TokenType.ITEM_NAME_CASE_INSENSITIVE);

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
	 * <LITERAL> ::= (<NUMBER> | <STRING> | <HEXA>)^<CHARS>
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

	// <NUMBER> ::= (0-9)^<CHARS>[<NUMBER>]
	public Token isNumber(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.NUMBER);

		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			boolean number = character >= '0' && character <= '9';

			if (!number) {
				if (content.length() > 0) {

					// ^Chars
					if (Character.isAlphabetic(text.codePointAt(index))) {
						if (required) {
							buildLexicalParserException(token, text);
						}
						return null;
					}
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

	// <SELECTOR BLOCK> ::= [<SPACES>] <SELECTOR ITEM> [<SPACES>] [,<SELECTOR
	// BLOCK>]
	public Token isSelectorBlock(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token(TokenType.SELECTOR_BLOCK);
		String content = text;
		Token leftToken = null;

		Token spaces = isSpaces(text, false);
		if (spaces != null) {
			leftToken = spaces;
			text = leftToken.getPosContent();
			token.getSubTokens().add(leftToken);
		}

		Token selectorItem = isSelectorItem(text, required);
		if (selectorItem == null) {
			if (required) {
				buildLexicalParserException(token, content);
			}
			return null;
		}

		updateNeighbors(leftToken, selectorItem);

		leftToken = selectorItem;
		text = leftToken.getPosContent();
		token.getSubTokens().add(leftToken);

		spaces = isSpaces(text, false);
		if (spaces != null) {
			updateNeighbors(leftToken, spaces);
			leftToken = spaces;
			text = leftToken.getPosContent();
			token.getSubTokens().add(leftToken);
		}

		Token comma = isComma(text, false);
		if (comma != null) {

			updateNeighbors(leftToken, comma);
			leftToken = comma;
			text = leftToken.getPosContent();
			Token anotherSelectorBlock = isSelectorBlock(text, false);

			if (anotherSelectorBlock != null) {
				updateNeighbors(leftToken, anotherSelectorBlock);
				leftToken = anotherSelectorBlock;
				text = leftToken.getPosContent();
				token.getSubTokens().add(leftToken);
			}
		}

		content = content.substring(0, content.length() - text.length());

		token.setPosContent(text);
		token.setContent(content);
		return token;

	}

	/**
	 * <SELECTOR ITEM> ::= ^<RESERVED WORD> (<LITERAL> |<ITEM NAME> [<ACESSOR>
	 * <ITEM NAME>] | <INJECT> | <FUNCTION>)
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

		Token tokenLiteral = isLiteral(text, false);

		if (tokenLiteral != null) {
			left = tokenLiteral;
			token.getSubTokens().add(left);
			content = content.substring(0, content.length()
					- left.getPosContent().length());
			token.setContent(content);
			token.setPosContent(left.getPosContent());

			return token;
		}

		Token tokenItemName = isItemName(text, false);

		if (tokenItemName != null) {
			left = tokenItemName;
			token.getSubTokens().add(left);

			Token tokenAcessor = isAcessor(left.getPosContent(), false);

			if (tokenAcessor != null) {

				updateNeighbors(left, tokenAcessor);
				left = tokenAcessor;

				tokenItemName = isItemName(left.getPosContent(), false);

				if (tokenItemName != null) {
					token.getSubTokens().add(left);

					updateNeighbors(left, tokenItemName);
					left = tokenItemName;
					token.getSubTokens().add(left);

				}

			}

			content = content.substring(0, content.length()
					- left.getPosContent().length());
			token.setContent(content);
			token.setPosContent(left.getPosContent());
			return token;
		}

		Token tokenInject = isInject(text, false);
		if (tokenInject != null) {
			updateNeighbors(left, tokenInject);
			left = tokenInject;
			token.getSubTokens().add(left);
			content = content.substring(0, content.length()
					- left.getPosContent().length());
			token.setContent(content);
			token.setPosContent(left.getPosContent());

			return token;
		}

		Token tokenFunction = isFunction(text, false);

		if (tokenFunction != null) {
			updateNeighbors(left, tokenFunction);
			left = tokenFunction;
			token.getSubTokens().add(left);
			content = content.substring(0, content.length()
					- left.getPosContent().length());
			token.setContent(content);
			token.setPosContent(left.getPosContent());
			return token;
		}

		if (required) {
			buildLexicalParserException(token, text);
		}
		return null;

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

		if (!caseSensitive) {
			text = text.toUpperCase();
			singleText = singleText.toUpperCase();
		}

		if (text.length() < singleText.length()) {
			if (required) {

				buildLexicalParserException(token, text);

			}
			return null;
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

		token.setContent(singleText);
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

	// <WHERE> ::= u(WHERE)
	public Token isWhere(String text, boolean required)
			throws LexicalParserException {

		return isSingleText(TokenType.WHERE, "WHERE", text, false, null,
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

}
