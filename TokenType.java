enum TokenType {
	UNKNOWN("<Unknown token>"),

	ID("Identifier"), STRING("String"), INTEGER("Integer"), FLOAT("Float"), CHAR("Char"), COMMENT("Comment"),

	COMMA(","), COLON(":"), SEMICOLON(";"), DOT("."), ASSIGN("="),

	IF("if"), FOR("for"), WHILE("while"), FN("fn"), CONTINUE("continue"), BREAK("break"), RETURN("return"),
	TRUE("true"), FALSE("false"),

	PAREN_OPEN("("), PAREN_CLOSE(")"),
	SQUARE_OPEN("["), SQUARE_CLOSE("]"),
	CURLY_OPEN("{"), CURLY_CLOSE("]"),

	PLUS("+"), MINUS("-"), STAR("*"), SLASH("/"), MODULO("%"),
	BIT_AND("&"), BIT_OR("|"), BIT_NOT("~"), BIT_SH_LEFT("<<"), BIT_SH_RIGHT(">>"),

	LOGIC_AND("&&"), LOGIC_OR("||"), LOGIC_NOT("!"),
	GT(">"), LT("<"), GT_EQ(">="), LT_EQ("<="), NEQ("!="), EQ("=="),

	EOF("<End of file>"),
	WS("<Space>");

	public String value;
	
	TokenType(String v){
		value = v;
	}
}
