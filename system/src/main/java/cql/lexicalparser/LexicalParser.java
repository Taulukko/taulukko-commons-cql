package cql.lexicalparser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

	// <CQL> ::= [<SPACES>] <COMMAND> [ <SPACES> <CONDITION> ] [ <SPACES>]
	// [<COMA> [ <SPACES>]]
	public Token isCQL(String cql) throws LexicalParserException {

		String left = cql;

		Token tokenCQL = null;
		Token tokenSpace = isSpaces(cql, false);
		if (tokenSpace != null) {
			tokenCQL = new Token();
			tokenCQL.setContent(cql);
			tokenCQL.setType(TokenType.CQL);

			tokenCQL.getSubTokens().add(tokenSpace);

			left = tokenSpace.getPosContent();
		}

		Token tokenComamand = isCommand(left, true);
		if (tokenComamand == null) {
			throw new LexicalParserException("Token command not found");
		}

		left = tokenComamand.getPosContent();

		if (tokenCQL == null) {
			tokenCQL = new Token();
			tokenCQL.setContent(cql);
			tokenCQL.setType(TokenType.CQL);
		}

		if (tokenSpace != null) {
			updateNeighbors(tokenSpace, tokenComamand);
		}

		tokenCQL.getSubTokens().add(tokenComamand);

		tokenSpace = isSpaces(left, false);
		if (tokenSpace != null) {
			tokenCQL.getSubTokens().add(tokenSpace);
			left = tokenSpace.getPosContent();
			updateNeighbors(tokenComamand, tokenSpace);

			Token tokenCondition = isCondition(left, false);
			if(tokenCondition!=null)
			{
				tokenCQL.getSubTokens().add(tokenCondition);
				left = tokenSpace.getPosContent();
				updateNeighbors(tokenComamand, tokenSpace);

			}

		}

		// TODO: verifica se tem condition
		// TODO: verifica se tem espaços
		// TODO: verifica se tem coma
		// TODO: verifica se tem espaços

		return tokenCQL;
	}

	// <CONDITION> ::= <WHERE> <SPACES> <CONDITIONS> <SPACES>
	public Token isCondition(String content, boolean required)
			throws LexicalParserException {
		Token tokenWhere = isWhere(content, required);
		if (tokenWhere == null) {
			return null;
		}

		Token tokenSpace = isSpaces(tokenWhere.getPosContent(), required);
		if (tokenSpace == null) {
			return null;
		}

		updateNeighbors(tokenWhere, tokenSpace);

		Token tokenConditions = isConditions(tokenSpace.getPosContent(),
				required);
		// TODO: falta verificar
		return null;
	}

	// <CONDITIONS> ::= <CONDITION-ITEM>[<SPACES> <JOIN CONDITION> <SPACES>
	// <CONDITIONS>]
	public Token isConditions(String content, boolean required)
			throws LexicalParserException {
		Token tokenConditionItem = isConditionItem(content, required);

		// TODO: falta verificar
		// TODO: falta analisar espacos
		// TODO: falta analisar join
		// TODO: falta analisar condition
		// TODO: falta analisar spaces
		// TODO: falta analisar conditions

		return null;
	}

	// <CONDITION-ITEM>::= <SELECTOR ITEM>[<SPACES>]<SYMBOL>[<SPACES>]<SELECTOR
	// ITEM>
	public Token isConditionItem(String content, boolean required)
			throws LexicalParserException {

		StringBuffer contentBuffer = new StringBuffer();

		Token tokenSelectorItem = isSelectorItem(content, required);

		if (tokenSelectorItem == null) {
			return null;
		}

		Token left = tokenSelectorItem;

		contentBuffer.append(tokenSelectorItem.getContent());

		Token tokenSpaces = isSpaces(tokenSelectorItem.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;
			contentBuffer.append(tokenSpaces.getContent());
		}

		Token tokenSymbol = isSymbol(tokenSpaces.getPosContent(), required);
		if (tokenSymbol == null) {
			return null;
		}

		contentBuffer.append(tokenSymbol.getContent());

		updateNeighbors(left, tokenSymbol);
		left = tokenSymbol;

		tokenSpaces = isSpaces(tokenSymbol.getPosContent(), false);
		if (tokenSpaces != null) {
			updateNeighbors(left, tokenSpaces);
			left = tokenSpaces;
			contentBuffer.append(tokenSpaces.getContent());
		}

		Token tokenSelectorItem2 = isSelectorItem(tokenSpaces.getPosContent(),
				required);
		if (tokenSelectorItem2 == null) {
			return null;
		}

		contentBuffer.append(tokenSelectorItem2.getContent());

		updateNeighbors(left, tokenSelectorItem2);
		left = tokenSelectorItem2;

		Token tokenConditionItem = new Token();

		tokenConditionItem.setContent(contentBuffer.toString());
		tokenConditionItem.setType(TokenType.CONDITION_ITEM);
		tokenConditionItem.setPosContent(tokenSelectorItem2.getPosContent());

		tokenConditionItem.getSubTokens().add(tokenSelectorItem);
		left = tokenSelectorItem;

		while (left.getAfter() != null) {
			tokenConditionItem.getSubTokens().add(left.getAfter());
			left = left.getAfter();
		}

		return tokenConditionItem;
	}

	// <WHERE> ::= WHERE
	public Token isWhere(String content, boolean required)
			throws LexicalParserException {
		Token token = new Token();
		token.setType(TokenType.WHERE);

		if (content.length() == 0 || !content.toUpperCase().startsWith("WHERE")) {
			if (required) {
				throw new LexicalParserException("Invalid WHERE in [" + content
						+ "]");

			}
			return null;
		}

		int length = "WHERE".length();

		token.setContent(content.substring(0, length));

		token.setPosContent(content.substring(length));

		return token;
	}

	private void updateNeighbors(Token before, Token after) {
		before.setAfter(after);
		after.setBefore(before);
	}

	// <COMMAND> ::= <RESERVED WORDS> <SPACES> [<SELECTOR_ITEM>] [<SPACES>]
	// [<SYMBOL>] [<SPACES>]
	// [<LITERAL>] [<SPACES>] [<COMMAND>]
	public Token isCommand(String command, boolean required)
			throws LexicalParserException {

		Token tokenReservedWords = isReservedWords(command, required);
		if (tokenReservedWords == null) {
			return null;
		}

		Token tokenSpaces = isSpaces(tokenReservedWords.getPosContent(),
				required);
		if (tokenSpaces == null) {
			return null;
		}

		updateNeighbors(tokenReservedWords, tokenSpaces);

		Token token = new Token();
		StringBuffer content = new StringBuffer();
		token.setType(TokenType.COMMAND);
		content.append(tokenReservedWords.getContent());
		token.getSubTokens().add(tokenReservedWords);
		content.append(tokenSpaces.getContent());
		token.getSubTokens().add(tokenSpaces);

		String left = tokenSpaces.getPosContent();

		Token leftToken = null;
		leftToken = tokenSpaces;
		Token tokenSelectorItem = isSelectorItem(left, false);

		if (tokenSelectorItem != null) {

			updateNeighbors(leftToken, tokenSelectorItem);
			leftToken = tokenSelectorItem;
			content.append(tokenSelectorItem.getContent());
			token.getSubTokens().add(tokenSelectorItem);
			left = tokenSelectorItem.getPosContent();
		}

		tokenSpaces = isSpaces(left, false);
		if (tokenSpaces != null) {

			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			left = leftToken.getPosContent();
			content.append(tokenSpaces.getContent());
			token.getSubTokens().add(tokenSpaces);

		}

		Token tokenSymbol = isSymbol(left, false);

		if (tokenSymbol != null) {
			updateNeighbors(leftToken, tokenSymbol);
			leftToken = tokenSymbol;
			left = leftToken.getPosContent();
			content.append(tokenSymbol.getContent());
			token.getSubTokens().add(tokenSymbol);
			left = tokenSymbol.getPosContent();
		}

		tokenSpaces = isSpaces(left, false);
		if (tokenSpaces != null) {

			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			left = leftToken.getPosContent();
			content.append(tokenSpaces.getContent());
			token.getSubTokens().add(tokenSpaces);
		}

		Token tokenLiteral = isLiteral(left, false);
		if (tokenLiteral != null) {
			updateNeighbors(leftToken, tokenLiteral);
			leftToken = tokenLiteral;
			left = leftToken.getPosContent();

			content.append(tokenLiteral.getContent());
			token.getSubTokens().add(tokenLiteral);
			left = tokenLiteral.getPosContent();
		}

		tokenSpaces = isSpaces(left, false);
		if (tokenSpaces != null) {

			updateNeighbors(leftToken, tokenSpaces);
			leftToken = tokenSpaces;
			left = leftToken.getPosContent();
			content.append(tokenSpaces.getContent());
			token.getSubTokens().add(tokenSpaces);
		}

		Token tokenAnotherComand = isCommand(left, false);
		if (tokenAnotherComand != null) {
			updateNeighbors(leftToken, tokenAnotherComand);
			leftToken = tokenAnotherComand;
			left = leftToken.getPosContent();

			content.append(tokenAnotherComand.getContent());
			token.getSubTokens().add(tokenAnotherComand);
			left = tokenAnotherComand.getPosContent();
		}

		if (left.length() > 0) {
			throw new LexicalParserException("Inexpected string in [" + left
					+ "]");
		}

		token.setContent(content.toString());
		token.setPosContent(command.substring(content.length()));

		return token;
	}

	// <SYMBOL> ::= = | + | - | / | * | ( | ) | { | } | , [ | ]
	public Token isSymbol(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token();
		token.setType(TokenType.SYMBOL);

		if (text.length() == 0 || !SYMBOLS.contains(text.charAt(0))) {
			if (required) {
				throw new LexicalParserException("Invalid symbol in [" + text
						+ "]");

			}
			return null;
		}

		token.setContent(String.valueOf(text.charAt(0)));

		token.setPosContent(text.substring(1));

		return token;
	}

	// *<SELECTOR ITEM> ::= [<ITEM NAME> <ACESSOR>] <ITEM NAME> | <INJECT> |
	// <LITERAL>
	public Token isSelectorItem(String text, boolean required)
			throws LexicalParserException {

		if (text.length() == 0) {
			if (required) {
				throw new LexicalParserException("Expected selector item in ["
						+ text + "]");

			} else {
				return null;
			}
		}

		Token tokenItemName = isItemName(text, false);
		Token token = new Token();

		if (tokenItemName == null) {
			Token tokenInject = isInject(text, false);
			if (tokenInject != null) {
				token.getSubTokens().add(tokenInject);
				token.setContent(tokenInject.getContent());
				token.setPosContent(text.substring(token.getContent().length()));
				token.setType(TokenType.SELECTOR_ITEM);
				return token;
			}

			Token tokenLiteral = isLiteral(text, required);
			if (tokenLiteral == null) {
				return null;
			}
			token.getSubTokens().add(tokenLiteral);
			token.setContent(tokenLiteral.getContent());
			token.setPosContent(text.substring(token.getContent().length()));
			token.setType(TokenType.SELECTOR_ITEM);
			return token;
		}

		StringBuffer sb = new StringBuffer();

		token.getSubTokens().add(tokenItemName);
		sb.append(tokenItemName.getContent());

		Token tokenAcessor = isAcessor(tokenItemName.getPosContent(), false);

		if (tokenAcessor != null) {

			Token anotherTokenItemName = isItemName(
					tokenAcessor.getPosContent(), false);
			if (anotherTokenItemName != null) {
				updateNeighbors(tokenItemName, tokenAcessor);
				updateNeighbors(tokenAcessor, anotherTokenItemName);

				token.getSubTokens().add(tokenAcessor);
				token.getSubTokens().add(anotherTokenItemName);

				sb.append(tokenAcessor.getContent());
				sb.append(anotherTokenItemName.getContent());
			}

		}

		token.setContent(sb.toString());
		token.setPosContent(text.substring(token.getContent().length()));
		token.setType(TokenType.SELECTOR_ITEM);
		return token;

	}

	// <INJECT> ::= ?
	public Token isInject(String text, boolean required)
			throws LexicalParserException {
		Token token = new Token();
		token.setType(TokenType.INJECT);

		if (text.length() == 0 || text.charAt(0) != '?') {
			if (required) {
				throw new LexicalParserException("Invalid inject in [" + text
						+ "]");

			}
			return null;
		}

		token.setContent(String.valueOf('?'));

		token.setPosContent(text.substring(1));

		return token;
	}

	// <LITERAL> ::= <NUMBER> | <STRING> | <HEXA>
	public Token isLiteral(String text, boolean required)
			throws LexicalParserException {

		Token token = new Token();
		token.setType(TokenType.LITERAL);

		if (text.length() == 0) {
			if (required) {
				throw new LexicalParserException("Expected literal in [" + text
						+ "]");
			} else {
				return null;
			}
		}

		Token tokenNumber = isNumber(text, false);
		if (tokenNumber != null) {
			token.setContent(tokenNumber.getContent());
			token.setPosContent(tokenNumber.getPosContent());
			token.getSubTokens().add(tokenNumber);
			return token;
		}

		Token tokenString = isString(text, false);
		if (tokenString != null) {
			token.setContent(tokenString.getContent());
			token.setPosContent(tokenString.getPosContent());
			token.getSubTokens().add(tokenString);
			return token;
		}

		Token tokenHexa = isHexa(text, required);
		if (tokenHexa == null) {
			return null;
		}

		token.setContent(tokenHexa.getContent());
		token.setPosContent(tokenHexa.getPosContent());
		token.getSubTokens().add(tokenHexa);
		return token;
	}

	// <HEXA> ::= (a-f)|(A-F)|(0-9)|-[<HEXA>]
	// Notes: The position of the hyphen will not be validated
	public Token isHexa(String text, boolean required)
			throws LexicalParserException {
		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
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

		Token ret = new Token();
		String contentStr = content.toString();
		ret.setContent(contentStr);
		ret.setPosContent(text.substring(contentStr.length()));
		ret.setType(TokenType.HEX);
		return ret;
	}

	// <STRING> ::= '<INPUT CHARACTER>'
	public Token isString(String text, boolean required)
			throws LexicalParserException {
		if (text.length() < 3) {
			if (required) {
				throw new LexicalParserException("Invalid String in [" + text
						+ "]");
			}
			return null;
		}

		if (!text.startsWith("'")) {
			if (required) {
				throw new LexicalParserException("Expected start with ' in ["
						+ text + "]");
			}
			return null;
		}

		String preContent = text.substring(1);

		Token tokenString = isInputcharacter(preContent, required);

		if (tokenString == null) {
			return null;
		}

		if (!tokenString.getPosContent().startsWith("'")) {
			if (required) {
				throw new LexicalParserException("Expected start with ' in ["
						+ tokenString.getPosContent() + "]");
			}
			return null;
		}

		Token token = new Token();
		StringBuffer sb = new StringBuffer();
		sb.append("'");
		sb.append(tokenString.getContent());
		sb.append("'");
		token.setContent(sb.toString());
		token.setPosContent(tokenString.getPosContent().substring(1));
		token.getSubTokens().add(tokenString);
		token.setType(TokenType.STRING);
		return token;
	}

	/*
	 * 
	 * <INPUT CHARACTER> :: = (*) Notes: 1-) Any character except ' unless is
	 * part of a double quoted ''
	 */
	public Token isInputcharacter(String text, boolean required) {
		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			if (character == '\'') {
				int nextIndex = index + 1;
				if (nextIndex >= text.length()) {
					break;
				}
				char nextCharacter = text.charAt(nextIndex);
				if (nextCharacter != '\'') {
					break;
				}
				index++;
				content.append("''");
				continue;
			}

			content.append(character);
		}

		Token ret = new Token();
		String contentStr = content.toString();
		ret.setContent(contentStr);
		ret.setPosContent(text.substring(contentStr.length()));
		ret.setType(TokenType.INPUT_CHARACTER);
		return ret;
	}

	// *<NUMBER> ::= (0-9)[<NUMBER>]
	public Token isNumber(String text, boolean required)
			throws LexicalParserException {

		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			boolean number = character >= '0' && character <= '9';

			if (!number) {
				if (content.length() > 0) {
					break;
				}
				if (required) {
					throw new LexicalParserException("Expected 0-9 in '" + text
							+ "'");
				}
				return null;
			}

			content.append(character);
		}

		Token ret = new Token();
		String contentStr = content.toString();
		ret.setContent(contentStr);
		ret.setPosContent(text.substring(contentStr.length()));
		ret.setType(TokenType.NUMBER);
		return ret;
	}

	// <ACESSOR> ::= .
	public Token isAcessor(String posContent, boolean required)
			throws LexicalParserException {
		if (posContent.length() == 0 || !posContent.startsWith(".")) {
			if (required) {
				throw new LexicalParserException("Expected token acessor in '"
						+ posContent + "'");
			}
			return null;
		}
		Token token = new Token();
		token.setContent(".");
		token.setPosContent(posContent.substring(1));
		token.setType(TokenType.ACESSOR);
		return token;
	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE INSENSITIVE>
	public Token isItemName(String posContent, boolean required)
			throws LexicalParserException {
		Token tokenCaseSensitive = isItemNameCaseSensitive(posContent, false);

		Token tokenItemName = new Token();
		tokenItemName.setType(TokenType.ITEMNAME);

		if (tokenCaseSensitive != null) {
			tokenItemName.getSubTokens().add(tokenCaseSensitive);
			tokenItemName.setContent(tokenCaseSensitive.getContent());
			tokenItemName.setPosContent(posContent.substring(tokenItemName
					.getContent().length()));
			return tokenItemName;
		}

		Token tokenCaseInsensitive = isItemNameCaseInsensitive(posContent,
				required);
		if (tokenCaseInsensitive == null) {
			return null;
		}
		tokenItemName.getSubTokens().add(tokenCaseInsensitive);
		tokenItemName.setContent(tokenCaseInsensitive.getContent());
		tokenItemName.setPosContent(posContent.substring(tokenItemName
				.getContent().length()));
		return tokenItemName;

	}

	// <ITEM NAME CASE INSENSITIVE> ::= <CHARS>
	public Token isItemNameCaseInsensitive(String text, boolean required)
			throws LexicalParserException {
		Token charToken = isChars(text, required);
		if (charToken == null) {
			return null;
		}

		Token token = new Token();
		token.getSubTokens().add(charToken);
		token.setContent(charToken.getContent());
		token.setPosContent(text.substring(charToken.getContent().length()));
		token.setType(TokenType.ITEMNAME_CASE_INSENSITIVE);
		return token;

	}

	// <ITEM NAME CASE SENSITIVE> ::= "<CHARS>"
	public Token isItemNameCaseSensitive(String content, boolean required)
			throws LexicalParserException {

		String tokenContent = consume(content, ' ', '.');

		if (!tokenContent.startsWith("\"")) {
			if (required) {
				throw new LexicalParserException(
						"Expected start with '\"' in '" + content + "'");
			}
			return null;
		}

		if (!tokenContent.endsWith("\"")) {
			if (required) {
				throw new LexicalParserException("Expected end with '\"' in '"
						+ content + "'");
			}
			return null;
		}

		String posibleCharsContent = tokenContent.substring(1,
				tokenContent.length() - 1);
		Token tokenChars = isChars(posibleCharsContent, required);
		if (tokenChars == null) {
			if (required) {
				throw new LexicalParserException("Expected token chars in '"
						+ posibleCharsContent + "'");
			}
			return null;
		}

		if (tokenChars.getPosContent().length() > 0) {
			if (required) {
				throw new LexicalParserException(
						"Expected end with ITEM NAME CASE SENSITIVE in "
								+ tokenContent);
			}
			return null;
		}

		Token token = new Token();
		token.setContent(tokenContent);
		token.setPosContent(content.substring(tokenContent.length()));
		token.setType(TokenType.ITEMNAME_CASE_SENSITIVE);
		token.getSubTokens().add(tokenChars);

		return token;
	}

	// <CHARS> ::= [<CHARS>](a-Z0-9)
	public Token isChars(String text, boolean required)
			throws LexicalParserException {
		StringBuffer content = new StringBuffer();

		for (int index = 0; index < text.length(); index++) {
			char character = text.charAt(index);
			boolean characterSmall = character >= 'a' && character <= 'z';
			boolean characterBig = character >= 'A' && character <= 'Z';
			boolean number = character >= '0' && character <= '9';

			if (!characterSmall && !characterBig && !number) {
				if (content.length() > 0) {
					break;
				}
				if (required) {
					throw new LexicalParserException("Expected a-Z0-9 in '"
							+ text + "'");
				}
				return null;
			}

			content.append(character);
		}

		Token ret = new Token();
		String contentStr = content.toString();
		ret.setContent(contentStr);
		ret.setPosContent(text.substring(contentStr.length()));
		ret.setType(TokenType.CHARS);
		return ret;
	}

	// <RESERVED WORDS>: <RESERVED WORD> [<SPACES> <RESERVED WORDS>]
	public Token isReservedWords(String command, boolean required)
			throws LexicalParserException {
		Token ret = new Token();
		ret.setContent(command);
		ret.setType(TokenType.RESERVED_WORDS);

		Token tokenReservedWord = isReservedWord(command, required);

		if (tokenReservedWord == null) {
			return null;
		}

		StringBuffer content = new StringBuffer();

		ret.getSubTokens().add(tokenReservedWord);
		content.append(tokenReservedWord.getContent());
		ret.setContent(content.toString());

		Token spaces = isSpaces(tokenReservedWord.getPosContent(), false);

		while (spaces != null) {
			Token tokenReservedWordPrevious = tokenReservedWord;

			tokenReservedWord = isReservedWord(spaces.getPosContent(), false);
			if (tokenReservedWord == null) {
				break;
			}
			ret.getSubTokens().add(spaces);
			updateNeighbors(tokenReservedWordPrevious, spaces);
			content.append(spaces.getContent());

			ret.getSubTokens().add(tokenReservedWord);
			updateNeighbors(spaces, tokenReservedWord);
			content.append(tokenReservedWord.getContent());
			ret.setContent(content.toString());

			spaces = isSpaces(tokenReservedWord.getPosContent(), false);
		}

		ret.setContent(content.toString());
		ret.setPosContent(command.substring(content.length()));
		return ret;
	}

	// <RESERVED WORD> ::= SELECT,INSERT,...
	public Token isReservedWord(String command, boolean required)
			throws LexicalParserException {
		String word = consume(command, ' ');
		if (!RESERVED_WORDS.contains(word.toUpperCase())) {
			if (required) {
				throw new LexicalParserException("Expected reserved Word in '"
						+ word + "'");
			}
			return null;
		}

		Token tokenReservedWord = new Token();
		tokenReservedWord.setContent(word);
		tokenReservedWord.setPosContent(command.substring(word.length()));
		tokenReservedWord.setType(TokenType.RESERVED_WORD);
		return tokenReservedWord;

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

	// <SPACES> ::= <SPACE> [<SPACES>]
	public Token isSpaces(String space, boolean required)
			throws LexicalParserException {
		StringBuffer content = new StringBuffer();

		for (int index = 0; index < space.length(); index++) {
			if (space.charAt(index) == ' ') {
				content.append(' ');
			} else {
				break;
			}
		}

		if (content.length() == 0) {
			if (required) {
				throw new LexicalParserException("Expected white space in '"
						+ space + "'");
			}
			return null;
		}

		Token tokenSpace = new Token();
		tokenSpace.setContent(content.toString());
		tokenSpace.setPosContent(space.substring(content.length()));
		tokenSpace.setType(TokenType.SPACES);
		return tokenSpace;
	}

}
