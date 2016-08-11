package cql.lexicalparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cql.Token;
import cql.TokenType;
import cql.lexicalparser.exceptions.CQLException;

public class TokenTest {

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
	public void nullTest() throws CQLException {

		String cql = "INSERT INTO test (age,year,day) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		tokenCQL.replace(TokenType.INJECT, 5, 2);
		// left 2, last again (year)
		tokenCQL.replace(TokenType.INJECT, null, 1);
		// left 1, last again (age)
		tokenCQL.replace(TokenType.INJECT, null, 0);
		Assert.assertEquals(
				"INSERT INTO test (age,year,day) VALUES (NULL,NULL,5)",
				tokenCQL.getContent());

	}

	@Test
	public void count() throws CQLException {

		String cql = "INSERT INTO test (age,year,day) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		int count = tokenCQL.count(TokenType.FIELD_NAME);

		Assert.assertEquals(3, count);

		cql = "SELECT 1,2,3,4,5,6,7,8 FROM TEST WHERE 9=0 ";

		tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		count = tokenCQL.count(TokenType.LITERAL);

		Assert.assertEquals(10, count);
	}

	@Test
	public void replaceAll() throws CQLException {

		String cql = "INSERT INTO test (age,year,day) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		tokenCQL.replaceAll(TokenType.FIELD_NAME,
				t -> t.setContent("'" + t.getContent() + "'"));

		Assert.assertEquals(
				"INSERT INTO test ('age','year','day') VALUES (?,?,?)",
				tokenCQL.getContent());

		cql = "SELECT 1,2,3,4,5,6,7,8 FROM TEST WHERE 9=0 ";

		tokenCQL = lexicalParser.isCQL(cql);

		tokenCQL.replaceAll(TokenType.LITERAL,
				t -> t.setContent("'" + t.getContent() + "'"));

		Assert.assertEquals(
				"SELECT '1','2','3','4','5','6','7','8' FROM TEST WHERE '9'='0' ",
				tokenCQL.getContent());

	}

	@Test
	public void injectInteger() throws CQLException {
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
		tokenCQL.replace(TokenType.INJECT, 2016, 1);
		tokenCQL.replace(TokenType.INJECT, 5, 2);

		Assert.assertEquals(
				"INSERT INTO test (age,year,day) VALUES (33,2016,5)",
				tokenCQL.getContent());
	}

	@Test
	public void wrapper() throws CQLException {
		String cql = "INSERT INTO test (key,name,lastname) VALUES ('ABC','Tom','Muller')";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		tokenCQL.replace(TokenType.TABLE_NAME, "AtestA", 0);

		Assert.assertEquals(
				"INSERT INTO 'AtestA' (key,name,lastname) VALUES ('ABC','Tom','Muller')",
				tokenCQL.getContent());

		cql = "INSERT INTO test (key,name,lastname) VALUES ('ABC','Tom','Muller')";

		tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		tokenCQL.replace(TokenType.TABLE_NAME, t -> {
			t.setContent("\"" + t.getContent() + "\"");
		}, 0);

		Assert.assertEquals(
				"INSERT INTO \"test\" (key,name,lastname) VALUES ('ABC','Tom','Muller')",
				tokenCQL.getContent());

		cql = "INSERT INTO test as testeA (key,name,lastname) VALUES ('ABC','Tom','Muller')";

		tokenCQL = lexicalParser.isCQL(cql);

		// replace ever last
		// last (day)
		tokenCQL.replace(TokenType.TABLE_NAME, t -> {
			t.setContent("\"" + t.getContent() + "\"");
		}, 0);

		Assert.assertEquals(
				"INSERT INTO \"test\" as testeA (key,name,lastname) VALUES ('ABC','Tom','Muller')",
				tokenCQL.getContent());

	}

	@Test
	public void injectString() throws CQLException {
		String cql = "INSERT INTO test (key,name,lastname) VALUES (?,?,?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		// replace ever first
		tokenCQL.replace(TokenType.INJECT, "ABC", 0);
		tokenCQL.replace(TokenType.INJECT, "Tom", 1);
		tokenCQL.replace(TokenType.INJECT, "Muller", 2);

		Assert.assertEquals(
				"INSERT INTO test (key,name,lastname) VALUES ('ABC','Tom','Muller')",
				tokenCQL.getContent());
	}

	@Test
	public void injectDate() throws CQLException, ParseException {
		String cql = "INSERT INTO test (key,name,create) VALUES (1,'ABC',?)";

		Token tokenCQL = lexicalParser.isCQL(cql);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

		sdf.setTimeZone(TimeZone.getTimeZone("GMT-08"));

		Date create = sdf.parse("2016-06-23 00:00:00+0300");

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(create);
		// replace ever first
		tokenCQL.replace(TokenType.INJECT, calendar.getTime(), 0);

		Assert.assertEquals(
				"INSERT INTO test (key,name,create) VALUES (1,'ABC','2016-06-22 21:00:00+0000')",
				tokenCQL.getContent());
	}
}
