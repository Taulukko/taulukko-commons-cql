package cql.lexicalparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cql.Token;
import cql.TokenType;
import cql.lexicalparser.exceptions.CQLException;

public class LexicalParserTest {

	private LexicalParser lexicalParser = null;

	@Before
	public void cleanup() throws CQLException {
		lexicalParser = new LexicalParser();
	}

	// <ACESSOR> ::= .
	@Test
	public void acessor() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals(".", token.getContent());
		Assert.assertEquals(" teste", token.getPosContent());
		Assert.assertEquals(TokenType.ACESSOR, token.getType());
		Assert.assertEquals(0, token.getSubTokens().size());
	}

	// <AND> ::= u(AND)
	@Test
	public void and() throws CQLException {
		String cql = "algo,test";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = "AnD test";
		token = lexicalParser.isAnd(cql, false);
		token = lexicalParser.isAnd(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("AnD", token.getContent());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals(" test", token.getPosContent());

	}

	// <COMA> ::= ,
	@Test
	public void comma() throws CQLException {
		String cql = "algo,teste";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = ",teste";
		token = lexicalParser.isComma(cql, false);
		token = lexicalParser.isComma(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals(",", token.getContent());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals("teste", token.getPosContent());

	}

	// <INSERT> ::= u(INSERT)
	@Test
	public void insert() throws CQLException {
		String cql = "select ";
		Token token = lexicalParser.isInsert(cql, false);
		Assert.assertNull(token);

		cql = "insert (x,y,z) values (z,n,q)";
		token = lexicalParser.isInsert(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("insert", token.getContent());

	}

	// <FIELD NAME> ::= [<TABLE NAME>[<SPACES>]<ACESSOR>[<SPACES>]] (<ENTITY
	// NAME>|<ASTERISK>)
	@Test
	public void fieldName() throws CQLException {
		String cql = "select ";
		Token token = lexicalParser.isFieldName(cql, false);
		Assert.assertNull(token);

		cql = "as fieldA";
		token = lexicalParser.isFieldName(cql, false);
		Assert.assertNull(token);

		cql = "fieldA as fieldB";
		token = lexicalParser.isFieldName(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("fieldA", token.getContent());
		Assert.assertEquals(" as fieldB", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_NAME, token.getType());

	}

	// <ANY> ::= ?
	@Test
	public void any() throws CQLException {
		String cql = "";
		Token token = lexicalParser.isAny(cql, false);
		Assert.assertNull(token);

		cql = "*as fieldA";
		token = lexicalParser.isAny(cql, true);
		Assert.assertNotNull(token);

		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertEquals("*", token.getContent());
		Assert.assertEquals("as fieldA", token.getPosContent());
		Assert.assertEquals(TokenType.ANY, token.getType());

	}

	// <AS> :: = AS
	@Test
	public void as() throws CQLException {
		String cql = "select ";
		Token token = lexicalParser.isAs(cql, false);
		Assert.assertNull(token);

		cql = "as fieldA";
		token = lexicalParser.isAs(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("as", token.getContent());
		Assert.assertEquals(" fieldA", token.getPosContent());
		Assert.assertEquals(TokenType.AS, token.getType());

	}

	// <TABLE NAME> ::= <ENTITY NAME>
	// <ALIAS>::=[<AS> <SPACES>] <ENTITY NAME>
	@Test
	public void tableName() throws CQLException {
		entityName();

		String cql = "clients insert";
		Token token = lexicalParser.isTableName(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("clients", token.getContent());
		Assert.assertEquals(" insert", token.getPosContent());
		Assert.assertEquals(TokenType.TABLE_NAME, token.getType());

		Assert.assertEquals(token.rebuild(), token.getContent());

	}

	// <FIELD NAME DECLARATION> ::= <FIELD VALUE> [<SPACES>] <ALIAS>]

	@Test
	public void fieldNameDeclaration() throws CQLException {
		String cql = "+select ";
		Token token = lexicalParser.isFieldNameDeclaration(cql, false);
		Assert.assertNull(token);

		cql = " aliasField";
		token = lexicalParser.isFieldNameDeclaration(cql, false);
		Assert.assertNull(token);

		cql = "aliasField that a test";
		token = lexicalParser.isFieldNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField that", token.getContent());
		Assert.assertEquals(" a test", token.getPosContent());

		Assert.assertEquals(TokenType.FIELD_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

		cql = "aliasField as that a test";
		token = lexicalParser.isFieldNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField as that", token.getContent());
		Assert.assertEquals(" a test", token.getPosContent());

		Assert.assertEquals(TokenType.FIELD_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

		cql = "count(*) as total from ";
		token = lexicalParser.isFieldNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("count(*) as total", token.getContent());
		Assert.assertEquals(" from ", token.getPosContent());

		Assert.assertEquals(TokenType.FIELD_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

		cql = "[3,5,7] as firstPrimes from ";
		token = lexicalParser.isFieldNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5,7] as firstPrimes", token.getContent());
		Assert.assertEquals(" from ", token.getPosContent());

		Assert.assertEquals(TokenType.FIELD_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

		cql = "{age:35,name:'Jhon'} as info from ";
		token = lexicalParser.isFieldNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("{age:35,name:'Jhon'} as info", token.getContent());
		Assert.assertEquals(" from ", token.getPosContent());

		Assert.assertEquals(TokenType.FIELD_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

	}

	// <TABLE NAME DECLARATION>::= <TABLE NAME> [<SPACES>] <ALIAS>]
	@Test
	public void tableNameDeclaration() throws CQLException {
		String cql = "+select ";
		Token token = lexicalParser.isTableNameDeclaration(cql, false);
		Assert.assertNull(token);

		cql = " aliasField";
		token = lexicalParser.isTableNameDeclaration(cql, false);
		Assert.assertNull(token);

		cql = "aliasField that a test";
		token = lexicalParser.isTableNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField that", token.getContent());
		Assert.assertEquals(" a test", token.getPosContent());

		Assert.assertEquals(TokenType.TABLE_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.TABLE_NAME, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

		cql = "aliasField as that a test";
		token = lexicalParser.isTableNameDeclaration(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField as that", token.getContent());
		Assert.assertEquals(" a test", token.getPosContent());

		Assert.assertEquals(TokenType.TABLE_NAME_DECLARATION, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.TABLE_NAME, sub.getType());

		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());

		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

	}

	// <ALIAS>::=[<AS> <SPACES>] <ENTITY NAME>
	@Test
	public void alias() throws CQLException {
		String cql = "+select ";
		Token token = lexicalParser.isAlias(cql, false);
		Assert.assertNull(token);

		cql = " aliasField";
		token = lexicalParser.isAlias(cql, false);
		Assert.assertNull(token);

		cql = "aliasField that a test";
		token = lexicalParser.isAlias(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("aliasField", token.getContent());
		Assert.assertEquals(" that a test", token.getPosContent());

		Assert.assertEquals(TokenType.ALIAS, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());

		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ENTITY_NAME, sub.getType());

		cql = "asaliasField that a test";
		token = lexicalParser.isAlias(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("asaliasField", token.getContent());
		Assert.assertEquals(" that a test", token.getPosContent());

		Assert.assertEquals(TokenType.ALIAS, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());

		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ENTITY_NAME, sub.getType());

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
		Assert.assertEquals(TokenType.ENTITY_NAME, sub.getType());

	}

	// <INTO> ::= u(INTO)
	@Test
	public void into() throws CQLException {
		String cql = "select ";
		Token token = lexicalParser.isInto(cql, false);
		Assert.assertNull(token);

		cql = "into (x,y,z) values (z,n,q)";
		token = lexicalParser.isInto(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("into", token.getContent());

		Assert.assertEquals(TokenType.INTO, token.getType());

	}

	// <VALUES> ::= u(VALUES)
	@Test
	public void values() throws CQLException {
		String cql = "select ";
		Token token = lexicalParser.isValues(cql, false);
		Assert.assertNull(token);

		cql = "values (z,n,q)";
		token = lexicalParser.isValues(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("values", token.getContent());

		Assert.assertEquals(TokenType.VALUES, token.getType());
	}

	// <OTHER COMMAND> ::= ^<WHERE> <RESERVED WORDS> [<SPACES>] (<SELECTOR
	// BLOCK> | ( [<SYMBOL>] [<SPACES>] [<LITERAL>] )[<SPACES>] )
	// [[<SPACES>]<OTHER COMMAND>]
	@Test
	public void otherCommand() throws CQLException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "'test' ";
		Token token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNull(token);

		// where starting
		cql = "where a,v,c from tablea";
		token = lexicalParser.isOtherCommands(cql, false);
		Assert.assertNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select a,v,c";
		token = lexicalParser.isOtherCommands(cql, false);
		token = lexicalParser.isOtherCommands(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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

	// <START DELETE COMMAND> ::= <DELETE> <SPACES> [<FROM> <SPACES>] <TABLE
	// NAME>
	@Test
	public void startDeleteCommand() throws CQLException {

		String cql = "'test' ";
		Token token = lexicalParser.isStartDeleteCommand(cql, false);
		Assert.assertNull(token);

		cql = "  delete  ";
		token = lexicalParser.isStartDeleteCommand(cql, false);
		Assert.assertNull(token);

		cql = "deleteteste";
		token = lexicalParser.isStartDeleteCommand(cql, false);
		Assert.assertNull(token);

		cql = "delete users test";
		token = lexicalParser.isStartDeleteCommand(cql, false);
		token = lexicalParser.isStartDeleteCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("delete users", token.getContent());
		Assert.assertEquals(" test", token.getPosContent());
		Assert.assertEquals(TokenType.START_DELETE_COMMAND, token.getType());

		cql = "delete  from  users test";
		token = lexicalParser.isStartDeleteCommand(cql, false);
		token = lexicalParser.isStartDeleteCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("delete  from  users", token.getContent());
		Assert.assertEquals(" test", token.getPosContent());
		Assert.assertEquals(TokenType.START_DELETE_COMMAND, token.getType());

	}

	// <DELETE COMMAND>::=<START DELETE COMMAND> [<SPACES> <END COMMON COMMAND>]
	@Test
	public void deleteCommand() throws CQLException {

		String cql = "'test' ";
		Token token = lexicalParser.isDeleteCommand(cql, false);
		Assert.assertNull(token);

		cql = "  delete  from  testeA  ";
		token = lexicalParser.isDeleteCommand(cql, false);
		Assert.assertNull(token);

		cql = "delete  from  testeA  ";
		token = lexicalParser.isDeleteCommand(cql, false);
		token = lexicalParser.isDeleteCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("delete  from  testeA", token.getContent());
		Assert.assertEquals("  ", token.getPosContent());
		Assert.assertEquals(TokenType.DELETE_COMMAND, token.getType());

	}

	// <UPDATE COMMAND>::=<START UPDATE COMMAND> <SPACES> <END COMMON COMMAND>
	@Test
	public void updateCommand() throws CQLException {

		String cql = "'test' ";
		Token token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "  update  ";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "updateteste";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "update name  ";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "update name  set ";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "update name  set a  = ";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("update name  set a  =", token.getContent());
		Assert.assertEquals(" ", token.getPosContent());
		Assert.assertEquals(TokenType.UPDATE_COMMAND, token.getType());

		cql = "update name  set a";
		token = lexicalParser.isUpdateCommand(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("update name  set a", token.getContent());
		Assert.assertEquals(TokenType.UPDATE_COMMAND, token.getType());

		cql = "update name  set a  = 3  ";
		token = lexicalParser.isUpdateCommand(cql, false);
		token = lexicalParser.isUpdateCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("update name  set a  = 3", token.getContent());
		Assert.assertEquals("  ", token.getPosContent());
		Assert.assertEquals(TokenType.UPDATE_COMMAND, token.getType());

		cql = "UPDATE teste set teste.a=? WHERE b = ?";
		token = lexicalParser.isUpdateCommand(cql, false);
		token = lexicalParser.isUpdateCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("UPDATE teste set teste.a=?", token.getContent());
		Assert.assertEquals(" WHERE b = ?", token.getPosContent());
		Assert.assertEquals(TokenType.UPDATE_COMMAND, token.getType());

	}

	// <START UPDATE COMMAND> ::= <UPDATE> <SPACES> <TABLE NAME> <SPACES> <SET>
	@Test
	public void startUpdateCommand() throws CQLException {

		String cql = "'test' ";
		Token token = lexicalParser.isStartUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "  update  ";
		token = lexicalParser.isStartUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "updateteste";
		token = lexicalParser.isStartUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "update name  ";
		token = lexicalParser.isStartUpdateCommand(cql, false);
		Assert.assertNull(token);

		cql = "update name  set a=a  ";
		token = lexicalParser.isStartUpdateCommand(cql, false);
		token = lexicalParser.isStartUpdateCommand(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("update name  set", token.getContent());
		Assert.assertEquals(TokenType.START_UPDATE_COMMAND, token.getType());

	}

	// <END COMMON COMMAND>::= (<SELECTOR BLOCK> | <SYMBOL> | <LITERAL> )
	// [[<SPACES>]<END COMMON COMMAND>]]
	@Test
	public void endCommonCommand() throws CQLException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "'test' ";
		Token token = lexicalParser.isEndCommonCommand(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("'test'", token.getContent());
		Assert.assertEquals(TokenType.END_COMMON_COMMAND, token.getType());

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select a,v,c";
		token = lexicalParser.isEndCommonCommand(cql, false);
		Assert.assertNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select a,v,c from tablea";
		token = lexicalParser.isEndCommonCommand(cql, false);
		Assert.assertNull(token);

		// where starting
		cql = "where a,v,c from tablea";
		Assert.assertNull(token);
	}

	// <ENTITY NAME> ::= ^<RESERVED WORD> (<ITEM NAME CASE SENSITIVE> | <ITEM
	// NAME CASE INSENSITIVE>)
	@Test
	public void entityName() throws CQLException {
		String cql = " \"1234abcde\" adsfasdf ";
		Token token = lexicalParser.isEntityName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde²\" adsfasdf ";
		token = lexicalParser.isEntityName(cql, false);
		Assert.assertNull(token);

		cql = "\"1234abcde$\" adsfasdf ";
		token = lexicalParser.isEntityName(cql, false);
		Assert.assertNull(token);

		cql = "\"é1234abcde\" adsfasdf ";
		token = lexicalParser.isEntityName(cql, true);
		Assert.assertEquals(TokenType.ENTITY_NAME, token.getType());
		cql = "\"1234abcde\" adsfasdf ";
		token = lexicalParser.isEntityName(cql, true);
		token = lexicalParser.isEntityName(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("\"1234abcde\"", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.ENTITY_NAME, token.getType());

		cql = "abcde1234 adsfasdf ";
		token = lexicalParser.isEntityName(cql, true);
		token = lexicalParser.isEntityName(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ENTITY_NAME, token.getType());

	}

	// <INSERT COMMAND> ::= <INSERT> <SPACES> [<INTO> <SPACES>] <TABLE NAME>
	// [<SPACES>] <START_PARAMETERS> [<SPACES>] <FIELD LIST> [<SPACES>]
	// <END_PARAMETERS> [<SPACES>] <VALUES> [<SPACES>] <START_PARAMETERS>
	// [<SPACES>] [<SELECTOR BLOCK>] [<SPACES>] <END_PARAMETERS>

	 

	@Test
	public void insertCommand() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());

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

	// <CREATE COMMAND> ::= <CREATE TABLE COMMAND> | <CREATE INDEX COMMAND>
	@Test
	public void createCommand() throws CQLException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "select ";
		Token token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = " create ";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create index";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table abc";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNotNull(token);

		cql = "create index abc";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNotNull(token);

		cql = " create table";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table;";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create ; table tab";
		token = lexicalParser.isCreateCommand(cql, false);
		Assert.assertNull(token);

		cql = "create index tab;";
		token = lexicalParser.isCreateCommand(cql, true);
		cql = "create index tab test ; test2";
		token = lexicalParser.isCreateCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.CREATE_COMMAND, token.getType());
		Assert.assertEquals(TokenType.CREATE_INDEX_COMMAND, token
				.getSubTokens().get(0).getType());
		Assert.assertEquals("create index tab test ", token.getContent());
		Assert.assertEquals("; test2", token.getPosContent());

	}

	// <CREATE INDEX COMMAND> ::= <START CREATE INDEX COMMAND> <END CREATE INDEX
	// COMMAND>
	@Test
	public void createIndexCommand() throws CQLException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "select ";
		Token token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = " create ";
		token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table";
		token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = " create table";
		token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table;";
		token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "create ; table tab";
		token = lexicalParser.isCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "create index tab;";
		token = lexicalParser.isCreateIndexCommand(cql, true);
		cql = "create index tab test ; test2";
		token = lexicalParser.isCreateIndexCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.CREATE_INDEX_COMMAND, token.getType());
		Assert.assertEquals("create index tab test ", token.getContent());
		Assert.assertEquals("; test2", token.getPosContent());

	}

	// <CREATE TABLE COMMAND> ::= <START CREATE TABLE> <END CREATE TABLE>
	@Test
	public void createTableCommand() throws CQLException {
		// reserved word, space but not selector item, symbol , literal or other
		// command
		String cql = "select ";
		Token token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = " create ";
		token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table";
		token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = " create table";
		token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table;";
		token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = "create ; table tab";
		token = lexicalParser.isCreateTableCommand(cql, false);
		Assert.assertNull(token);

		cql = "create table tab;";
		token = lexicalParser.isCreateTableCommand(cql, true);
		cql = "create table tab test ; test2";
		token = lexicalParser.isCreateTableCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.CREATE_TABLE_COMMAND, token.getType());
		Assert.assertEquals("create table tab test ", token.getContent());
		Assert.assertEquals("; test2", token.getPosContent());

	}

	// <CONDITIONAL COMMAND> ::= (<DELETE COMMAND> | <UPDATE COMMAND> | <OTHER
	// COMMAND>) [ <SPACES> <CONDITION> ]
	@Test
	public void conidtionalCommand() throws CQLException {

		// start symbol
		String cql = ". teste";
		Token token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNull(token);

		// start string
		cql = "'. teste'";
		token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNull(token);

		// start space
		cql = " . teste'";
		token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNull(token);

		// start literal
		cql = "age . teste'";
		token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNull(token);

		// reserved word but not spaces
		cql = "select";
		token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNotNull(token);

		cql = "selectinsert";
		token = lexicalParser.isConditionalCommand(cql, false);
		Assert.assertNull(token);

		// add symbol
		cql = "select x where x=y  ";

		token = lexicalParser.isConditionalCommand(cql, true);
		token = lexicalParser.isConditionalCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select x where x=y", token.getContent());
		Assert.assertEquals("  ", token.getPosContent());
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

		cql = "UPDATE teste set teste.a=? WHERE b = ?  ";
		token = lexicalParser.isConditionalCommand(cql, true);
		token = lexicalParser.isConditionalCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("UPDATE teste set teste.a=? WHERE b = ?",
				token.getContent());
		Assert.assertEquals("  ", token.getPosContent());
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

	}

 
	//<COMMAND> ::= <CREATE COMMAND>  | <INSERT COMMAND> | <CONDITIONAL COMMAND>
	@Test
	public void command() throws CQLException {

		// start symbol
		String cql = ". teste";
		Token token = lexicalParser.isCommand(cql, false);
		Assert.assertNull(token);

		// start string
		cql = "'. teste'";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNull(token);

		// start space
		cql = " . teste'";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNull(token);

		// start literal
		cql = "age . teste'";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNull(token);

		// reserved word but not spaces
		cql = "select";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNotNull(token);

		cql = "selectinsert";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "select ";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNotNull(token);

		// create index
		cql = "create index abc";
		token = lexicalParser.isCommand(cql, false);
		Assert.assertNotNull(token);

		// reserved word, space but not selector item, symbol , literal or other
		// command
		cql = "insert tab1 (x,y,z) values (z,n,q)";
		token = lexicalParser.isCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(cql, token.getContent());

		cql = "select insert";
		Assert.assertNotNull(lexicalParser.isCommand(cql, false));

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
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

		// add selector item
		cql = "select insert  accounts.id";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select ", token.getContent());
		Assert.assertEquals("insert  accounts.id", token.getPosContent());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

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
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

		// add symbol
		cql = "select x where x=y;select insert  accounts.name";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select x where x=y", token.getContent());
		Assert.assertEquals(";select insert  accounts.name",
				token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());

		cql = "select insert accounts.id;select insert  accounts.name";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("select ", token.getContent());
		Assert.assertEquals("insert accounts.id;select insert  accounts.name",
				token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.CONDITIONAL_COMMAND, token.getType());

		cql = "create table x a b c ; test";

		token = lexicalParser.isCommand(cql, true);
		token = lexicalParser.isCommand(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("create table x a b c ", token.getContent());
		Assert.assertEquals("; test", token.getPosContent());
		Assert.assertEquals(TokenType.COMMAND, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.CREATE_COMMAND, token.getType());
		Assert.assertEquals(TokenType.CREATE_TABLE_COMMAND, token
				.getSubTokens().get(0).getType());

	}

	// <CREATE> :: = CREATE
	@Test
	public void create() throws CQLException {
		String cql = "   CREATE    X=3 AND X=5   ;";
		Token token = lexicalParser.isCreate(cql, false);
		Assert.assertNull(token);

		cql = "CrEATE    X=3 AND X=5   ; teste";
		token = lexicalParser.isCreate(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("CrEATE", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.CREATE, token.getType());
	}

	// <FROM>::=u(FROM)
	@Test
	public void from() throws CQLException {
		String cql = "   FROM    X=3 AND X=5   ;";
		Token token = lexicalParser.isFrom(cql, false);
		Assert.assertNull(token);

		cql = "FrOM    X=3 AND X=5   ; teste";
		token = lexicalParser.isFrom(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("FrOM", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.FROM, token.getType());
	}

	// <SET>::=u(SET)
	@Test
	public void set() throws CQLException {
		String cql = "   SET    X=3 AND X=5   ;";
		Token token = lexicalParser.isSet(cql, false);
		Assert.assertNull(token);

		cql = "SeT    X=3 AND X=5   ; teste";
		token = lexicalParser.isSet(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("SeT", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.SET, token.getType());
	}

	// <UPDATE>::=u(UPDATE)
	@Test
	public void update() throws CQLException {
		String cql = "   UPDATE    X=3 AND X=5   ;";
		Token token = lexicalParser.isUpdate(cql, false);
		Assert.assertNull(token);

		cql = "UpDATE    X=3 AND X=5   ; teste";
		token = lexicalParser.isUpdate(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("UpDATE", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.UPDATE, token.getType());
	}

	// <DELETE>::=u(DELETE)
	@Test
	public void delete() throws CQLException {
		String cql = "   DELETE    X=3 AND X=5   ;";
		Token token = lexicalParser.isDelete(cql, false);
		Assert.assertNull(token);

		cql = "DeLETE    X=3 AND X=5   ; teste";
		token = lexicalParser.isDelete(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("DeLETE", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.DELETE, token.getType());
	}

	// <INDEX> ::= u(INDEX)
	@Test
	public void index() throws CQLException {
		String cql = "   INDEX    X=3 AND X=5   ;";
		Token token = lexicalParser.isIndex(cql, false);
		Assert.assertNull(token);

		cql = "InDEX    X=3 AND X=5   ; teste";
		token = lexicalParser.isIndex(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("InDEX", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.INDEX, token.getType());
	}

	// <TABLE> :: = TABLE
	@Test
	public void table() throws CQLException {
		String cql = "   TABLE X=3 AND X=5   ;";
		Token token = lexicalParser.isTable(cql, false);
		Assert.assertNull(token);

		cql = "TaBle    X=3 AND X=5   ; teste";
		token = lexicalParser.isTable(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("TaBle", token.getContent());
		Assert.assertEquals("    X=3 AND X=5   ; teste", token.getPosContent());
		Assert.assertEquals(TokenType.TABLE, token.getType());
	}

	// <DOT COMMA> :: = ;
	@Test
	public void dotComma() throws CQLException {
		String cql = "   WHERE    X=3 AND X=5   ;";
		Token token = lexicalParser.isDotComma(cql, false);
		Assert.assertNull(token);

		cql = ";   WHERE    X=3 AND X=5   ; teste";
		token = lexicalParser.isDotComma(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(";", token.getContent());
		Assert.assertEquals("   WHERE    X=3 AND X=5   ; teste",
				token.getPosContent());
		Assert.assertEquals(TokenType.DOT_COMMA, token.getType());
	}

	// <START CREATE INDEX COMMAND> ::= <CREATE> <SPACES> <INDEX>
	@Test
	public void startCreateIndexCommand() throws CQLException {
		String cql = " CREATE INDEX    X=3 AND X=5   ;";
		Token token = lexicalParser.isStartCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = " CREATE INDEX    X=3 AND X=5   ;";
		token = lexicalParser.isStartCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "CREATE     X=3 AND X=5   ;";
		token = lexicalParser.isStartCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "CREATE INDEX TAB  WHERE    X=3 AND X=5   ; teste";
		token = lexicalParser.isStartCreateIndexCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("CREATE INDEX", token.getContent());
		Assert.assertEquals(" TAB  WHERE    X=3 AND X=5   ; teste",
				token.getPosContent());
		Assert.assertEquals(TokenType.START_CREATE_INDEX_COMMAND,
				token.getType());
	}

	// <START CREATE TABLE> ::= <CREATE> <SPACES> <TABLE>
	@Test
	public void startCreateTable() throws CQLException {
		String cql = "WHERE    X=3 AND X=5   ;";
		Token token = lexicalParser.isStartCreateTable(cql, false);
		Assert.assertNull(token);

		cql = " WHERE    X=3 AND X=5   ;";
		token = lexicalParser.isStartCreateTable(cql, false);
		Assert.assertNull(token);

		cql = "CREATE    X=3 AND X=5   ;";
		token = lexicalParser.isStartCreateTable(cql, false);
		Assert.assertNull(token);

		cql = "CREATE TABLE TAB  WHERE    X=3 AND X=5   ; teste";
		token = lexicalParser.isStartCreateTable(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("CREATE TABLE", token.getContent());
		Assert.assertEquals(" TAB  WHERE    X=3 AND X=5   ; teste",
				token.getPosContent());
		Assert.assertEquals(TokenType.START_CREATE_TABLE, token.getType());
	}

	// <END CREATE TABLE>::=^<DOT COMMA> <ANY> [<END CREATE TABLE>]
	@Test
	public void endCreateTable() throws CQLException {
		String cql = ";   WHERE    X=3 AND X=5   ;";
		Token token = lexicalParser.isEndCreateTable(cql, false);
		Assert.assertNull(token);

		cql = "   WHERE    X=3 AND X=5   ; teste";
		token = lexicalParser.isEndCreateTable(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("   WHERE    X=3 AND X=5   ", token.getContent());
		Assert.assertEquals("; teste", token.getPosContent());
		Assert.assertEquals(TokenType.END_CREATE_TABLE, token.getType());
		Token subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());
		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.END_CREATE_TABLE, subtoken.getType());
	}

	// <END CREATE INDEX COMMAND>::=^<DOT COMMA> <ANY> [<END CREATE INDEX
	// COMMAND>]
	@Test
	public void endCreateIndexCommand() throws CQLException {
		String cql = ";   WHERE    X=3 AND X=5   ;";
		Token token = lexicalParser.isEndCreateIndexCommand(cql, false);
		Assert.assertNull(token);

		cql = "   WHERE    X=3 AND X=5   ; teste";
		token = lexicalParser.isEndCreateIndexCommand(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("   WHERE    X=3 AND X=5   ", token.getContent());
		Assert.assertEquals("; teste", token.getPosContent());
		Assert.assertEquals(TokenType.END_CREATE_INDEX_COMMAND, token.getType());
		Token subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());
		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.END_CREATE_INDEX_COMMAND,
				subtoken.getType());

	}

	/**
	 * <CONDITION> ::= <WHERE> <CONDITIONS>
	 */
	@Test
	public void condition() throws CQLException {
		String cql = "   WHERE    X=3 AND X=5   ";
		Assert.assertNull(lexicalParser.isCondition(cql, false));
		cql = "WHEREX=3 AND X=5 test";
		Assert.assertNull(lexicalParser.isCondition(cql, false));
		cql = "WHERE    X=3 AND X=5 aliasa   test";
		Token token = lexicalParser.isCondition(cql, false);
		token = lexicalParser.isCondition(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(token.getType(), TokenType.CONDITION);
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(" aliasa   test", token.getPosContent());
		Assert.assertEquals("WHERE    X=3 AND X=5", token.getContent());
		Assert.assertEquals("WHERE", token.getSubTokens().get(0).getContent());

		Assert.assertEquals("    X=3 AND X=5", token.getSubTokens().get(1)
				.getContent());
		Assert.assertEquals("    ", token.getSubTokens().get(1).getSubTokens()
				.get(0).getContent());

	}

	/**
	 * <CONDITION-ITEM>::= <SELECTOR ITEM
	 * STRICT>[<SPACES>]<SYMBOL>[<SPACES>]<SELECTOR ITEM STRICT>
	 */
	@Test
	public void conditionItem() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		token = lexicalParser.isConditionItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id =  3", token.getContent());
		Assert.assertEquals(" aliasA    teste", token.getPosContent());
		Assert.assertEquals(TokenType.CONDITION_ITEM, token.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());
		boolean hasSymbol = false;
		for (Token child : token.getSubTokens()) {
			if (child.getType().equals(TokenType.SYMBOL)) {
				hasSymbol = true;
				break;
			}
		}

		Assert.assertTrue(hasSymbol);

		Assert.assertEquals(5, token.getSubTokens().size());

		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.SYMBOL, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(3)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(4).getType());

	}

	/**
	 * // <CONDITIONS> ::= <SPACES><CONDITION-ITEM>[<SPACES> <JOIN CONDITION>
	 * <CONDITIONS>]
	 */
	@Test
	public void conditions() throws CQLException {
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
		Assert.assertEquals(" aliasa test", token.getPosContent());
		cql = " X=3 AND Y=3 aliasb test";
		token = lexicalParser.isConditions(cql, false);
		token = lexicalParser.isConditions(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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
		Assert.assertEquals(" aliasb test", token.getPosContent());
		cql = " user_token = ?";
		token = lexicalParser.isConditions(cql, false);
		token = lexicalParser.isConditions(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.CONDITION_ITEM,
				token.getSubTokens().get(1).getType());
	}

	@Test
	public void replace() throws CQLException, ParseException {

		String cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";

		Token cqlToken = lexicalParser.isCQL(cql);
		String newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('test',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		Assert.assertEquals(newCQL, cqlToken.replace("test", 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (35,?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		Assert.assertEquals(newCQL, cqlToken.replace(35, 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (-35,?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		Assert.assertEquals(newCQL, cqlToken.replace(-35, 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('test''test',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		Assert.assertEquals(newCQL, cqlToken.replace("test'test", 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('2012-03-05 22:15:36+0000',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("Z"));
		Date date = sdf.parse("2012-03-05 22:15:36");
		Assert.assertEquals(newCQL, cqlToken.replace(date, 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('2012-03-05',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		Assert.assertEquals(newCQL,
				cqlToken.replace(LocalDate.of(2012, Month.MARCH, 05), 0));

		cqlToken = lexicalParser.isCQL(cql);
		newCQL = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('2012-03-05 22:15:36+0000',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL ?";
		ZonedDateTime zonedatetime = ZonedDateTime.of(2012, 3, 05, 22, 15, 36,
				2873676, ZoneId.of("Z"));
		String resultCql = cqlToken.replace(zonedatetime, 0);
		Assert.assertEquals(newCQL, resultCql);

	}

	// <CQL>::= [<SPACES>] <COMMAND> [ <SPACES>] [<USING>] [<DOT COMMA>
	// [<SPACES>]]
	@Test
	public void cql() throws CQLException, ParseException {
		String cql = "select count(*) as valid from accounts where user_token = ?";
		Token cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },[?,?,?],{?:?,?:?,?:?})";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		cql = "CREATE TABLE test "
				+ " (key varchar PRIMARY KEY,email text,age int,tags list<text>,"
				+ " \"friendsByName\" map<text,int>, cmps set<int>)";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?}) USING TTL 10";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		cql = "CREATE INDEX accounts_userid ON accounts (\"oldId\")";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

		Assert.assertEquals(TokenType.CQL, cqlToken.getType());

		cql = "UPDATE teste set teste.a=? WHERE b = ?";
		cqlToken = lexicalParser.isCQL(cql);
		Assert.assertNotNull(cqlToken);
		Assert.assertEquals(cql, cqlToken.getContent());

	}

	// <USING OPTIONS>::= <TTL>
	@Test
	public void usingOptions() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isUsingOptions(cql, false);
		Assert.assertNull(token);
		cql = " TTL 33";
		token = lexicalParser.isUsingOptions(cql, false);
		Assert.assertNull(token);
		cql = "TTL a";
		token = lexicalParser.isUsingOptions(cql, false);
		Assert.assertNull(token);

		cql = "TTL 3a3";
		token = lexicalParser.isUsingOptions(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.USING_OPTIONS, token.getType());
		Assert.assertEquals("TTL 3", token.getContent());
		Assert.assertEquals("a3", token.getPosContent());

		cql = "TTL ?a3";
		token = lexicalParser.isUsingOptions(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.USING_OPTIONS, token.getType());
		Assert.assertEquals("TTL ?", token.getContent());
		Assert.assertEquals("a3", token.getPosContent());
		Assert.assertEquals(TokenType.TTL, token.getSubTokens().get(0)
				.getType());

	}

	// <START USING> ::= "USING"
	@Test
	public void startUsing() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);
		cql = " TTL 33";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);
		cql = "TTL a";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);

		cql = "TTL 3a3";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);

		cql = "TTL ?a3";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);

		cql = " USING TTL ?a3";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNull(token);

		cql = "USINGTTL ?a3";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.START_USING, token.getType());
		Assert.assertEquals("USING", token.getContent());
		Assert.assertEquals("TTL ?a3", token.getPosContent());

		cql = "USING TTL ?a3";
		token = lexicalParser.isStartUsing(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.START_USING, token.getType());
		Assert.assertEquals("USING", token.getContent());
		Assert.assertEquals(" TTL ?a3", token.getPosContent());

	}

	// <USING>::=<START USING> <SPACES> <USING OPTIONS>
	@Test
	public void using() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);
		cql = " TTL 33";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);
		cql = "TTL a";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);

		cql = "TTL 3a3";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);

		cql = "TTL ?a3";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);

		cql = "USINGTTL ?a3";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);

		cql = " USING TTL ?a3";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertNull(token);

		cql = "USING TTL ?a3";
		token = lexicalParser.isUsing(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.USING, token.getType());
		Assert.assertEquals("USING TTL ?", token.getContent());
		Assert.assertEquals("a3", token.getPosContent());

	}

	// <START TTL> ::= u("TTL")
	@Test
	public void ttlStart() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isStartTTL(cql, false);
		Assert.assertNull(token);
		cql = " TTL 33";
		token = lexicalParser.isStartTTL(cql, false);
		Assert.assertNull(token);
		cql = "TTL a";
		token = lexicalParser.isStartTTL(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertEquals(TokenType.START_TTL, token.getType());
		Assert.assertEquals("TTL", token.getContent());
		Assert.assertEquals(" a", token.getPosContent());

	}

	// <TTL>::= "TTL" (<NUMBER> | <INJECT> )
	@Test
	public void ttl() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isTTL(cql, false);
		Assert.assertNull(token);
		cql = " TTL 33";
		token = lexicalParser.isTTL(cql, false);
		Assert.assertNull(token);
		cql = "TTL a";
		token = lexicalParser.isTTL(cql, false);
		Assert.assertNull(token);

		cql = "TTL 3a3";
		token = lexicalParser.isTTL(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.TTL, token.getType());
		Assert.assertEquals("TTL 3", token.getContent());
		Assert.assertEquals("a3", token.getPosContent());

		cql = "TTL ?a3";
		token = lexicalParser.isTTL(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.TTL, token.getType());
		Assert.assertEquals("TTL ?", token.getContent());
		Assert.assertEquals("a3", token.getPosContent());

	}

	// <END_PARAMETERS>::=)
	@Test
	public void endParameters() throws CQLException {

		// right
		String cql = ")";
		Token token = lexicalParser.isEndParameters(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isEndParameters(cql, false);
		Assert.assertNotNull(token);
		cql = ") test ";
		token = lexicalParser.isEndParameters(cql, true);
		token = lexicalParser.isEndParameters(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(")", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals(TokenType.END_PARAMETERS, token.getType());

		// wrong

		cql = " )test ";
		token = lexicalParser.isEndParameters(cql, false);
		Assert.assertNull(token);

	}

	// <END BRACKET>::=)
	@Test
	public void endArray() throws CQLException {
		String cql = "]";
		Token token = lexicalParser.isEndBracket(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isEndBracket(cql, false);
		Assert.assertNotNull(token);
		cql = " ]test ";
		token = lexicalParser.isEndBracket(cql, false);
		Assert.assertNull(token);

		cql = "] test ";
		token = lexicalParser.isEndBracket(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isEndBracket(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("]", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());

		Assert.assertEquals(TokenType.END_BRACKET, token.getType());

	}

	// <END MAP>::=}
	@Test
	public void endMap() throws CQLException {
		String cql = "}";
		Token token = lexicalParser.isEndBrace(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isEndBrace(cql, false);
		Assert.assertNotNull(token);
		cql = "} test ";
		token = lexicalParser.isEndBrace(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isEndBrace(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("}", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		Assert.assertEquals(TokenType.END_BRACE, token.getType());
	}

	// // <FUNCTION>::=<ITEM
	// NAME>[<SPACES>]<START_PARAMETERS>[<SPACES>][<SELECTOR
	// BLOCK>][<SPACES>]<END_PARAMETERS>
	@Test
	public void function() throws CQLException {
		String cql = "count(*) ";
		Token token = lexicalParser.isFunction(cql, false);
		token = lexicalParser.isFunction(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);

		Assert.assertEquals(TokenType.FUNCTION, token.getType());

		cql = "count(abc.def)";
		token = lexicalParser.isFunction(cql, false);
		token = lexicalParser.isFunction(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.FUNCTION, token.getType());
	}

	@Test
	// <SIGN>::=+|-
	public void sign() throws CQLException {
		String cql = " +";
		Token token = lexicalParser.isSign(cql, false);
		Assert.assertNull(token);
		cql = "*";
		token = lexicalParser.isSign(cql, false);
		Assert.assertNull(token);
		cql = "+";
		token = lexicalParser.isSign(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		cql = "-";
		token = lexicalParser.isSign(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SIGN, token.getType());
	}

	@Test
	// <ABSOLUTE HEX>::= (<HEXA CHAR>|<DIGIT>) [<ABSOLUTE HEX>]
	public void absoluteHexa() throws CQLException {
		String cql = " 0x1234abcde adsfasdf ";
		Token token = lexicalParser.isAbsoluteHexa(cql, false);
		Assert.assertNull(token);
		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isAbsoluteHexa(cql, false);
		Assert.assertNull(token);

		cql = "-1234abc1-3 1234Éabcde adsfasdf ";
		token = lexicalParser.isAbsoluteHexa(cql, false);
		Assert.assertNull(token);

		cql = "0x1234abcde adsfasdf ";
		token = lexicalParser.isAbsoluteHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("0", token.getContent());
		Assert.assertEquals("x1234abcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ABSOLUTE_HEX, token.getType());

		cql = "1234abc1-3 1234Éabcde adsfasdf ";
		token = lexicalParser.isAbsoluteHexa(cql, false);
		token = lexicalParser.isAbsoluteHexa(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("1234abc1", token.getContent());
		Assert.assertEquals("-3 1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ABSOLUTE_HEX, token.getType());

	}

	@Test
	// <HEXA CHAR> :: = u(a-f)
	public void hexaChar() throws CQLException {
		String cql = " abcde134 adsfasdf ";
		Token token = lexicalParser.isHexaChar(cql, false);
		Assert.assertNull(token);
		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isHexaChar(cql, false);
		Assert.assertNull(token);

		cql = "habcdehf1234-de adsfasdf ";
		token = lexicalParser.isHexaChar(cql, false);
		Assert.assertNull(token);

		cql = "e1234abc-de adsfasdf ";
		token = lexicalParser.isHexaChar(cql, false);
		token = lexicalParser.isHexaChar(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("e", token.getContent());

		cql = "abcdef1234-de adsfasdf ";
		token = lexicalParser.isHexaChar(cql, false);
		token = lexicalParser.isHexaChar(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.HEXA_CHAR, token.getType());
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertEquals("a", token.getContent());

	}

	@Test
	// <START HEX> ::= u(0X)
	public void startHexa() throws CQLException {
		String cql = "e1234abc-de adsfasdf ";
		Token token = lexicalParser.isStartHexa(cql, false);
		Assert.assertNull(token);

		cql = "0X1234Éabcde adsfasdf ";
		token = lexicalParser.isStartHexa(cql, false);
		token = lexicalParser.isStartHexa(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals("0X", token.getContent());
		Assert.assertEquals("1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.START_HEX, token.getType());

		cql = "0x1234Éabcde adsfasdf ";
		token = lexicalParser.isStartHexa(cql, false);
		token = lexicalParser.isStartHexa(cql, true);
		Assert.assertNotNull(token);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertEquals("0x", token.getContent());
		Assert.assertEquals("1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.START_HEX, token.getType());

	}

	@Test
	// <HEXA>::= [<SIGN>] [<START HEX>] <ABSOLUTE HEX>
	public void hexa() throws CQLException {
		String cql = " 0x1234abcde adsfasdf ";
		Token token = lexicalParser.isHexa(cql, false);
		Assert.assertNull(token);
		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNull(token);

		cql = "e1234abc-de adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);

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

		cql = "-0X1234abcde² adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("-0X1234abcde", token.getContent());
		Assert.assertEquals("² adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SIGN, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.START_HEX, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.ABSOLUTE_HEX, token.getSubTokens().get(2)
				.getType());

		cql = "+0X1234abcde² adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("+0X1234abcde", token.getContent());
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("0X1234abcde", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

		cql = "-0X1234abc1-3 1234Éabcde adsfasdf ";
		token = lexicalParser.isHexa(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("-0X1234abc1", token.getContent());
		Assert.assertEquals("-3 1234Éabcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.HEX, token.getType());

	}

	/**
	 * <INJECT> ::= ?
	 */
	@Test
	public void inject() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "'?. teste";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "";
		Assert.assertNull(lexicalParser.isInject(cql, false));

		cql = "?. \n adf '' ; * ?  teste' 123";
		Token token = lexicalParser.isInject(cql, true);
		token = lexicalParser.isInject(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());

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
	 * <INPUT CHARACTER EXCEPT SINGLE>::= (<SINGLE QUOTED><SINGLE QUOTED> |
	 * (^<SINGLE QUOTED><ANY>))[<INPUT CHARACTER EXCEPT SINGLE>]
	 */
	@Test
	public void inputCharacterExeptSingle() throws CQLException {
		String cql = ". ";
		Token token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(". ", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());

		Assert.assertEquals(TokenType.ANY, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE, token
				.getSubTokens().get(1).getType());

		Assert.assertEquals(2, token.getSubTokens().size());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());

		Token subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				subtoken.getType());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());
		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				subtoken.getType());

		cql = "adf ''' 123";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("adf ''", token.getContent());
		Assert.assertEquals("' 123", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptSingle(cql, true);
		token = lexicalParser.isInputCharacterExceptSingle(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("'teste", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				token.getType());

		subtoken = token.getSubTokens().get(0);

		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);

		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_SINGLE,
				subtoken.getType());

	}

	/**
	 * <INPUT CHARACTER EXCEPT DOUBLE>::= (<DOUBLE QUOTED><DOUBLE QUOTED> |
	 * (^<DOUBLE QUOTED><ANY>))[<INPUT CHARACTER EXCEPT DOUBLE>]
	 */
	@Test
	public void inputCharacterExeptDouble() throws CQLException {
		String cql = ". teste";
		Token token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals(". teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		Token subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				subtoken.getType());

		cql = "teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("teste'teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				subtoken.getType());

		cql = "teste''teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				subtoken.getType());

		cql = "teste''teste'teste";
		token = lexicalParser.isInputCharacterExceptDouble(cql, true);
		token = lexicalParser.isInputCharacterExceptDouble(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("teste''teste'teste", token.getContent());
		Assert.assertEquals("", token.getPosContent());
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				token.getType());

		subtoken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ANY, subtoken.getType());

		subtoken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.INPUT_CHARACTER_EXCEPT_DOUBLE,
				subtoken.getType());

	}

	// <ITEM NAME> ::= <ITEM NAME CASE SENSITIVE> | <ITEM NAME CASE INSENSITIVE>
	// | <ASTERISK>
	@Test
	public void itemName() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.ITEMNAME, token.getType());
		Assert.assertEquals(TokenType.ITEM_NAME_CASE_INSENSITIVE, token
				.getSubTokens().get(0).getType());

	}

	// <ITEM NAME CASE INSENSITIVE> ::= ^<NUMBER> <CHARS>
	@Test
	public void itemNameCaseInsensitive() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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

	// <ITEM NAME CASE SENSITIVE>::= <DOUBLE QUOTED><CHARS><DOUBLE QUOTED>
	@Test
	public void itemNameCaseSensitive() throws CQLException {
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
		Token subToken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, subToken.getType());
		subToken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.CHARS, subToken.getType());
		subToken = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, subToken.getType());

		cql = "\"1234abcde\" adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, true);
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("\"1234abcde\"", token.getContent());
		Assert.assertEquals(" adsfasdf ", token.getPosContent());
		Assert.assertEquals(3, token.getSubTokens().size());
		subToken = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, subToken.getType());
		subToken = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.CHARS, subToken.getType());
		subToken = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.DOUBLE_QUOTED, subToken.getType());
		Assert.assertNull(token.getSubTokens().get(0).getBefore());
		Assert.assertEquals(TokenType.CHARS, token.getSubTokens().get(0)
				.getAfter().getType());

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isItemNameCaseSensitive(cql, false);
		Assert.assertNull(token);
	}

	/**
	 * <LITERAL> ::= (<NUMBER> | <STRING> | <HEXA>)^<CHARS>
	 */
	@Test
	public void literal() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());

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
	public void number() throws CQLException {
		String cql = " 1234abcde adsfasdf ";
		Token token = lexicalParser.isNumber(cql, false);
		Assert.assertNull(token);

		cql = "é1234abcde adsfasdf ";
		token = lexicalParser.isNumber(cql, false);
		Assert.assertNull(token);

		cql = "1234Éabcde adsfasdf ";
		token = lexicalParser.isNumber(cql, false);
		token = lexicalParser.isNumber(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);

		Assert.assertEquals(TokenType.NUMBER, token.getType());

	}

	// <DIGIT> ::= (0-9)
	@Test
	public void digit() throws CQLException {
		String cql = "é1234Éabcde adsfasdf ";
		Token token = lexicalParser.isDigit(cql, false);
		Assert.assertNull(token);

		cql = " 1234Éabcde adsfasdf ";
		token = lexicalParser.isDigit(cql, false);
		Assert.assertNull(token);

		cql = "1234abcde adsfasdf ";
		token = lexicalParser.isDigit(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("1", token.getContent());
		Assert.assertEquals("234abcde adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.DIGIT, token.getType());

	}

	// <OR> ::= u(OR)
	@Test
	public void or() throws CQLException {
		String cql = "algo,test";
		Token token = lexicalParser.isComma(cql, false);
		Assert.assertNull(token);
		cql = "Or test";
		token = lexicalParser.isOr(cql, false);
		token = lexicalParser.isOr(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void reservedWord() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("SeLeCt", token.getContent());
		Assert.assertEquals(" TEST ", token.getPosContent());
		Assert.assertEquals(TokenType.RESERVED_WORD, token.getType());
	}

	@Test
	public void reservedWords() throws CQLException {
		String cql = "abcd";
		Token token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = " SELECT INSERT  UPDATE   DELETE ";
		token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = "SELECT INSERT  UPDATE   DELETE";
		token = lexicalParser.isReservedWords(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void reservedWordsIgnoreDoubleSpaces() throws CQLException {
		String cql = "abcd";
		Token token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = " SELECT  INSERT UPDATE    DELETE ";
		token = lexicalParser.isReservedWords(cql, false);
		Assert.assertNull(token);

		cql = "SELECT  INSERT   UPDATE    DELETE";
		token = lexicalParser.isReservedWords(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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

	// <SELECTOR BLOCK> ::= <FIELD VALUE> [[<SPACES>] <COMMA> [<SPACES>]
	// <SELECTOR BLOCK>]
	@Test
	public void selectorBlock() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("3,5,6", token.getContent());
		Assert.assertEquals("] , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());

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
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.COMMA, token.getSubTokens().get(2)
				.getType());
		Assert.assertEquals(TokenType.SPACES, token.getSubTokens().get(3)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(4).getType());

		cql = "abc.def)";
		token = lexicalParser.isSelectorBlock(cql, false);
		Assert.assertNotNull(token);

		Assert.assertEquals(TokenType.SELECTOR_BLOCK, token.getType());

	}

	/**
	 * <SELECTOR ITEM>::= <SELECTOR ITEM STRICT> [<SPACES><ALIAS>]
	 */
	@Test
	public void selectorItem() throws CQLException {
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
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());

		cql = "'. \n adf '' ; * ?  teste' 123";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());

		cql = "3";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());

		cql = "'. \n adf '' ; * ?  teste' 123'";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());

		cql = "abcde1234 adsfasdf adsfasdf";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(3, token.getSubTokens().size());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());
		Assert.assertEquals("abcde1234 adsfasdf", token.getContent());
		Assert.assertEquals(" adsfasdf", token.getPosContent());

		cql = "? afd adsf adsfasdf ";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("? afd", token.getContent());
		Assert.assertEquals(" adsf adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());

		cql = "accounts.id = 3";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id", token.getContent());
		Assert.assertEquals(" = 3", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token
				.getSubTokens().get(0).getType());

		cql = "[3,5,6] , 6";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5,6]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());

		cql = "{idade:3,min:5} , 6";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{idade:3,min:5}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());

		cql = "tableA.fieldA as fieldB, test";
		token = lexicalParser.isSelectorItem(cql, true);
		token = lexicalParser.isSelectorItem(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("tableA.fieldA as fieldB", token.getContent());
		Assert.assertEquals(", test", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM, token.getType());
		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, sub.getType());
		sub = token.getSubTokens().get(1);
		Assert.assertEquals(TokenType.SPACES, sub.getType());
		sub = token.getSubTokens().get(2);
		Assert.assertEquals(TokenType.ALIAS, sub.getType());

	}

	// <FIELD VALUE> ::= <FUNCTION> | <ARRAY> | <MAP> | <LITERAL> | <FIELD NAME>
	@Test
	public void fieldValue() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isFieldValue(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isFieldValue(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isFieldValue(cql, false));

		cql = "count(abc.def)";
		Token token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FUNCTION, token.getType());

		cql = "'. \n adf '' ; * ?  teste' 123";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "3";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "'. \n adf '' ; * ?  teste' 123'";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "abcde1234 adsfasdf adsfasdf";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.FIELD_NAME, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf adsfasdf", token.getPosContent());

		cql = "? afd adsf adsfasdf ";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("?", token.getContent());
		Assert.assertEquals(" afd adsf adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

		cql = "accounts.id = 3";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id", token.getContent());
		Assert.assertEquals(" = 3", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(TokenType.FIELD_NAME, token.getSubTokens().get(0)
				.getType());

		cql = "[3,5,6] , 6";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5,6]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY_BRACKET, token.getType());

		cql = "{idade:3,min:5} , 6";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{idade:3,min:5}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(TokenType.START_BRACE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, token.getSubTokens().get(2)
				.getType());

		cql = "tableA.fieldA as fieldB, test";
		token = lexicalParser.isFieldValue(cql, true);
		token = lexicalParser.isFieldValue(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("tableA.fieldA", token.getContent());
		Assert.assertEquals(" as fieldB, test", token.getPosContent());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_NAME, sub.getType());

	}

	// <FIELD LIST> ::= <FIELD NAME> [[<SPACES>] <COMMA> [<SPACES>] <FIELD
	// LIST>]
	@Test
	public void fieldList() throws CQLException {
		String cql = ". test";
		Assert.assertNull(lexicalParser.isFieldList(cql, false));

		cql = "'. test";
		Assert.assertNull(lexicalParser.isFieldList(cql, false));

		cql = ". test'";
		Assert.assertNull(lexicalParser.isFieldList(cql, false));

		cql = "a,b,c test";
		Token token = lexicalParser.isFieldList(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("a,b,c", token.getContent());
		Assert.assertEquals(" test", token.getPosContent());
		Assert.assertEquals(token.getContent(), token.rebuild());

		Assert.assertEquals(TokenType.FIELD_LIST, token.getType());

	}

	// <SELECTOR ITEM STRICT> ::= ^<RESERVED WORD> <FIELD VALUE>
	@Test
	public void selectorItemStrict() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isSelectorItemStrict(cql, false));

		cql = "'. teste";
		Assert.assertNull(lexicalParser.isSelectorItemStrict(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isSelectorItemStrict(cql, false));

		cql = "count(abc.def)";
		Token token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());

		cql = "'. \n adf '' ; * ?  teste' 123";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());

		cql = "3";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());

		cql = "'. \n adf '' ; * ?  teste' 123'";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);

		Assert.assertNotNull(token);
		Assert.assertEquals("'. \n adf '' ; * ?  teste'", token.getContent());
		Assert.assertEquals(" 123'", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());

		cql = "abcde1234 adsfasdf adsfasdf";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals("abcde1234", token.getContent());
		Assert.assertEquals(" adsfasdf adsfasdf", token.getPosContent());

		cql = "? afd adsf adsfasdf ";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("?", token.getContent());
		Assert.assertEquals(" afd adsf adsfasdf ", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());

		cql = "accounts.id = 3";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("accounts.id", token.getContent());
		Assert.assertEquals(" = 3", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getSubTokens().get(0)
				.getType());

		cql = "[3,5,6] , 6";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5,6]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY, token.getType());

		cql = "{idade:3,min:5} , 6";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{idade:3,min:5}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		token = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, token.getType());
		Assert.assertEquals(TokenType.MAP, token.getSubTokens().get(0)
				.getType());

		cql = "tableA.fieldA as fieldB, test";
		token = lexicalParser.isSelectorItemStrict(cql, true);
		token = lexicalParser.isSelectorItemStrict(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("tableA.fieldA", token.getContent());
		Assert.assertEquals(" as fieldB, test", token.getPosContent());
		Assert.assertEquals(TokenType.SELECTOR_ITEM_STRICT, token.getType());
		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.FIELD_VALUE, sub.getType());

	}

	// <ARRAY BRACE> ::= <START BRACE>[<SPACES>][<SELECTOR BLOCK>][<SPACES>]<END
	// BRACE>
	@Test
	public void arrayBrace() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isArrayBrace(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isArrayBrace(cql, false));

		cql = "{} , 6";
		Token token = lexicalParser.isArrayBrace(cql, true);
		token = lexicalParser.isArrayBrace(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("{}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY_BRACE, token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(TokenType.START_BRACE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, token.getSubTokens().get(1)
				.getType());

		cql = "{'Test' , 35} , 6";
		token = lexicalParser.isArrayBrace(cql, true);
		token = lexicalParser.isArrayBrace(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{'Test' , 35}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY_BRACE, token.getType());
		Assert.assertEquals(TokenType.START_BRACE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.END_BRACE, token.getSubTokens().get(2)
				.getType());

	}

	/**
	 * <MAP> ::= <START MAP>[<SPACES>][<PROPERTIES>][<SPACES>]<END MAP>
	 */
	@Test
	public void map() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isMap(cql, false));

		cql = "[3,5] , 6";
		Assert.assertNull(lexicalParser.isMap(cql, false));

		cql = "{} , 6";
		Token token = lexicalParser.isMap(cql, true);
		token = lexicalParser.isMap(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("{}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(2, token.getSubTokens().size());
		Assert.assertEquals(TokenType.START_BRACE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, token.getSubTokens().get(1)
				.getType());

		cql = "{name:'Test',age:35} , 6";
		token = lexicalParser.isMap(cql, true);
		token = lexicalParser.isMap(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{name:'Test',age:35}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.MAP, token.getType());
		Assert.assertEquals(TokenType.START_BRACE, token.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.PROPERTIES, token.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, token.getSubTokens().get(2)
				.getType());

	}

	/**
	 * <PROPERTIES> ::= <PROPERTY> [[<SPACES>]<COMMA>[<SPACES>] <PROPERTIES>]
	 */
	@Test
	public void properties() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void property() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void key() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("'name'", token.getContent());
		Assert.assertEquals(TokenType.KEY, token.getType());
		Assert.assertEquals(1, token.getSubTokens().size());
		Assert.assertEquals(TokenType.LITERAL, token.getSubTokens().get(0)
				.getType());

	}

	/**
	 * <ARRAY>::= <ARRAY BRACKET> | <ARRAY BRACE>
	 */
	@Test
	public void array() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isArray(cql, false));

		cql = "[3,5] , 6";
		Token token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		Token sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY_BRACKET, sub.getType());
		sub = sub.getSubTokens().get(0);

		Assert.assertEquals(TokenType.START_BRACKET, sub.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK, sub.getAfter().getType());
		Assert.assertEquals(TokenType.END_BRACKET, sub.getAfter().getAfter()
				.getType());

		cql = "[] , 6";
		token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY_BRACKET, sub.getType());
		Assert.assertEquals(TokenType.START_BRACKET, sub.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_BRACKET, sub.getSubTokens().get(1)
				.getType());

		cql = "{3,5} , 6";
		token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{3,5}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY_BRACE, sub.getType());
		Assert.assertEquals(TokenType.START_BRACE, sub.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK, sub.getSubTokens().get(1)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, sub.getSubTokens().get(2)
				.getType());

		cql = "{} , 6";
		token = lexicalParser.isArray(cql, true);
		token = lexicalParser.isArray(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("{}", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY, token.getType());
		sub = token.getSubTokens().get(0);
		Assert.assertEquals(TokenType.ARRAY_BRACE, sub.getType());
		Assert.assertEquals(TokenType.START_BRACE, sub.getSubTokens().get(0)
				.getType());
		Assert.assertEquals(TokenType.END_BRACE, sub.getSubTokens().get(1)
				.getType());

	}

	/**
	 * <ARRAY BRACKET>::= <START BRACKET>[<SPACES>][<SELECTOR
	 * BLOCK>][<SPACES>]<END BRACKET>
	 */
	@Test
	public void arrayBracket() throws CQLException {
		String cql = ". teste";
		Token token = lexicalParser.isArrayBracket(cql, false);
		Assert.assertNull(token);

		cql = "{3,5} , 6";
		token = lexicalParser.isArrayBracket(cql, false);
		Assert.assertNull(token);

		cql = "[3,5] , 6";
		token = lexicalParser.isArrayBracket(cql, true);
		token = lexicalParser.isArrayBracket(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[3,5]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY_BRACKET, token.getType());
		Assert.assertEquals(TokenType.START_BRACKET, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.SELECTOR_BLOCK,
				token.getSubTokens().get(1).getType());
		Assert.assertEquals(TokenType.END_BRACKET, token.getSubTokens().get(2)
				.getType());

		cql = "[] , 6";
		token = lexicalParser.isArrayBracket(cql, true);
		token = lexicalParser.isArrayBracket(cql, false);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		Assert.assertEquals("[]", token.getContent());
		Assert.assertEquals(" , 6", token.getPosContent());
		Assert.assertEquals(TokenType.ARRAY_BRACKET, token.getType());
		Assert.assertEquals(TokenType.START_BRACKET, token.getSubTokens()
				.get(0).getType());
		Assert.assertEquals(TokenType.END_BRACKET, token.getSubTokens().get(1)
				.getType());

	}

	// <SINGLE QUOTED> ::= '
	@Test
	public void singleQuoted() throws CQLException {
		String cql = "'";
		Token token = lexicalParser.isSingleQuoted(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isSingleQuoted(cql, false);
		Assert.assertNotNull(token);
		cql = "' test ";
		token = lexicalParser.isSingleQuoted(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		token = lexicalParser.isSingleQuoted(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("'", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		cql = " 'test ";
		token = lexicalParser.isEndParameters(cql, false);
		Assert.assertNull(token);

	}

	@Test
	public void spaces() throws CQLException {
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
	public void chars() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void startParameters() throws CQLException {
		String cql = "(";
		Token token = lexicalParser.isStartParameters(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isStartParameters(cql, false);
		Assert.assertNotNull(token);
		cql = "( test ";
		token = lexicalParser.isStartParameters(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		token = lexicalParser.isStartParameters(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("(", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		cql = " (test ";
		token = lexicalParser.isStartParameters(cql, false);
		Assert.assertNull(token);

	}

	// <START BRACKET>::=(
	@Test
	public void startBracket() throws CQLException {
		String cql = "[";
		Token token = lexicalParser.isStartBracket(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isStartBracket(cql, false);
		Assert.assertNotNull(token);
		cql = "[ test ";
		token = lexicalParser.isStartBracket(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		token = lexicalParser.isStartBracket(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("[", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		cql = " [test ";
		token = lexicalParser.isStartBracket(cql, false);
		Assert.assertNull(token);

	}

	// <START BRACE>::={
	@Test
	public void startBrace() throws CQLException {
		String cql = "{";
		Token token = lexicalParser.isStartBrace(cql, true);
		Assert.assertNotNull(token);
		token = lexicalParser.isStartBrace(cql, false);
		Assert.assertNotNull(token);
		cql = "{ test ";
		token = lexicalParser.isStartBrace(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
		Assert.assertNotNull(token);
		token = lexicalParser.isStartBrace(cql, false);
		Assert.assertNotNull(token);
		Assert.assertEquals("{", token.getContent());
		Assert.assertEquals(" test ", token.getPosContent());
		Assert.assertNull(token.getBefore());
		Assert.assertNull(token.getAfter());
		Assert.assertEquals(0, token.getSubTokens().size());
		cql = " {test ";
		token = lexicalParser.isStartBrace(cql, false);
		Assert.assertNull(token);

	}

	/**
	 * <STRING>::= (<SINGLE QUOTED>[<INPUT CHARACTER EXCEPT SINGLE>]<SINGLE
	 * QUOTED>) | (<DOUBLE QUOTED>[<INPUT CHARACTER EXCEPT DOUBLE>]<DOUBLE
	 * QUOTED>)
	 */
	@Test
	public void string() throws CQLException {
		String cql = ". teste";
		Assert.assertNull(lexicalParser.isString(cql, false));

		cql = "'. teste";
		Assert.assertNull("Non string [" + cql + "]",
				lexicalParser.isString(cql, false));

		cql = ". teste'";
		Assert.assertNull(lexicalParser.isString(cql, false));

		cql = "'. \n adf '' ; * ?  test' 123";
		Token token = lexicalParser.isString(cql, true);
		Assert.assertEquals(token.rebuild(), token.getContent());
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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
	public void symbol() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());

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
	public void where() throws CQLException {
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
		Assert.assertEquals(token.rebuild(), token.getContent());
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
