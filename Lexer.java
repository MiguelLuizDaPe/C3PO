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

	Token next() throws LanguageException {
		char c = advance();
		// System.out.print(c);
		if(c == 0){
			return new Token(TokenType.EOF);
		}

		switch(c){
			case ':': return new Token(TokenType.COLON);
			case ';': return new Token(TokenType.SEMICOLON);
			case ',': return new Token(TokenType.COMMA);
			case '^': return new Token(TokenType.CARET);
			case '.': return new Token(TokenType.DOT);
			
			case '(': return new Token(TokenType.PAREN_OPEN);
			case ')': return new Token(TokenType.PAREN_CLOSE);
			case '[': return new Token(TokenType.SQUARE_OPEN);
			case ']': return new Token(TokenType.SQUARE_CLOSE);
			case '{': return new Token(TokenType.CURLY_OPEN);
			case '}': return new Token(TokenType.CURLY_CLOSE);

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

			case '~': return new Token(TokenType.TILDE);
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
					current -= 1;
					return tokenizeNumber();
				}
				else if (c == '"'){
					return tokenizeString();
				}
				else if (c == '\''){
					return tokenizeChar();
				}
				else if(isAlpha(c) || c == '_') {
					current -= 1;
					return tokenizeIdentifier();
				}
			}
		}

		return new Token(TokenType.UNKNOWN);
	}

	static List<Token> tokenize(String source, boolean stripComments) throws LanguageException {
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
			if(tk.type == TokenType.UNKNOWN){
				throw new LanguageException(CompilerStage.LEXER, String.format("Unrecognized char"));
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
		previous = current;
		boolean isFloat = false;
		var digits = new StringBuilder();

		while(!done()){
			char c = advance();
			if(isNum(c)){
				digits.append(c);
			}
			else if(c == '.'){
				isFloat = true;
				digits.append(c);
			}
			else if(c == '_'){
				continue;
			}
			else {
				current -= 1;
				break;
			}
		}

		var lexeme = source.substring(previous, current);
		var numText = digits.toString();

		if(isFloat){
			float val = Float.parseFloat(numText);
			return new Token(TokenType.FLOAT, lexeme, val);
		} else {
			int val = Integer.parseInt(numText);
			return new Token(TokenType.INTEGER, lexeme, val);
		}
	}

	Token tokenizeString() throws LanguageException {
		previous = current;

		var chars = new StringBuilder();

		while(!done()){
			char c = advance();

			if(c == '"'){
				break;
			} else if (c == '\n'){
				throw new LanguageException(CompilerStage.LEXER, "Multi line strings are not allowed.");
			} else if (c == '\\'){
				char esc = escapeSequence(advance());
				if(esc == 0){
					throw new LanguageException(CompilerStage.LEXER, "Invalid escape sequence");
				}
				chars.append(esc);
			} else {
				chars.append(c);
			}
		}

		var lexeme = source.substring(previous - 1, current);
		var value = chars.toString();
		return new Token(TokenType.STRING, lexeme, value);
	}

	static char escapeSequence(char c){
		switch(c){
			case 'n': return '\n';
			case 't': return '\t';
			case 'r': return '\r';
			case '\\': return '\\';
			case '\'': return '\'';
			case '\"': return '\"';
			default: return 0;
		}
	}

	Token tokenizeChar() throws LanguageException {
		previous = current;

		char value = advance();
		if(value == 0){
			throw new LanguageException(CompilerStage.LEXER, "Unterminated char literal");
		}
		if(value == '\\'){
			char next = advance();
			value = escapeSequence(next);
			if(value == 0){
				throw new LanguageException(CompilerStage.LEXER, "Invalid escape sequence");
			}
		}
		if(!advanceMatching('\'')){
			throw new LanguageException(CompilerStage.LEXER, "Char literal is too long");
		}

		var lexeme = source.substring(previous - 1, current);

		return new Token(TokenType.CHAR, lexeme, value);
	}

	Token tokenizeIdentifier(){
		previous = current;
		while(!done()){
			char c = advance();
			if(isAlpha(c) || isNum(c) || c == '_'){
				continue;
			} else {
				current -= 1;
				break;
			}
		}

		var lexeme = source.substring(previous, current);
		var type = TokenType.ID;

		for(var key : TokenType.values()){
			if(lexeme.equals(key.value)){
				type = key;
			}
		}

		return new Token(type, lexeme);
	}

	Lexer(String source){
		this.source = source;
	}
}
