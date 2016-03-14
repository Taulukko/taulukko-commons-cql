package cql.lexicalparser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cql.Token;
import cql.TokenType;

public class LexicalParserTest {

	private LexicalParser lexicalParser = null;

	// <ACESSOR> ::= .
	@Test
	public void acessor() throws LexicalParserException {
		String cql = " . ";
		Token token = lexicalParser.isAcessor(cql, false);
		Assert.assertNull(token);

		cql = "S ";
		token = lexicalParser.isAcessor(cql, false);
		Assert.assertNull(token);

		cql = "";
		token = lexicalParser.isAcessor(cql, false);
		Assert.assertNull(token);

		cql = ". teste";
		token = lexicalParser.isAcessor(cql, true);
		token = lexicalParser.isAcessor(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(".", token.getContent());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(TokenType.ACESSOR, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());
	}

	// <AND> ::= u(AND)
	@Test
	public void and() throws LexicalParserException {
		String cql = "algo,test";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = "AnD test";
		token = lexicalParser.isAnd(cql, false);
		token = lexicalParser.isAnd(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("AND", token.getContent());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals(" test", token.getPosContent());

	}

	@Before
	public void cleanup() throws LexicalParserException {
		lexicalParser = new LexicalParser();
	}

	// <COMA> ::= ,
	@Test
	public void comma() throws LexicalParserException {
		String cql = "algo,teste";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = ",teste";
		token = lexicalParser.isComma(cql, false);
		token = lexicalParser.isComma(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(",", token.getContent());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals("teste", token.getPosContent());

	}

	// <<COMMAND> ::= <RESERVED WORDS> [<SELECTOR BLOCK>] [<SPACES>] [<SYMBOL>]
	// [<SPACES>] [<LITERAL>] [<SPACES>] [<COMMAND>]
	@Test
	public void command() throws LexicalParserException {

		// start symbol
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// start string
		cql = "'. teste'";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// start space
		cql = " . teste'";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// start literal
		cql = "age . teste'";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// start where
		cql = "where age . teste'";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// reserved word but not spaces
		cql = "select";
		Assert.assertNotNull(lexicalParser.isCommand(cql, false));

		cql = "selectinsert";
		Assert.assertNull(lexicalParser.isCommand(cql, false));

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select ";
		Token token = lexicalParser.isCommand(cql, true);
		Assert.assertNotNull(token);

		cql = "select insert";
		Assert.assertNotNull(lexicalParser.isCommand(cql, false));

		// only reserved words
		cql = "select insert  ";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select insert  ", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(TokenType.RESERVED_WORDS,
				token.getSubTokens().get(0).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());

		// add selector item
		cql = "select insert  accounts.id";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select insert  accounts.id", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(TokenType.RESERVED_WORDS,
				token.getSubTokens().get(0).getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());

		// add symbol
		cql = "select insert  accounts.id=";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select insert  accounts.id=", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.RESERVED_WORDS,
				token.getSubTokens().get(0).getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.SYMBOL, token.getSubTokens().get(2)
				.getType());

		cql = "select insert  accounts.id = ";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select insert  accounts.id = ", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		Assert.assertEquals(4, token.getSubTokens().size());
		Assert.assertEquals(TokenType.RESERVED_WORDS,
				token.getSubTokens().get(0).getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());

	}

	/**
	 * <CONDITION> ::= <WHERE> <CONDITIONS>
	 */
	@Test
	public void condition() throws LexicalParserException {
		String cql = "   WHERE    X=3 AND X=5   ";
		Assert.assertNull(lexicalParser.isCondition(cql, false));
		cql = "WHEREX=3 AND X=5 test";
		Assert.assertNull(lexicalParser.isCondition(cql, false));
		cql = "WHERE    X=3 AND X=5   test";
		Token token = lexicalParser.isCondition(cql, false);
		token = lexicalParser.isCondition(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.getType(), TokenType.CONDITION);
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals("   test", token.getPosContent());
		Assert.assertEquals("WHERE    X=3 AND X=5", token.getContent());
		Assert.assertEquals("WHERE", token.getSubTokens().get(0).getContent());

		Assert.assertEquals("    X=3 AND X=5", token.getSubTokens().get(1)
				.getContent());
		Assert.assertEquals("    ", token.getSubTokens().get(1).getSubTokens().get(0).getContent());

	}

