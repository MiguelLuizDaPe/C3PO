import java.util.*;

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

	void advanceExpected(TokenType t) throws LanguageException {
		var tk = advance();
		if(tk.type != t){
			throw new LanguageException(CompilerStage.PARSER, "Did not get expected token");
		}
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
		var expr = parser.parseExpression();
		return expr;
	}
}
