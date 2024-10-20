import java.util.*;

class Parser {
	int current;
	int previous;
	Token[] tokens;

	boolean done(){
		return current >= tokens.length;
	}

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

	Token advanceExpected(TokenType t) throws LanguageException {
		var tk = advance();
		if(tk.type != t){
			var msg = String.format("Expected %s, got %s", t.value, tk.type.value);
			throw new LanguageException(CompilerStage.PARSER, msg);
		}
		return tk;
	}

	boolean advanceMatching(TokenType t){
		var tk = advance();
		if(tk.type == t){
			return true;
		}
		current -= 1;
		return false;
	}

	IfStmt parseIf() throws LanguageException {
		advanceExpected(TokenType.IF);
		advanceExpected(TokenType.PAREN_OPEN);
		var cond = parseExpression();
		advanceExpected(TokenType.PAREN_CLOSE);
		var body = parseScope();

		/* TODO: Else */
		return new IfStmt(cond, body);
	}

	WhileStmt parseWhile() throws LanguageException {
		advanceExpected(TokenType.WHILE);
		advanceExpected(TokenType.PAREN_OPEN);
		var cond = parseExpression();
		advanceExpected(TokenType.PAREN_CLOSE);
		var body = parseScope();
		return new WhileStmt(cond, body);
	}

	InlineStmt parseInlineStatement() throws LanguageException {
		var lookahead = peek(0);

		InlineStmt statement = null;

		if(lookahead.type == TokenType.CONTINUE){
			statement = new Continue();
		}

		if(lookahead.type == TokenType.BREAK){
			statement = new Break();
		}

		if(lookahead.type == TokenType.RETURN){
			var res = parseExpression();
			statement = new Return(res);
		}

		advanceExpected(TokenType.SEMICOLON);
		return statement;
	}

	Scope parseScope() throws LanguageException {
		var lookahead = peek(0);
		var statements = new ArrayList<Statement>();

		while(!done()){
			if(advanceMatching(TokenType.CURLY_CLOSE)){
				break;
			}
			if(peek(0).type == TokenType.EOF){
				LanguageException.parserError("Unclosed Scope");
			}

			// If
			if(lookahead.type == TokenType.IF){
				statements.add(parseIf());
				continue;
			}

			// While
			if(lookahead.type == TokenType.WHILE){
				statements.add(parseWhile());
				continue;
			}

			// FuncDef
			if(lookahead.type == TokenType.FN){
				statements.add(parseFn());
				continue;
			}

			// InlineStmt
			{
				assert(false);
			}

		}

		return new Scope(statements.toArray(new Statement[statements.size()]));
	}


	TypeExpr parseType() throws LanguageException {
		var typeName = advanceExpected(TokenType.ID);
		var modifiers = new ArrayList<Modifier>();
		// Foo * [INT]
		while(!done()){
			// Pointer
			if(advanceMatching(TokenType.STAR)){
				modifiers.add(Modifier.pointer());
				continue;
			}
			// Array
			if(advanceMatching(TokenType.SQUARE_OPEN)){
				var num = advanceExpected(TokenType.INTEGER);
				advanceExpected(TokenType.SQUARE_CLOSE);
				modifiers.add(Modifier.array((int)num.intValue));
				continue;
			}
			break;
		}

		var mods = modifiers.toArray(new Modifier[modifiers.size()]);
		return new TypeExpr(typeName.lexeme, mods);
	}

	// FuncDef.ParameterList parseParameters() throws LanguageException {
	// 	advanceExpected(TokenType.PAREN_OPEN);
	// 	while(!done()){
	// 	}
	// 	advanceExpected(TokenType.PAREN_CLOSE);
	// }

	FuncDef parseFn() throws LanguageException {
		// fn T ID(...) {
		advanceExpected(TokenType.FN);
		var type = parseType();
		var name = advanceExpected(TokenType.ID);
		return null;
	}

	Expression parseExpression() throws LanguageException {
		return parsePratt(0);
	}

	Expression[] parseExpressionList(TokenType close) throws LanguageException {
		var exprs = new ArrayList<Expression>();

		if(peek(0).type != close){
			var first = parseExpression();
			exprs.add(first);

			while(true){
				if(peek(0).type == close){
					break;
				}
				if(peek(0).type == TokenType.EOF){
					LanguageException.parserError("Unclosed expression list");
				}

				advanceExpected(TokenType.COMMA);

				var e = parseExpression();
				exprs.add(e);
			}
		}

		advanceExpected(close);

		return exprs.toArray(new Expression[exprs.size()]);
	}

	// Uses pratt parsing to quickly parse binary, unary and indexing expressions
	Expression parsePratt(int minBp) throws LanguageException {
		var token = advance();
		Expression left;
		if(token.isPrimary()){
			left = new PrimaryExpr(token);
		}
		else if (token.type == TokenType.PAREN_OPEN){
			left = parsePratt(0);
			advanceExpected(TokenType.PAREN_CLOSE);
		}
		else {
			var power = Operators.prefixPower(token.type);
			if(power == null){
				LanguageException.parserError("Not a prefix operator");
			}
			var right = parsePratt(power.rbp());
			left = new UnaryExpr(token.type, right);
		}

		while(true){
			var op = peek(0);
			if(op.type == TokenType.EOF){ break; }

			var postPower = Operators.postfixPower(op.type);
			if(postPower != null){
				if(postPower.lbp() < minBp){
					break;
				}
				advance();

				if(op.type == TokenType.SQUARE_OPEN){
					var index = parsePratt(0);
					advanceExpected(TokenType.SQUARE_CLOSE);
					left = new IndexExpr(left, index);
				}
				else if(op.type == TokenType.PAREN_OPEN){
					var args = parseExpressionList(TokenType.PAREN_CLOSE);
					left = new CallExpr(left, args);
				}
				else {
					left = new UnaryExpr(op.type, left);
				}
				continue;
			}

			var inPower = Operators.infixPower(op.type);
			if(inPower != null){
				if(inPower.lbp() < minBp){
					break;
				}
				advance();

				var right = parsePratt(inPower.rbp());
				left = new BinaryExpr(left, op.type, right);
				continue;
			}

			break; // Nothing more to parse, OR not an operator.
		}

		return left;
	}

	Parser(Token[] tokens){
		this.tokens = tokens;
	}

	static Expression parse(Token[] tokens) throws LanguageException {
		var parser = new Parser(tokens);
		var expr = parser.parseType();
		System.out.println(expr.toString());
		// return expr;
		return null;
	}
}