	/**
	 * <CONDITION-ITEM>::= <SELECTOR ITEM>[<SPACES>]<SYMBOL>[<SPACES>]<SELECTOR
	 * ITEM>
	 */
	@Test
	public void conditionItem() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isConditionItem(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isConditionItem(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isConditionItem(cql, false));

		cql = "accounts.id = ";
		Assert.assertNull(lexicalParser.isConditionItem(cql, false));

		cql = "accounts.id =  3    teste";
		Token token = lexicalParser.isConditionItem(cql, true);
		token = lexicalParser.isConditionItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id =  3", token.getContent());
		Assert.assertEquals("    teste", token.getPosContent());
		Assert.assertEquals(TokenType.CONDITION_ITEM, token.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getSubTokens()
				.get(0).getType());
		boolean hasSymbol = false;
		for (Token child : token.getSubTokens()) {
			if (child.getType().equals(TokenType.SYMBOL)) {
				hasSymbol = true;
				break;
			}
		}

		Assert.assertTrue(hasSymbol);

		int selectItem = 0;

		for (Token child : token.getSubTokens()) {
			if (child.getType().equals(TokenType.SELECTOR_ITEM)) {
				selectItem++;
				continue;
			}
		}
		Assert.assertEquals(1, selectItem);
	}

	/**
	 * // <CONDITIONS> ::= <SPACES><CONDITION-ITEM>[<SPACES> <JOIN CONDITION> <CONDITIONS>]
	 */
	@Test
	public void conditions() throws LexicalParserException {
		String cql = "X=3 AND  X=5";
		Assert.assertNull(lexicalParser.isConditions(cql, false));
		cql = " X=3 test";
		Token token = lexicalParser.isConditions(cql, false);
		token = lexicalParser.isConditions(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.getSubTokens().toString(), 2, token
				.getSubTokens().size());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.CONDITION_ITEM,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(" test", token.getPosContent());
		cql = " X=3 AND Y=3 test";
		token = lexicalParser.isConditions(cql, false);
		token = lexicalParser.isConditions(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.getSubTokens().toString(), 5, token
				.getSubTokens().size());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.CONDITION_ITEM,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.JOIN_CONDITION,
				token.getSubTokens().get(3).getType());
		Assert.assertEquals(token.getSubTokens().toString(),TokenType.CONDITIONS, token.getSubTokens().get(4)
				.getType());
		Assert.assertEquals(" test", token.getPosContent());

	}

	// <CQL> ::= [<SPACES>] <COMMAND> [ <SPACES> <CONDITION> ] [ <SPACES>]
	// [<COMA> [ <SPACES>]]
	@Test
	public void cql() throws LexicalParserException {
		String cql = "select count(*) as valid from users where user_token = ?";
		Assert.assertNotNull(lexicalParser.isCQL(cql));

	}

	// <END_PARAMETERS>::=)
	@Test
	public void endParameters() throws LexicalParserException {
		String cql = ")";
		Token start = lexicalParser.isEndParameters(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndParameters(cql, false);
		Assert.assertNotNull(start);
		cql = ") test ";
		start = lexicalParser.isEndParameters(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndParameters(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals(")", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " )test ";
		start = lexicalParser.isEndParameters(cql, false);
		Assert.assertNull(start);

	}

	// <FUNCTION>::= <ITEM NAME>[<SPACES>]<START_PARAMETERS>[<SPACES>][<SELECTOR
	// ITEM>][<SPACES>]<END_PARAMETERS>
	@Test
	public void function() throws LexicalParserException {
		String cql = "count(*) ";
		Assert.assertNotNull(lexicalParser.isFunction(cql, false));

	}

	@Test
	public void hexa() throws LexicalParserException {
		String cql = " 0x1234abcde adsfasdf ";
		Token token = lexicalParser.isHexa(cql, false);
		Assert.assertNull(token);
		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNull(token);

		cql = "0X1234Éabcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234", token.getContent());
		Assert.assertEquals("Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "0X1234abcde² adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abcde", token.getContent());
		Assert.assertEquals("² adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "0X1234abcde$ adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abcde", token.getContent());
		Assert.assertEquals("$ adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "\"0X1234abcde\" adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNull(token);

		cql = "0X1234abcde adsfasdf ";
		token = lexicalParser.isHexa(cql, true);
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abcde", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "0X1234abc1-3 1234Éabcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abc1-3", token.getContent());
		Assert.assertEquals(" 1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "0X1234abc1-34567abc-de 1234Éabcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abc1-34567abc-de", token.getContent());
		Assert.assertEquals(" 1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "0x1234abc1-34567abc-de 1234Éabcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0x1234abc1-34567abc-de", token.getContent());
		Assert.assertEquals(" 1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

	}

	/**
	 * <INJECT> ::= ?
	 */
	@Test
	public void inject() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "'?. teste";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "?. \n adf '' ; * ?  teste' 123";
		Token token = lexicalParser.isInject(cql, true);
		token = lexicalParser.isInject(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("?", token.getContent());
		Assert.assertEquals(". \n adf '' ; * ?  teste' 123",
				token.getPosContent());
		Assert.assertEquals(TokenType.INJECT, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertNull(token.getAfter());
		Assert.assertNull(token.getBefore());
	}

	/**
	 * <INPUT CHARACTER EXCEPT SINGLE> ::= (^<SINGLE QUOTED>%s)[<INPUT CHARACTER
	 * EXCEPT SINGLE>]
	 */
	@Test
	public void inputCharacterExeptSingle() throws LexicalParserException {
		String cql = ". teste";
		Token token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(". teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());
	}

	/**
	 * <INPUT CHARACTER EXCEPT DOUBLE> ::= (^<DOUBLE QUOTED>%s)[<INPUT CHARACTER
	 * EXCEPT DOUBLE>]
	 */
	@Test
	public void inputCharacterExeptDouble() throws LexicalParserException {
		String cql = ". teste";
		Token token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(". teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());
	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE INSENSITIVE>
	@Test
	public void itemName() throws LexicalParserException {
		String cql = " \"1234abcde\" adsfasdf ";
		Token token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"é1234abcde\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234Éabcde\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde²\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde$\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde\" adsfasdf ";
		token = lexicalParser.isItemName(cql, true);
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("\"1234abcde\"", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ITEMNAME, token.getType());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_SENSITIVE, token
				.getSubTokens().get(0).getType());
		Assert.assertNull(token.getSubTokens().get(0).getAfter());
		Assert.assertNull(token.getSubTokens().get(0).getBefore());

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isItemName(cql, true);
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("1234abcde", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ITEMNAME, token.getType());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_INSENSITIVE, token
				.getSubTokens().get(0).getType());

	}

	// <ITEM NAME CASE SENSITIVE> ::= "<CHARS>"
	@Test
	public void itemNameCaseSensitive() throws LexicalParserException {
		String cql = " \"1234abcde\" adsfasdf ";
		Token token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"é1234abcde\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234Éabcde\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde²\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde$\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, true);
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("\"1234abcde\"", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_SENSITIVE, token.getType());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getType());
		Assert.assertNull(token.getSubTokens().get(0).getAfter());
		Assert.assertNull(token.getSubTokens().get(0).getBefore());

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);
	}

	/**
	 * <INPUT CHARACTER> :: = (*) Notes: 1-) Any character except ' unless is
	 * part of a double quoted ''
	 */
	@Test
	public void literal() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isLiteral(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isLiteral(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isLiteral(cql, false));

		cql = "'. \n adf '' ; * ?  teste' 123";
		Token token = lexicalParser.isLiteral(cql, true);
		token = lexicalParser.isLiteral(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.LITERAL, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.STRING, token.getSubTokens().get(0)
				.getType());

		cql = "'. \n adf '' ; * ?  teste' 123'";
		token = lexicalParser.isLiteral(cql, true);
		token = lexicalParser.isLiteral(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.LITERAL, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.STRING, token.getSubTokens().get(0)
				.getType());

		cql = " 1234abcde adsfasdf ";
		token = lexicalParser.isLiteral(cql, false);
		Assert.assertNull(token);

		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isLiteral(cql, false);
		Assert.assertNull(token);

		cql = "1234Éabcde adsfasdf ";
		token = lexicalParser.isLiteral(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("1234", token.getContent());
		Assert.assertEquals("Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.LITERAL, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.NUMBER, token.getSubTokens().get(0)
				.getType());

		cql = "1234abc1-3 1234Éabcde adsfasdf ";
		token = lexicalParser.isLiteral(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("1234", token.getContent());
		Assert.assertEquals("abc1-3 1234Éabcde adsfasdf ",
				token.getPosContent());
		Assert.assertEquals(TokenType.LITERAL, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.NUMBER, token.getSubTokens().get(0)
				.getType());

	}

	@Test
	public void number() throws LexicalParserException {
		String cql = " 1234abcde adsfasdf ";
		Token token = lexicalParser.isNumber(cql, false);
		Assert.assertNull(token);

		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isNumber(cql, false);
		Assert.assertNull(token);

		cql = "1234Éabcde adsfasdf ";
		token = lexicalParser.isNumber(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("1234", token.getContent());
		Assert.assertEquals("Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.NUMBER, token.getType());

	}

	// <OR> ::= u(OR)
	@Test
	public void or() throws LexicalParserException {
		String cql = "algo,test";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = "Or test";
		token = lexicalParser.isOr(cql, false);
		token = lexicalParser.isOr(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("OR", token.getContent());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals(" test", token.getPosContent());

	}

	/**
	 * *<RESERVED WORD> ::= ADD | ALL | ALLOW | ALTER | AND | ANY | APPLY | ASC
	 * | ASCII | AUTHORIZE | BATCH | BEGIN | BIGINT | BLOB | BOOLEAN | BY |
	 * CLUSTERING | COLUMNFAMILY | COMPACT | COUNT | COUNTER | CONSISTENCY |
	 * CREATE | DECIMAL | DELETE | DESC | DOUBLE | DROP | EACH_QUORUM |
	 * FILTERING | FLOAT | FROM | GRANT | IN | INDEX | INET | INSERT | INT |
	 * INTO | KEY | KEYSPACE | KEYSPACES | LEVEL | LIMIT | LIST | LOCAL_ONE |
	 * LOCAL_QUORUM | MAP | MODIFY | OF | ON | ONE | ORDER | PASSWORD |
	 * PERMISSION | PERMISSIONS | PRIMARY | QUORUM | RENAME | REVOKE | RECURSIVE
	 * | SUPERUSER | SCHEMA | SELECT | SET | STORAGE | SUPERUSER | TABLE | TEXT
	 * | TIMESTAMP | TIMEUUID | TO | TOKEN | THREE | TRUNCATE | TTL | TWO | TYPE
	 * | UNLOGGED | UPDATE | USE | USER | USERS | USING | UUID | VALUES |
	 * VARCHAR | VARINT | WITH | WRITETIME
	 * 
	 * */
	@Test
	public void reservedWord() throws LexicalParserException {
		String cql = " SELECT ";
		Token token = lexicalParser.isReservedWord(cql, false);
		Assert.assertNull(token);

		cql = "SELECT TEST ";
		token = lexicalParser.isReservedWord(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("SELECT", token.getContent());
		Assert.assertEquals(" TEST ", token.getPosContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		cql = "SeLeCt TEST ";
		token = lexicalParser.isReservedWord(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("SeLeCt", token.getContent());
		Assert.assertEquals(" TEST ", token.getPosContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());
	}

	@Test
	public void reservedWords() throws LexicalParserException {
		String cql = "abcd";
		Token token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = " SELECT INSERT  UPDATE   DELETE ";
		token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = "SELECT INSERT  UPDATE   DELETE";
		token = lexicalParser.isReservedWords(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("SELECT INSERT  UPDATE   DELETE",
				token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORDS, token.getType());
		Assert.assertEquals(7, token.getSubTokens().size());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());

		token = token.getSubTokens().get(0);
		Assert.assertNotNull(token);
		Assert.assertNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("SELECT", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals(" ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("INSERT", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("  ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("UPDATE", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("   ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals("DELETE", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

	}

	@Test
	public void reservedWordsIgnoreDoubleSpaces() throws LexicalParserException {
		String cql = "abcd";
		Token token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = " SELECT  INSERT UPDATE    DELETE ";
		token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = "SELECT  INSERT   UPDATE    DELETE";
		token = lexicalParser.isReservedWords(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("SELECT  INSERT   UPDATE    DELETE",
				token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORDS, token.getType());
		Assert.assertEquals(7, token.getSubTokens().size());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());

		token = token.getSubTokens().get(0);
		Assert.assertNotNull(token);
		Assert.assertNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("SELECT", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("  ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("INSERT", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("   ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("UPDATE", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNotNull(token.getAfter());
		Assert.assertEquals("    ", token.getContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		token = token.getAfter();
		Assert.assertNotNull(token);
		Assert.assertNotNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals("DELETE", token.getContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());

	}

	// <SELECTOR BLOCK> ::= [<SPACES>] <SELECTOR ITEM> [<SPACES>] [,<SELECTOR
	// BLOCK>]
	@Test
	public void selectorBlock() throws LexicalParserException {
		String cql = "SELECT   test   ";
		Assert.assertNull(lexicalParser.isSelectorBlock(cql, false));

		cql = " teste  ";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, false));
		cql = " teste  ";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, true));
		cql = " teste  , teste2";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, false));
		cql = "teste3.teste  , teste2INSERT";
		Token token = lexicalParser.isSelectorBlock(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals("teste3.teste  , teste2INSERT", token.getContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(2).getType());

	}

	/**
	 * <SELECTOR ITEM> ::= ^<RESERVED WORD> <ITEM NAME> [<ACESSOR> <ITEM NAME>]
	 * | <INJECT> | <LITERAL> | <FUNCTION>
	 */
	@Test
	public void selectorItem() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = "'. \n adf '' ; * ?  teste' 123";
		Token token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());
		cql = "'. \n adf '' ; * ?  teste' 123'";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ITEMNAME, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals("1234abcde", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());

		cql = "? afd adsf adsfasdf ";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("?", token.getContent());
		Assert.assertEquals(" afd adsf adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(TokenType.INJECT, token.getSubTokens().get(0)
				.getType());

		cql = "accounts.id = 3";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id", token.getContent());
		Assert.assertEquals(" = 3", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(TokenType.ITEMNAME, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.ACESSOR, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.ITEMNAME, token.getSubTokens().get(2)
				.getType());
	}

	// <SINGLE QUOTED> ::= '
	@Test
	public void singleQuoted() throws LexicalParserException {
		String cql = "'";
		Token start = lexicalParser.isSingleQuoted(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isSingleQuoted(cql, false);
		Assert.assertNotNull(start);
		cql = "' test ";
		start = lexicalParser.isSingleQuoted(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isSingleQuoted(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("'", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " 'test ";
		start = lexicalParser.isEndParameters(cql, false);
		Assert.assertNull(start);

	}

	@Test
	public void spaces() throws LexicalParserException {
		String cql = " x";
		Token token = lexicalParser.isSpaces(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("x", token.getPosContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

		cql = "  x  ";
		token = lexicalParser.isSpaces(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("  ", token.getContent());
		Assert.assertEquals("x  ", token.getPosContent());
		Assert.assertEquals(TokenType.SPACES, token.getType());

	}

	// <START_PARAMETERS>::=(
	@Test
	public void startParameters() throws LexicalParserException {
		String cql = "(";
		Token start = lexicalParser.isStartParameters(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartParameters(cql, false);
		Assert.assertNotNull(start);
		cql = "( test ";
		start = lexicalParser.isStartParameters(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartParameters(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("(", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " (test ";
		start = lexicalParser.isStartParameters(cql, false);
		Assert.assertNull(start);

	}

	/**
	 * <INPUT CHARACTER> :: = (^<SINGLE QUOTED>^<DOUBLE QUOTED>%s)[<INPUT
	 * CHARACTER>]
	 */
	@Test
	public void string() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isString(cql, false));

		cql = "'. teste";
		Assert.assertNull("Non string [" + cql + "]",
				lexicalParser.isString(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isString(cql, false));

		cql = "'. \n adf '' ; * ?  test' 123";
		Token token = lexicalParser.isString(cql, true);
		token = lexicalParser.isString(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  test'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.STRING, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SINGLE_QUOTED, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE, token
				.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.SINGLE_QUOTED, token.getSubTokens()
				.get(2).getType());

		cql = "'. \n adf ''' 123";

		token = lexicalParser.isString(cql, true);
		Assert.assertEquals("'. \n adf '''", token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '''", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.STRING, token.getType());

		Assert.assertEquals(3, token.getSubTokens().size());
		// vai dar erro, falta ' e '
		Assert.assertEquals(TokenType.SINGLE_QUOTED, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE, token
				.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.SINGLE_QUOTED, token.getSubTokens()
				.get(2).getType());

		cql = "\". \n 'adf \"\" ;' * ?  test\" 123'";
		token = lexicalParser.isString(cql, true);
		token = lexicalParser.isString(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("\". \n 'adf \"\" ;' * ?  test\"",
				token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.STRING, token.getType());

		Assert.assertEquals(3, token.getSubTokens().size());
		// vai dar erro, falta ' e '
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE, token
				.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, token.getSubTokens()
				.get(2).getType());

	}

	// <SYMBOL> ::= = | + | - | / | * | ( | ) | { | } | , [ | ]
	@Test
	public void symbol() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isSymbol(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isSymbol(cql, false));

		cql = " . teste'";
		Assert.assertNull(lexicalParser.isSymbol(cql, false));

		cql = "as'. \n adf '' ; * , ? [ ]  teste' 123";
		Assert.assertNull(lexicalParser.isSymbol(cql, false));

		cql = "";
		Assert.assertNull(lexicalParser.isSymbol(cql, false));

		cql = "*as'. \n adf '' ; * , ? [ ]  teste' 123";
		Token token = lexicalParser.isSymbol(cql, true);
		token = lexicalParser.isSymbol(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("*", token.getContent());
		Assert.assertEquals("as'. \n adf '' ; * , ? [ ]  teste' 123",
				token.getPosContent());
		Assert.assertEquals(TokenType.SYMBOL, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "-as'. \n adf '' ; * , ? [ ]  teste' 123";
		token = lexicalParser.isSymbol(cql, true);
		token = lexicalParser.isSymbol(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("-", token.getContent());
		Assert.assertEquals("as'. \n adf '' ; * , ? [ ]  teste' 123",
				token.getPosContent());
		Assert.assertEquals(TokenType.SYMBOL, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "+as'. \n adf '' ; * , ? [ ]  teste' 123";
		token = lexicalParser.isSymbol(cql, true);
		token = lexicalParser.isSymbol(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("+", token.getContent());
		Assert.assertEquals("as'. \n adf '' ; * , ? [ ]  teste' 123",
				token.getPosContent());
		Assert.assertEquals(TokenType.SYMBOL, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "[as'. \n adf '' ; * , ? [ ]  teste' 123";
		token = lexicalParser.isSymbol(cql, true);
		token = lexicalParser.isSymbol(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("[", token.getContent());
		Assert.assertEquals("as'. \n adf '' ; * , ? [ ]  teste' 123",
				token.getPosContent());
		Assert.assertEquals(TokenType.SYMBOL, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

	}

	// <WHERE> ::= u(WHERE)
	@Test
	public void where() throws LexicalParserException {
		String cql = "where teste";
		Token token = lexicalParser.isWhere(cql, true);
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("WHERE", token.getContent());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "whEre teste";
		token = lexicalParser.isWhere(cql, true);
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("WHERE", token.getContent().toUpperCase());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "WHERE teste";
		token = lexicalParser.isWhere(cql, true);
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("WHERE", token.getContent());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "WHERE";
		token = lexicalParser.isWhere(cql, true);
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("WHERE", token.getContent().toUpperCase());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = " WHERE ";
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNull(token);

		cql = " WHERE";
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNull(token);

		cql = " asdf ";
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNull(token);

		try {

			cql = " teste";
			token = lexicalParser.isWhere(cql, true);
			Assert.fail("Incorrect content to where");

		} catch (Exception e) {
			Assert.assertNotNull(e);
		}
	}

}
