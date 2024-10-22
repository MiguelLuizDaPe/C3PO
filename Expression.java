sealed interface Expression permits BinaryExpr, UnaryExpr, PrimaryExpr, IndexExpr, CallExpr {
	public String toString();
	public Type evalType(Scope context) throws LanguageException;
}

final class IndexExpr implements Expression {
	Expression array;
	Expression index;

	public String toString(){
		return String.format("([] %s %s)", array.toString(), index.toString());
	}

	public Type evalType(Scope context) throws LanguageException{
		Debug.unimplemented();return null;
	}

	IndexExpr(Expression array, Expression index){
		this.array = array;
		this.index = index;
	}
}

final class CallExpr implements Expression {
	Expression callable;
	Expression[] arguments;

	public String toString(){
		var builder = new StringBuilder();
		builder.append(String.format("(call %s ", callable.toString()));
		for(var arg : arguments){
			builder.append(arg.toString());
			builder.append(" ");
		}
		builder.setLength(builder.length() - 1);
		builder.append(")");
		return builder.toString();
	}

	public Type evalType(Scope context) throws LanguageException{
		Debug.unimplemented();return null;
	}

	CallExpr(Expression callable, Expression[] arguments){
		this.callable = callable;
		this.arguments = arguments;
	}
}

final class BinaryExpr implements Expression {
	TokenType operator;
	Expression left;
	Expression right;

	public Type evalType(Scope context) throws LanguageException{
		Debug.unimplemented();return null;
	}

	public String toString(){
		return String.format("(%s %s %s)", operator.value, left.toString(), right.toString());
	}

	BinaryExpr(Expression left, TokenType operator, Expression right){
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
}

final class UnaryExpr implements Expression {
	TokenType operator;
	Expression operand;

	public Type evalType(Scope context) throws LanguageException{
		var operandType = operand.evalType(context);
		if(operandType.quals.length != 0){
			LanguageException.checkerError("Cannot apply operator to aggregate or indirect type: " + operandType.toString());
		}
		if(!Operators.unaryCompatible(operator, operandType.primitive)){
			LanguageException.checkerError(String.format("Incompatible type '%s' for operator '%s'", operandType.toString(), operator.value));
		}
		return operandType;
	}

	public String toString(){
		return String.format("(%s %s)", operator.value, operand.toString());
	}

	UnaryExpr(TokenType operator, Expression operand){
		this.operator = operator;
		this.operand = operand;
	}
}

final class PrimaryExpr implements Expression {
	Token token;

	public Type evalType(Scope context) throws LanguageException {
		if(token.type == TokenType.INTEGER){
			return new Type(PrimitiveType.INT, null);
		}
		else if(token.type == TokenType.FLOAT){
			return new Type(PrimitiveType.FLOAT, null);
		}
		else if(token.type == TokenType.STRING){
			return new Type(PrimitiveType.STRING, null);
		}
		else if(token.type == TokenType.CHAR){
			return new Type(PrimitiveType.CHAR, null);
		}
		else if(token.type == TokenType.TRUE || token.type == TokenType.FALSE){
			return new Type(PrimitiveType.BOOL, null);
		}
		else if(token.type == TokenType.ID){
			var info = context.searchSymbol(token.lexeme);
			return info.type;
		}

		LanguageException.checkerError("Not a primary expression???");
		return null;
	}

	public String toString(){
		if(token.type == TokenType.INTEGER){
			return String.format("%d", token.intValue);
		} else if(token.type == TokenType.FLOAT){
			return String.format("%f", token.realValue);
		} else {
			return token.lexeme;
		}
	}

	PrimaryExpr(Token tk){
		token = tk;
	}
}



