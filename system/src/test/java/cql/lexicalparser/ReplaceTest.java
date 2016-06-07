package cql.lexicalparser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cql.Token;
import cql.TokenType;
import cql.lexicalparser.exceptions.CQLException;

public class ReplaceTest {

	private LexicalParser lexicalParser = null;

	@Before
	public void cleanup() throws CQLException {
		lexicalParser = new LexicalParser();
	}

	@Test
	public void all() throws CQLException {
		String cql = "INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES (?,?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?})";

		Token tokenCQL = lexicalParser.isCQL(cql);

		tokenCQL.replace(TokenType.INJECT, "userTest", 0);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest',?,?,[?,?,?],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		// TODO: deveria ser zero, mas mesmo assim não explica o erro.
		tokenCQL.replace(TokenType.INJECT, "userTest@gmail.com", 1);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',?,[?,?,?],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 45, 2);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,[?,?,?],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Pelé1", 3);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1',?,?],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Pelé2", 4);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2',?],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Pelé3", 5);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{?:?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Eduardo", 6);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':?,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 1, 7);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,?:? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Rafael", 8);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':? ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 2, 9);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,?:? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, "Gabi", 10);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,'Gabi':? },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 3, 11);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,'Gabi':3 },{?,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 33, 12);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,'Gabi':3 },{33,?,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 44, 13);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,'Gabi':3 },{33,44,?})",
				tokenCQL.getContent());

		tokenCQL.replace(TokenType.INJECT, 55, 14);
		Assert.assertEquals(
				"INSERT INTO test (key,email,age,tags,\"friendsByName\",cmps) VALUES ('userTest','userTest@gmail.com',45,['Pelé1','Pelé2','Pelé3'],{'Eduardo':1,'Rafael':2 ,'Gabi':3 },{33,44,55})",
				tokenCQL.getContent());

	}

	@Test
	public void simpleInteger() throws CQLException {
		String cql = "INSERT INTO test (age,year,day) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		tokenCQL.replace(TokenType.INJECT, 5, 2);
		// left 2, last again (year)
		tokenCQL.replace(TokenType.INJECT, 2016, 1);
		// left 1, last again (age)
		tokenCQL.replace(TokenType.INJECT, 33, 0);
		Assert.assertEquals(
				"INSERT INTO test (age,year,day) VALUES (33,2016,5)",
				tokenCQL.getContent());

		cql = "INSERT INTO test (age,year,day) VALUES (?,?,?)";

		tokenCQL = lexicalParser.isCQL(cql);

		// replace ever first
		tokenCQL.replace(TokenType.INJECT, 33, 0);
		tokenCQL.replace(TokenType.INJECT, 2016, 0);
		tokenCQL.replace(TokenType.INJECT, 5, 0);

		Assert.assertEquals(
				"INSERT INTO test (age,year,day) VALUES (33,2016,5)",
				tokenCQL.getContent());
	}

	@Test
	public void simpleString() throws CQLException {
		String cql = "INSERT INTO test (key,name,lastname) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever first
		tokenCQL.replace(TokenType.INJECT, "ABC", 0);
		tokenCQL.replace(TokenType.INJECT, "Tom", 0);
		tokenCQL.replace(TokenType.INJECT, "Muller", 0);

		Assert.assertEquals(
				"INSERT INTO test (key,name,lastname) VALUES ('ABC','Tom','Muller')",
				tokenCQL.getContent());
	}
}
