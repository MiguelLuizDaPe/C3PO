class Parser {
	int current;
	int previous;
	Token[] tokens;

	Token advance(){
		if(current >= tokens.length){
			return new Token(TokenType.EOF);
		}
		current += 1;
		return tokens[current - 1];
	}

	Token peek(int delta){
		int pos = current + delta;
		if(pos < 0 || pos >= tokens.length){
			return new Token(TokenType.EOF);
		}
		return tokens[pos];
	}

	PrimaryExpr parsePrimary() throws LanguageException {
		var token = advance();
		if(!token.isPrimary()){
			throw new LanguageException(CompilerStage.PARSER, "Not a valid primary expression");
		}

		return new PrimaryExpr(token);
	}

	Parser(Token[] tokens){
		this.tokens = tokens;
	}

	static Expression parse(Token[] tokens) throws LanguageException {
		var parser = new Parser(tokens);
		var expr = parser.parsePrimary();
		return expr;
	}
}
