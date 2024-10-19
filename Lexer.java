import java.util.*;

class Lexer {
	String source;
	int current;
	int previous;

	static boolean isWhitespace(char c){
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}

	static boolean isNum(char c){
		return c >= '0' && c <= '9';
	}

	static boolean isAlpha(char c){
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	};

	boolean done(){
		return current >= source.length();
	}

	char advance(){
		if(done()){
			return 0;
		}
		this.current += 1;
		return source.charAt(current - 1);
	}

	boolean advanceMatching(char c){
		if(source.charAt(current) == c){
			advance();
			return true;
		}
		return false;
	}

	char peek(int delta){
		int pos = current + delta;
		if(pos < 0 || pos >= source.length()){
			return 0;
		}
		return source.charAt(current + delta);
	}

	Token next(){
		char c = advance();
		// System.out.print(c);
		if(c == 0){
			return new Token(TokenType.EOF);
		}

		switch(c){
			case ':': return new Token(TokenType.COLON);
			case ';': return new Token(TokenType.SEMICOLON);
			case ',': return new Token(TokenType.COMMA);
			case '.': return new Token(TokenType.DOT);

			case '+': return new Token(TokenType.PLUS);
			case '-': return new Token(TokenType.MINUS);
			case '*': return new Token(TokenType.STAR);
			case '%': return new Token(TokenType.MODULO);
			case '/':
			if(advanceMatching('/')){
				return tokenizeLineComment();
			} else {
				return new Token(TokenType.SLASH);
			}

			case '>':
			if(advanceMatching('>')){
				return new Token(TokenType.BIT_SH_RIGHT);
			} else if(advanceMatching('=')){
				return new Token(TokenType.GT_EQ);
			} else {
				return new Token(TokenType.GT);
			}

			case '<':
			if(advanceMatching('<')){
				return new Token(TokenType.BIT_SH_LEFT);
			} else if(advanceMatching('=')){
				return new Token(TokenType.LT_EQ);
			} else {
				return new Token(TokenType.LT);
			}

			case '=':
			if(advanceMatching('=')){
				return new Token(TokenType.EQ);
			} else {
				return new Token(TokenType.ASSIGN);
			}

			case '!':
			if(advanceMatching('=')){
				return new Token(TokenType.NEQ);
			} else {
				return new Token(TokenType.LOGIC_NOT);
			}

			case '~': return new Token(TokenType.BIT_NOT);
			case '&':
			if(advanceMatching('&')){
				return new Token(TokenType.LOGIC_AND);
			} else {
				return new Token(TokenType.BIT_AND);
			}

			case '|':
			if(advanceMatching('|')){
				return new Token(TokenType.LOGIC_OR);
			} else {
				return new Token(TokenType.BIT_OR);
			}

			default: {
				if(isWhitespace(c)){
					return new Token(TokenType.WS);
				}
				else if (isNum(c)){
					// current -= 1;
					return tokenizeNumber();
				}
				else if(isAlpha(c) || c == '_') {
					// current -= 1;
					return tokenizeIdentifier();
				}
			}
		}

		return new Token(TokenType.UNKNOWN);
	}

	static List<Token> tokenize(String source, boolean stripComments){
		var lex = new Lexer(source);
		var tokens = new ArrayList<Token>();

		while(true){
			var tk = lex.next();
			if(tk.type == TokenType.WS){
				continue;
			}
			if(tk.type == TokenType.COMMENT && stripComments){
				continue;
			}

			tokens.add(tk);

			if(tk.type == TokenType.EOF){
				break;
			}
		}

		return tokens;
	}

	Token tokenizeLineComment(){
		previous = current;
		while(!done()){
			char c = advance();
			if(c == '\n'){
				break;
			}
		}

		var lexeme = source.substring(previous, current);
		return new Token(TokenType.COMMENT, lexeme);
	}

	Token tokenizeNumber(){
		return new Token(TokenType.UNKNOWN);
	}

	Token tokenizeIdentifier(){
		return new Token(TokenType.UNKNOWN);
	}

	Lexer(String source){
		this.source = source;
	}

}
