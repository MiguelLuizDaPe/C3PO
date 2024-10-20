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
		if(peek(0).type == TokenType.PAREN_CLOSE){
			LanguageException.parserError("If with an empty condition is not allowed.");
		}

		var cond = parseExpression();
		advanceExpected(TokenType.PAREN_CLOSE);

		var body = parseScope();
		Statement elseBranch = null;

		if(advanceMatching(TokenType.ELSE)){
			if(peek(0).type == TokenType.IF){
				elseBranch = parseIf();
			}
			else {
				elseBranch = parseScope();
			}
		}

		return new IfStmt(cond, body, elseBranch);
	}

	ForStmt parseFor() throws LanguageException{
		advanceExpected(TokenType.FOR);
		advanceExpected(TokenType.PAREN_OPEN);
		var fir = parseInlineStatement();
		var cond = parseExpression();
		advanceExpected(TokenType.SEMICOLON);
		var aft = parseAssignment();
		advanceExpected(TokenType.PAREN_CLOSE);
		var body = parseScope();
		return new ForStmt(fir, cond, aft, body);
	}

	DoStmt parseDo() throws LanguageException{
		System.out.println("entrou");
		advanceExpected(TokenType.DO);
		System.out.println("passou");
		var body = parseScope();
		advanceExpected(TokenType.WHILE);
		advanceExpected(TokenType.PAREN_OPEN);
		var cond = parseExpression();
		advanceExpected(TokenType.PAREN_CLOSE);
		advanceExpected(TokenType.SEMICOLON);
		return new DoStmt(cond, body);
	}

	WhileStmt parseWhile() throws LanguageException {
		advanceExpected(TokenType.WHILE);
		advanceExpected(TokenType.PAREN_OPEN);
		var cond = parseExpression();
		advanceExpected(TokenType.PAREN_CLOSE);
		var body = parseScope();
		return new WhileStmt(cond, body);
	}

	 private static final int DECL = 0;
	 private static final int ASS = 1;
	 private static final int EXPR = 2;

	// Try to use left side as type
	// | Type is ill formed
	// 		| followed by = -> Assignment
	// 		| _ -> ExprStmt.
	// | Type is well formed
	//    | Followed by ID -> Declaration.
	//    | Not followed? It's an expression now.
	//    	| Followed by = -> Assignment.
	//    	| _ -> ExprStmt.
	int disambiguateDeclarationOrAssignOrExprStatement() throws LanguageException {
		int rewindPoint = current;

		TypeExpr type = null;
		try {
			type = parseType();
		} catch(LanguageException e){
			var left = parseExpression();
			if(advanceMatching(TokenType.ASSIGN)){
				current = rewindPoint;
				return ASS;
			} else {
				current = rewindPoint;
				return EXPR;
			}
		}

		if(advanceMatching(TokenType.ID)){
			current = rewindPoint;
			return DECL;
		} else {
			current = rewindPoint;
			var left = parseExpression();

			if(advanceMatching(TokenType.ASSIGN)){
				current = rewindPoint;
				return ASS;
			} else {
				current = rewindPoint;
				return EXPR;
			}
		}
	}

	VarAssign parseAssignment() throws LanguageException {
		var left = parseExpression();
		advanceExpected(TokenType.ASSIGN);
		var right = parseExpression();
		return new VarAssign(left, right);
	}

	VarDecl parseVarDecl() throws LanguageException {
		var type = parseType();
		var identifiers = new ArrayList<String>();
		var expressions = new ArrayList<Expression>();

		// int
		// 	x = 1,
		// 	y = 2,
		// 	z = 10,
		// ;
		// Type (ID (= Expr)?, )+
		while(!done()){
			if(peek(0).type == TokenType.SEMICOLON){
				break;
			}
			if(advanceMatching(TokenType.EOF)){
				LanguageException.parserError("Unterminated Declaration");
			}

			identifiers.add(advanceExpected(TokenType.ID).lexeme);
			if(advanceMatching(TokenType.ASSIGN)){
				var left = parseExpression();
				expressions.add(left);
			}
			else {
				expressions.add(null);
			}

			if(advanceMatching(TokenType.COMMA)){
				continue;
			} else {
				break;
			}
		}

		// advanceExpected(TokenType.SEMICOLON);

		var ids = identifiers.toArray(new String[identifiers.size()]);
		var exprs = expressions.toArray(new Expression[expressions.size()]);
		return new VarDecl(type, ids, exprs);
	}

	InlineStmt parseInlineStatement() throws LanguageException {
		var lookahead = peek(0);

		InlineStmt statement = null;

		if(advanceMatching(TokenType.CONTINUE)){
			statement = new Continue();
		}
		else if(advanceMatching(TokenType.BREAK)){
			statement = new Break();
		}
		else if(advanceMatching(TokenType.RETURN)){
			var res = parseExpression();
			statement = new Return(res);
		}
		else {
			int whatNext = disambiguateDeclarationOrAssignOrExprStatement();
			if(whatNext == ASS){
				statement = parseAssignment();
			}
			else if(whatNext == EXPR){
				var expr = parseExpression();
				statement = new ExprStmt(expr);
			}
			else if(whatNext == DECL){
				statement = parseVarDecl();
			}
			else {
				// NOTE: Should never happen.
				LanguageException.parserError("Encountered unresolved ambiguity");
			}
		}

		advanceExpected(TokenType.SEMICOLON);
		return statement;
	}

	Scope parseScope() throws LanguageException {
		advanceExpected(TokenType.CURLY_OPEN);

		var statements = new ArrayList<Statement>();

		while(!done()){
			var lookahead = peek(0);
			// System.out.println("Parsing scope: " + peek(-1).type.value + " -> " + peek(0).type.value + " -> " + peek(1).type.value);

			if(advanceMatching(TokenType.CURLY_CLOSE)){
				break;
			}
			if(peek(0).type == TokenType.EOF){
				LanguageException.parserError("Unclosed Scope");
			}

			// Subscope
			if(lookahead.type == TokenType.CURLY_OPEN){
				statements.add(parseScope());
				continue;
			}

			// If
			if(lookahead.type == TokenType.IF){
				var stmt = parseIf();
				statements.add(stmt);
				continue;
			}

			//For
			if(lookahead.type == TokenType.FOR){
				statements.add(parseFor());
				continue;
			}

			//Do
			if(lookahead.type == TokenType.DO){
				statements.add(parseDo());
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
				statements.add(parseInlineStatement());
				System.out.println("----");
				continue;
			}
		}

		return new Scope(statements.toArray(new Statement[statements.size()]));
	}

	TypeExpr parseType() throws LanguageException {
		var typeName = advanceExpected(TokenType.ID);
		var modifiers = new ArrayList<Modifier>();
		while(!done()){
			// Pointer
			if(advanceMatching(TokenType.CARET)){
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

	FuncDef.ParameterList parseParameters() throws LanguageException {
		advanceExpected(TokenType.PAREN_OPEN);
		var types = new ArrayList<TypeExpr>();
		var identifiers = new ArrayList<String>();

		if(peek(0).type != TokenType.PAREN_CLOSE){
			// Parse first arg
			types.add(parseType());
			identifiers.add(advanceExpected(TokenType.ID).lexeme);

			while(!done()){
				if(peek(0).type == TokenType.PAREN_CLOSE){
					break;
				}
				if(peek(0).type == TokenType.EOF){
					LanguageException.parserError("Unclosed parameter list");
				}

				advanceExpected(TokenType.COMMA);
				types.add(parseType());
				identifiers.add(advanceExpected(TokenType.ID).lexeme);
			}
		}

		advanceExpected(TokenType.PAREN_CLOSE);

		var paramTypes = types.toArray(new TypeExpr[types.size()]);
		var paramIds = identifiers.toArray(new String[identifiers.size()]);

		return new FuncDef.ParameterList(paramTypes, paramIds);
	}

	FuncDef parseFn() throws LanguageException {
		advanceExpected(TokenType.FN);
		var type = parseType();
		var name = advanceExpected(TokenType.ID);
		var arguments = parseParameters();
		var body = parseScope();

		return new FuncDef(name.lexeme, arguments, type, body);
	}

	Expression parseExpression() throws LanguageException {
		return parsePratt(0);
	}

	Expression[] parseExpressionList(TokenType close) throws LanguageException {
		var exprs = new ArrayList<Expression>();

		if(peek(0).type != close){
			var first = parseExpression();
			exprs.add(first);

			while(!done()){
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
				LanguageException.parserError("Not a prefix operator " + token.type.value);
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

	static Statement parse(Token[] tokens) throws LanguageException {
		var parser = new Parser(tokens);
		var expr = parser.parseScope();
		return expr;
	}
}
