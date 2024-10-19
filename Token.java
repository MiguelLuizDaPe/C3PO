class Token {
	String lexeme;
	TokenType type;

	public String toString(){
		if(hasNonTrivialLexeme()){
			String.format("%s(%s)", type.value, lexeme);
		}
		return type.value;
	}

	boolean hasNonTrivialLexeme(){
		return type == TokenType.ID
			|| type == TokenType.STRING
			|| type == TokenType.INTEGER
			|| type == TokenType.FLOAT
			|| type == TokenType.CHAR
			|| type == TokenType.COMMENT;
	}

	Token(){
		type = TokenType.UNKNOWN;
		lexeme = TokenType.UNKNOWN.value;
	}

	Token(TokenType t){
		type = t;
	}

	Token(TokenType t, String s){
		type = t;
		lexeme = s;
	}
}
