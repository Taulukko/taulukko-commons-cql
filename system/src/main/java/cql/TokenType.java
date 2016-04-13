package cql;

/**
 * 
 * Ler BNF.txt
 * */
public enum TokenType {

	COMMAND("COMMAND"), CQL("CQL"), ITEMNAME("ITEMNAME"), ITEM_NAME_CASE_SENSITIVE(
			"ITEM NAME CASE SENSITIVE"), ITEM_NAME_CASE_INSENSITIVE(
			"ITEM NAME CASE INSENSITIVE"), RESERVED_WORDS("RESERVED WORDS"), RESERVED_WORD(
			"RESERVED WORD"), SELECTOR_ITEM("SELECTOR ITEM"), CHARS("CHARS"), ACESSOR(
			"ACESSOR"), LITERAL("LITERAL"), STRING("STRING"), NUMBER("NUMBER"), CONDITION(
			"CONDITION"), WHERE("WHERE"), CONDITIONS("CONDITIONS"), CONDITION_ITEM(
			"CONDITION ITEM"), JOIN_CONDITION("JOIN CONDITION"), AND("AND"), OR(
			"OR"), SPACES("SPACES"), INPUT_CHARACTER("INPUT CHARACTER"), HEX(
			"HEX"), SYMBOL("SYMBOL"), INJECT("INJECT"), ASTERISK("ASTERISK"), FUNCTION(
			"FUNCTION"), START_PARAMETERS("START PARAMETERS"), END_PARAMETERS(
			"END PARAMETERS"), SINGLE_QUOTED("SINGLE QUOTED"), SELECTOR_BLOCK(
			"SELECTOR BLOCK"), DOUBLE_QUOTED("DOUBLE QUOTED"), INPUT_CHARACTER_EXCEPT_SINGLE(
			"INPUT CHARACTER EXCEPT SINGLE"), INPUT_CHARACTER_EXCEPT_DOUBLE(
			"INPUT CHARACTER EXCEPT DOUBLE"), INSERT_COMMAND("INSERT COMMAND"), OTHER_COMMAND(
			"OTHER COMMAND"), INSERT("INSERT"), VALUES("VALUES"), INTO("INTO"), START_ARRAY(
			"START ARRAY"), END_ARRAY("END ARRAY"), END_MAP("END MAP"), ARRAY(
			"ARRAY"), MAP("MAP"), START_MAP("START MAP"), PROPERTIES(
			"PROPERTIES"), PROPERTY("PROPERTY"), KEY("KEY"), DOUBLE_DOT(
			"DOUBLE DOT"), COMMA("COMMA"), AS("AS"), ALIAS("ALIAS"), FIELDNAME(
			"FIELD NAME"), DIGIT("DIGIT"), END_CREATE_TABLE("END CREATE TABLE"), DOT_COMMA(
			"DOT COMMA"), START_CREATE_TABLE("START CREATE TABLE"), CREATE(
			"CREATE"), TABLE("TABLE"), CREATE_TABLE_COMMAND("CREATE TABLE COMMAND");

	private String name = null;

	TokenType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
