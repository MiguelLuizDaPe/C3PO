sealed class Expression permits BinaryExpr, UnaryExpr, PrimaryExpr, IndexExpr {
	public String toString(){
		return "<Expr>";
	}
}

final class IndexExpr extends Expression {
	Expression array;
	Expression index;

	public String toString(){
		return String.format("([] %s %s)", array.toString(), index.toString());
	}

	IndexExpr(Expression array, Expression index){
		this.array = array;
		this.index = index;
	}
}

// final class CallExpr {
// 	Expression array;
// 	Expression[] index;
//
// 	public String toString(){
// 		return String.format("([] %s %s)", array.toString(), index.toString());
// 	}
//
// 	IndexExpr(Expression array, Expression index){
// 		this.array = array;
// 		this.index = index;
// 	}
// }

final class BinaryExpr extends Expression {
	TokenType operator;
	Expression left;
	Expression right;

	public String toString(){
		return String.format("(%s %s %s)", operator.value, left.toString(), right.toString());
	}

	BinaryExpr(Expression left, TokenType operator, Expression right){
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
}

final class UnaryExpr extends Expression {
	TokenType operator;
	Expression operand;

	public String toString(){
		return String.format("(%s %s)", operator.value, operand.toString());
	}

	UnaryExpr(TokenType operator, Expression operand){
		this.operator = operator;
		this.operand = operand;
	}
}

final class PrimaryExpr extends Expression {
	Token token;

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
