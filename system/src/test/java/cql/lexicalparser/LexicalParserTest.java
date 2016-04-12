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
		Assert.assertEquals("AnD", token.getContent());
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

	// <INSERT> ::= u(INSERT)
	@Test
	public void insert() throws LexicalParserException {
		String cql = "select ";
		Token token = lexicalParser.isInsert(cql, false);
		Assert.assertNull(token);

		cql = "insert (x,y,z) values (z,n,q)";
		token = lexicalParser.isInsert(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("insert", token.getContent());

	}

	// <FIELD NAME> ::= ^<RESERVED WORD> (<ITEM NAME CASE SENSITIVE> | <ITEM
	// NAME CASE INSENSITIVE>)
	@Test
	public void fieldName() throws LexicalParserException {
		String cql = "select ";
		Token token = lexicalParser.isFieldName(cql, false);
		Assert.assertNull(token);

		cql = "as fieldA";
		token = lexicalParser.isFieldName(cql, false);
		Assert.assertNull(token);

		cql = "fieldA as fieldB";
		token = lexicalParser.isFieldName(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("fieldA", token.getContent());
		Assert.assertEquals(" as fieldB", token.getPosContent());
		Assert.assertEquals(TokenType.FIELDNAME, token.getType());

	}

	// <AS> :: = AS
	@Test
	public void as() throws LexicalParserException {
		String cql = "select ";
		Token token = lexicalParser.isAs(cql, false);
		Assert.assertNull(token);

		cql = "as fieldA";
		token = lexicalParser.isAs(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("as", token.getContent());
		Assert.assertEquals(" fieldA", token.getPosContent());
		Assert.assertEquals(TokenType.AS, token.getType());

	}

	// <ALIAS> ::= [<AS> <SPACES>] <FIELD NAME>
	@Test
	public void alias() throws LexicalParserException {
		String cql = "+select ";
		Token token = lexicalParser.isAlias(cql, false);
		Assert.assertNull(token);

		cql = " aliasField";
		token = lexicalParser.isAlias(cql, false);
		Assert.assertNull(token);

		cql = "aliasField that a test";
		token = lexicalParser.isAlias(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField", token.getContent());
		Assert.assertEquals(" that a test", token.getPosContent());

		Assert.assertEquals(TokenType.ALIAS, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());

		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELDNAME, sub.getType());

		cql = "asaliasField that a test";
		token = lexicalParser.isAlias(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("asaliasField", token.getContent());
		Assert.assertEquals(" that a test", token.getPosContent());

		Assert.assertEquals(TokenType.ALIAS, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELDNAME, sub.getType());

		cql = "as aliasField that a test";
		token = lexicalParser.isAlias(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("as aliasField", token.getContent());
		Assert.assertEquals(" that a test", token.getPosContent());

		Assert.assertEquals(TokenType.ALIAS, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.AS, sub.getType());
		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());
		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.FIELDNAME, sub.getType());

	}

	// <INTO> ::= u(INTO)
	@Test
	public void into() throws LexicalParserException {
		String cql = "select ";
		Token token = lexicalParser.isInto(cql, false);
		Assert.assertNull(token);

		cql = "into (x,y,z) values (z,n,q)";
		token = lexicalParser.isInto(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("into", token.getContent());

		Assert.assertEquals(TokenType.INTO, token.getType());

	}

	// <VALUES> ::= u(VALUES)
	@Test
	public void values() throws LexicalParserException {
		String cql = "select ";
		Token token = lexicalParser.isValues(cql, false);
		Assert.assertNull(token);

		cql = "values (z,n,q)";
		token = lexicalParser.isValues(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("values", token.getContent());

		Assert.assertEquals(TokenType.VALUES, token.getType());
	}


	// <OTHER COMMAND> ::= ^<WHERE> <RESERVED WORDS> [<SPACES>] (<SELECTOR BLOCK> | (  [<SYMBOL>] [<SPACES>]  [<LITERAL>] )[<SPACES>] ) [[<SPACES>]<OTHER COMMAND>]
	@Test
	public void otherCommand() throws LexicalParserException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "'test' ";
		Token token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNull(token);
		
		//where starting
		cql = "where a,v,c from tablea";
		token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select a,v,c";
		token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select a,v,c from tablea";
		token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());
	}

	// <INSERT COMMAND> ::= <INSERT> <SPACES> [<INTO> <SPACES>] <SELECTOR ITEM>
	// [<SPACES>] <START_PARAMETERS> [<SPACES>] <SELECTOR BLOCK> [<SPACES>]
	// <END_PARAMETERS> [<SPACES>] <VALUES> [<SPACES>] <START_PARAMETERS>
	// [<SPACES>] [<SELECTOR BLOCK>] [<SPACES>] <END_PARAMETERS>

	@Test
	public void insertCommand() throws LexicalParserException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "select ";
		Token token = lexicalParser.isInsertCommand(cql, false);
		Assert.assertNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "insert users (x,y,z) values (z,n,q)";
		token = lexicalParser.isInsertCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },{?:?,?:?})";
		token = lexicalParser.isInsertCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "insert tab (x,y,z) values (z,n,q)";
		token = lexicalParser.isInsertCommand(cql, true);
		Assert.assertNotNull(token);

		cql = "insert (x,y,z) values (z,n,q)";
		token = lexicalParser.isInsertCommand(cql, false);
		Assert.assertNull(token);

		cql = "INSERTINTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },{?:?,?:?})";
		token = lexicalParser.isInsertCommand(cql, false);
		Assert.assertNull(token);

	}

	/**
	 * TODO:Faltou arrumar :
	 * 
	
	a-) CQL b-) <OTHER COMMAND>
	 ***/ 

	// <COMMAND> ::= <INSERT COMMAND> | <OTHER COMMAND>
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

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "insert tab1 (x,y,z) values (z,n,q)";
		token = lexicalParser.isCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "select insert";
		Assert.assertNotNull(lexicalParser.isCommand(cql, false));

		// only reserved words
		cql = "select insertT  ";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select insertT", token.getContent());
		Assert.assertEquals("  ", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());

		// add selector item
		cql = "select insert  accounts.id";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select ", token.getContent());
		Assert.assertEquals("insert  accounts.id", token.getPosContent());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());

		// add symbol
		cql = "select insert  accounts.id;select insert  accounts.name";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select ", token.getContent());
		Assert.assertEquals("insert  accounts.id;select insert  accounts.name",
				token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());

		cql = "select insert accounts.id;select insert  accounts.name";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select ", token.getContent());
		Assert.assertEquals("insert accounts.id;select insert  accounts.name",
				token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.OTHER_COMMAND, token.getType());

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
		cql = "WHERE    X=3 AND X=5 aliasa   test";
		Token token = lexicalParser.isCondition(cql, false);
		token = lexicalParser.isCondition(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.getType(), TokenType.CONDITION);
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals("   test", token.getPosContent());
		Assert.assertEquals("WHERE    X=3 AND X=5 aliasa", token.getContent());
		Assert.assertEquals("WHERE", token.getSubTokens().get(0).getContent());

		Assert.assertEquals("    X=3 AND X=5 aliasa",
				token.getSubTokens().get(1).getContent());
		Assert.assertEquals("    ", token.getSubTokens().get(1).getSubTokens()
				.get(0).getContent());

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

		cql = "accounts.id =  3 aliasA    teste";
		Token token = lexicalParser.isConditionItem(cql, true);
		token = lexicalParser.isConditionItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id =  3 aliasA", token.getContent());
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

		Assert.assertEquals(5, token.getSubTokens().size());

		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.SYMBOL, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(3)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getSubTokens()
				.get(4).getType());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(4)
				.getSubTokens().get(0).getType());

	}

	/**
	 * // <CONDITIONS> ::= <SPACES><CONDITION-ITEM>[<SPACES> <JOIN CONDITION>
	 * <CONDITIONS>]
	 */
	@Test
	public void conditions() throws LexicalParserException {
		String cql = "X=3 AND  X=5";
		Assert.assertNull(lexicalParser.isConditions(cql, false));
		cql = " X=3 aliasa test";
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
		cql = " X=3 AND Y=3 aliasb test";
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
		Assert.assertEquals(token.getSubTokens().toString(),
				TokenType.CONDITIONS, token.getSubTokens().get(4).getType());
		Assert.assertEquals(" test", token.getPosContent());
		cql = " user_token = ?";
		token = lexicalParser.isConditions(cql, false);
		token = lexicalParser.isConditions(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.CONDITION_ITEM,
				token.getSubTokens().get(1).getType());
	}

	// <CQL> ::= [<SPACES>] <COMMAND> [ <SPACES> <CONDITION> ] [ <SPACES>]
	// [<COMA> [ <SPACES>]]
	@Test
	public void cql() throws LexicalParserException {
		String cql = "select count(*) as valid from accounts where user_token = ?";
		Token cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },[?,?,?],{?:?,?:?,?:?})";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());
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

	// <END ARRAY>::=)
	@Test
	public void endArray() throws LexicalParserException {
		String cql = "]";
		Token start = lexicalParser.isEndArray(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndArray(cql, false);
		Assert.assertNotNull(start);
		cql = "] test ";
		start = lexicalParser.isEndArray(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndArray(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("]", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " ]test ";
		start = lexicalParser.isEndArray(cql, false);
		Assert.assertNull(start);

	}

	// <END MAP>::=}
	@Test
	public void endMap() throws LexicalParserException {
		String cql = "}";
		Token start = lexicalParser.isEndMap(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndMap(cql, false);
		Assert.assertNotNull(start);
		cql = "} test ";
		start = lexicalParser.isEndMap(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isEndMap(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("}", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
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
	// <HEXA> ::= (a-f)|(A-F)|<DIGIT>|-[<HEXA>]
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
		String cql = ". ";
		Token token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(". ", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

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

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste'teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste'teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE INSENSITIVE>
	// | <ASTERISK>
	@Test
	public void itemName() throws LexicalParserException {
		String cql = " \"1234abcde\" adsfasdf ";
		Token token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde²\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde$\" adsfasdf ";
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNull(token);

		cql = "\"é1234abcde\" adsfasdf ";
		token = lexicalParser.isItemName(cql, true);
		Assert.assertEquals(TokenType.ITEMNAME, token.getType());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_SENSITIVE, token
				.getSubTokens().get(0).getType());

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

		cql = "abcde1234 adsfasdf ";
		token = lexicalParser.isItemName(cql, true);
		token = lexicalParser.isItemName(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ITEMNAME, token.getType());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_INSENSITIVE, token
				.getSubTokens().get(0).getType());

	}

	// <ITEM NAME CASE INSENSITIVE> ::= ^<NUMBER> <CHARS>
	@Test
	public void itemNameCaseInsensitive() throws LexicalParserException {
		String cql = "1234abcde adsfasdf ";
		Token token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertNull(token);

		cql = "1234abcde² adsfasdf ";
		token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertNull(token);

		cql = "1234abcde$ adsfasdf ";
		token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertNull(token);

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertNull(token);

		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertEquals("é1234abcde", token.getContent());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_INSENSITIVE,
				token.getType());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getType());

		cql = "abcde1234 adsfasdf ";
		token = lexicalParser.isItemNameCaseInsensitive(cql, true);
		token = lexicalParser.isItemNameCaseInsensitive(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_INSENSITIVE,
				token.getType());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getType());
		Assert.assertNull(token.getSubTokens().get(0).getAfter());
		Assert.assertNull(token.getSubTokens().get(0).getBefore());

	}

	// <ITEM NAME CASE SENSITIVE> ::= "<CHARS>"
	@Test
	public void itemNameCaseSensitive() throws LexicalParserException {
		String cql = " \"1234abcde\" adsfasdf ";
		Token token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde²\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde$\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);

		cql = "\"é1234abcde\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertEquals("\"é1234abcde\"", token.getContent());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_SENSITIVE, token.getType());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getType());

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
	 * <LITERAL> ::= (<NUMBER> | <STRING> | <HEXA>)^<CHARS>
	 */
	@Test
	public void literal() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isLiteral(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isLiteral(cql, false));

		cql = "3abdcd";
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
		Assert.assertNull(token);

	}

	// <NUMBER> ::= <DIGIT>[<NUMBER>]
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

		Assert.assertEquals(TokenType.NUMBER, token.getType());

	}

	// <DIGIT> ::= (0-9)
	@Test
	public void digit() throws LexicalParserException {
		String cql = "é1234Éabcde adsfasdf ";
		Token token = lexicalParser.isDigit(cql, false);
		Assert.assertNull(token);

		cql = " 1234Éabcde adsfasdf ";
		token = lexicalParser.isDigit(cql, false);
		Assert.assertNull(token);

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isDigit(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("1", token.getContent());
		Assert.assertEquals("234abcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.DIGIT, token.getType());

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
		Assert.assertEquals(TokenType.OR, token.getType());
		Assert.assertEquals("Or", token.getContent());
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

	// <SELECTOR BLOCK> ::= ^<WHERE> <SELECTOR ITEM> [[<SPACES>] <COMMA>
	// [<SPACES>] <SELECTOR BLOCK>]
	@Test
	public void selectorBlock() throws LexicalParserException {
		String cql = "SELECT   test   ";
		Assert.assertNull(lexicalParser.isSelectorBlock(cql, false));

		cql = "teste";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, false));
		cql = "teste";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, true));
		cql = "teste  , teste2";
		Assert.assertNotNull(lexicalParser.isSelectorBlock(cql, false));

		cql = "temp where user_token = ?";
		Token token = lexicalParser.isSelectorBlock(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(" where user_token = ?", token.getPosContent());

		cql = "3,5,6] , 6";
		token = lexicalParser.isSelectorBlock(cql, true);
		token = lexicalParser.isSelectorBlock(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("3,5,6", token.getContent());
		Assert.assertEquals("] , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());

		cql = "x,[a,b,c],{a:'a',b:'b'} where user_token = ?";
		token = lexicalParser.isSelectorBlock(cql, false);
		token = lexicalParser.isSelectorBlock(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(" where user_token = ?", token.getPosContent());

		cql = "{?:?,?:?})";
		token = lexicalParser.isSelectorBlock(cql, false);
		token = lexicalParser.isSelectorBlock(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("{?:?,?:?}", token.getContent());
		Assert.assertEquals(")", token.getPosContent());
		
		cql = "?,?,?,[?,?,?],{?:?,?:? ,?:? },{?:?,?:?})";
		token = lexicalParser.isSelectorBlock(cql, false);
		token = lexicalParser.isSelectorBlock(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("?,?,?,[?,?,?],{?:?,?:? ,?:? },{?:?,?:?}",
				token.getContent());
		Assert.assertEquals(")", token.getPosContent());

		cql = "teste3.teste  , teste2INSERT";
		token = lexicalParser.isSelectorBlock(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(5, token.getSubTokens().size());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals("teste3.teste  , teste2INSERT", token.getContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.COMMA, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(3)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(4).getType());
		
		

	}

	/**
	 * <SELECTOR ITEM> ::= ^<RESERVED WORD> (<FUNCTION> | <LITERAL> |<ITEM NAME>
	 * [<ACESSOR> <FIELD NAME>] | <ARRAY> | <MAP>)[<SPACES><ALIAS>]
	 */
	@Test
	public void selectorItem() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isSelectorItem(cql, false));

		cql = "count(abc.def)";
		Token token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FUNCTION, token.getType());

		cql = "'. \n adf '' ; * ?  teste' 123";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "3";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
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

		cql = "abcde1234 adsfasdf adsfasdf";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ITEMNAME, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals("abcde1234 adsfasdf", token.getContent());
		Assert.assertEquals(" adsfasdf", token.getPosContent());

		cql = "? afd adsf adsfasdf ";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("? afd", token.getContent());
		Assert.assertEquals(" adsf adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
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
		Assert.assertEquals(TokenType.FIELDNAME, token.getSubTokens().get(2)
				.getType());

		cql = "[3,5,6] , 6";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5,6]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.START_ARRAY, token.getType());

		cql = "{idade:3,min:5} , 6";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{idade:3,min:5}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(TokenType.START_MAP, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.END_MAP, token.getSubTokens().get(2)
				.getType());

		cql = "tableA.fieldA as fieldB, test";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("tableA.fieldA as fieldB", token.getContent());
		Assert.assertEquals(", test", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ITEMNAME, sub.getType());
		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.ACESSOR, sub.getType());
		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.FIELDNAME, sub.getType());
		sub = token.getSubTokens().get(3);
		Assert.assertEquals(TokenType.SPACES, sub.getType());
		sub = token.getSubTokens().get(4);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

	}

	/**
	 * <MAP> ::= <START MAP>[<SPACES>][<PROPERTIES>][<SPACES>]<END MAP>
	 */
	@Test
	public void map() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isMap(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isMap(cql, false));

		cql = "{} , 6";
		Token token = lexicalParser.isMap(cql, true);
		token = lexicalParser.isMap(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(TokenType.START_MAP, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_MAP, token.getSubTokens().get(1)
				.getType());

		cql = "{name:'Test',age:35} , 6";
		token = lexicalParser.isMap(cql, true);
		token = lexicalParser.isMap(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{name:'Test',age:35}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(TokenType.START_MAP, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.END_MAP, token.getSubTokens().get(2)
				.getType());

	}

	/**
	 * <PROPERTIES> ::= <PROPERTY> [[<SPACES>]<COMMA>[<SPACES>] <PROPERTIES>]
	 */
	@Test
	public void properties() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isProperties(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isProperties(cql, false));

		cql = "{} , 6";
		Assert.assertNull(lexicalParser.isProperties(cql, false));

		cql = "{name:'Test',age:35} , 6";
		Assert.assertNull(lexicalParser.isProperties(cql, false));

		cql = "name:'Test',age:35,color:'fff' } , 6";
		Token token = lexicalParser.isProperties(cql, true);
		token = lexicalParser.isProperties(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("name:'Test',age:35,color:'fff'",
				token.getContent());
		Assert.assertEquals(" } , 6", token.getPosContent());
		Assert.assertEquals(TokenType.PROPERTIES, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.PROPERTY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.COMMA, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(2)
				.getType());

		cql = "a:'a',b:'b'} where user_token = ?";
		token = lexicalParser.isProperties(cql, true);
		token = lexicalParser.isProperties(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("a:'a',b:'b'", token.getContent());
		Assert.assertEquals("} where user_token = ?", token.getPosContent());
		Assert.assertEquals(TokenType.PROPERTIES, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.PROPERTY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.COMMA, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(2)
				.getType());

	}

	/**
	 * <PROPERTY> ::= <KEY>[<SPACES>]<DOUBLE DOT>[<SPACES>]<LITERAL>
	 */
	@Test
	public void property() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isProperty(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isProperty(cql, false));

		cql = "{} , 6";
		Assert.assertNull(lexicalParser.isProperty(cql, false));

		cql = "{name : 'Test' , age : 35 } , 6";
		Assert.assertNull(lexicalParser.isProperty(cql, false));

		cql = "name : 'Test' , age : 35 ";

		Token token = lexicalParser.isProperty(cql, true);
		token = lexicalParser.isProperty(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("name : 'Test'", token.getContent());
		Assert.assertEquals(" , age : 35 ", token.getPosContent());
		Assert.assertEquals(TokenType.PROPERTY, token.getType());
		Assert.assertEquals(5, token.getSubTokens().size());
		Assert.assertEquals(TokenType.KEY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.DOUBLE_DOT, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(3)
				.getType());

		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(4)
				.getType());

		cql = "name:'Test',age:35";

		token = lexicalParser.isProperty(cql, true);
		token = lexicalParser.isProperty(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("name:'Test'", token.getContent());
		Assert.assertEquals(",age:35", token.getPosContent());
		Assert.assertEquals(TokenType.PROPERTY, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.KEY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.DOUBLE_DOT, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(2)
				.getType());

	}

	/**
	 * <KEY> ::= <CHARS>|<LITERAL>
	 */
	@Test
	public void key() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isKey(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isKey(cql, false));

		cql = "{} , 6";
		Assert.assertNull(lexicalParser.isKey(cql, false));

		cql = "name : 'Test' , age : 35 } , 6";
		Token token = lexicalParser.isKey(cql, false);
		token = lexicalParser.isKey(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("name", token.getContent());
		Assert.assertEquals(TokenType.KEY, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getType());

		cql = "'name' : 'Test' , age : 35 } , 6";
		token = lexicalParser.isKey(cql, false);
		token = lexicalParser.isKey(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("'name'", token.getContent());
		Assert.assertEquals(TokenType.KEY, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

	}

	/**
	 * <ARRAY> ::= <START ARRAY>[<SPACES>][<SELECTOR BLOCK>][<SPACES>]<END
	 * ARRAY>
	 */
	@Test
	public void array() throws LexicalParserException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isArray(cql, false));

		cql = "[3,5] , 6";
		Token token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		Assert.assertEquals(TokenType.START_ARRAY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.END_ARRAY, token.getSubTokens().get(2)
				.getType());

		cql = "[] , 6";
		token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		Assert.assertEquals(TokenType.START_ARRAY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_ARRAY, token.getSubTokens().get(1)
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

	// <CHARS> ::= ^<EMPTY>[<CHARS>](a-Z0-9_)
	@Test
	public void chars() throws LexicalParserException {
		String cql = "afasdf";
		Token token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "afasdf342";
		token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "afasd_f342";
		token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "234afasdf342";
		token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "234afasdf";
		token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "éasdfasdfçã";
		token = lexicalParser.isChars(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = " teste ";
		token = lexicalParser.isChars(cql, false);
		Assert.assertNull(token);

		cql = " ";
		token = lexicalParser.isChars(cql, false);
		Assert.assertNull(token);

		cql = "";
		token = lexicalParser.isChars(cql, false);
		Assert.assertNull(token);

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

	// <START ARRAY>::=(
	@Test
	public void startArray() throws LexicalParserException {
		String cql = "[";
		Token start = lexicalParser.isStartArray(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartArray(cql, false);
		Assert.assertNotNull(start);
		cql = "[ test ";
		start = lexicalParser.isStartArray(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartArray(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("[", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " [test ";
		start = lexicalParser.isStartArray(cql, false);
		Assert.assertNull(start);

	}

	// <START MAP>::={
	@Test
	public void startMap() throws LexicalParserException {
		String cql = "{";
		Token start = lexicalParser.isStartMap(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartMap(cql, false);
		Assert.assertNotNull(start);
		cql = "{ test ";
		start = lexicalParser.isStartMap(cql, true);
		Assert.assertNotNull(start);
		start = lexicalParser.isStartMap(cql, false);
		Assert.assertNotNull(start);
		Assert.assertEquals("{", start.getContent());
		Assert.assertEquals(" test ", start.getPosContent());
		Assert.assertNull(start.getBefore());
		Assert.assertNull(start.getAfter());
		Assert.assertEquals(0, start.getSubTokens().size());
		cql = " {test ";
		start = lexicalParser.isStartMap(cql, false);
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
		Assert.assertEquals("where", token.getContent());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(0, token.getSubTokens().size());

		cql = "whEre teste";
		token = lexicalParser.isWhere(cql, true);
		token = lexicalParser.isWhere(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("whEre", token.getContent());
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
		Assert.assertEquals("WHERE", token.getContent());
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
