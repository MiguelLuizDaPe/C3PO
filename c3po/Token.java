package c3po;

class Token {
	String lexeme;
	TokenType type;

	// Literal values
	float realValue;
	int intValue;
	String stringValue;
	char charValue;

	public String toString(){
		if(hasNonTrivialLexeme()){
			if(type == TokenType.INTEGER){
				return String.format("%s(%s, %d)", type.value, lexeme, intValue);
			} else if(type == TokenType.FLOAT){
				return String.format("%s(%s, %f)", type.value, lexeme, realValue);
			} else if(type == TokenType.CHAR){
				return String.format("%s(%s, %c)", type.value, lexeme, charValue);
			} else if(type == TokenType.STRING){
				return String.format("%s(%s, %s)", type.value, lexeme, stringValue);
			} else {
				return String.format("%s(%s)", type.value, lexeme);
			}
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

	boolean isPrimary(){
		return type == TokenType.ID
			|| type == TokenType.STRING
			|| type == TokenType.INTEGER
			|| type == TokenType.FLOAT
			|| type == TokenType.CHAR
			|| type == TokenType.TRUE
			|| type == TokenType.FALSE;
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

	Token(TokenType t, String s, float v){
		type = t;
		lexeme = s;
		realValue = v;
	}

	Token(TokenType t, String s, int v){
		type = t;
		lexeme = s;
		intValue = v;
	}

	Token(TokenType t, String s, String v){
		type = t;
		lexeme = s;
		stringValue = v;
	}

	Token(TokenType t, String s, char v){
		type = t;
		lexeme = s;
		charValue = v;
	}
}
