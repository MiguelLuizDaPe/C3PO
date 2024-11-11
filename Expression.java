sealed interface Expression extends IREmmiter permits BinaryExpr, UnaryExpr, PrimaryExpr, IndexExpr, CallExpr  {
	public Type evalType(Scope context) throws LanguageException;
}

final class IndexExpr implements Expression {
	Expression array;
	Expression index;

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		throw new UnsupportedOperationException("TODO");
	}

	public String toString(){
		return String.format("([] %s %s)", array.toString(), index.toString());
	}

	public Type evalType(Scope context) throws LanguageException{// o index precisa ser um int sem qualificadores, o array/idexado precisa ter ARRAY como primeiro qualificador, o tipo resultado é o tipo do array sem o primeiro qualificador
		var indexType = index.evalType(context);
		if(!indexType.equals(new Type(PrimitiveType.INT, null))){
			LanguageException.checkerError("Index type not allowed");
		}

		var arrayType = array.evalType(context);
		Qualifier[] qualsCopy = new Qualifier[arrayType.quals.length - 1];

		for(int i = 0; i < qualsCopy.length; i++){
			qualsCopy[i] = arrayType.quals[i];
		}

		// System.arraycopy(arrayType.quals, 0, qualsCopy, 0, arrayType.quals.length);

		// System.out.println(qualsCopy[0]);

		var typeOut = new Type(arrayType.primitive, qualsCopy);
		return typeOut;
	}

	IndexExpr(Expression array, Expression index){
		this.array = array;
		this.index = index;
	}
}

final class CallExpr implements Expression {
	Expression callable;
	Expression[] arguments;

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		throw new UnsupportedOperationException("TODO");
	}

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

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		if(operator == TokenType.PLUS){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.ADD));
		}
		else if(operator == TokenType.MINUS){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.SUB));
		}
		else if(operator == TokenType.STAR){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.MUL));
		}
		else if(operator == TokenType.SLASH){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.DIV));
		}
		else if(operator == TokenType.BIT_AND){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_AND));
		}
		else if(operator == TokenType.BIT_OR){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_OR));
		}
		else if(operator == TokenType.BIT_SH_LEFT){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_SH_LEFT));
		}
		else if(operator == TokenType.BIT_SH_RIGHT){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_SH_RIGHT));
		}
		else if(operator == TokenType.TILDE){
			left.genIR(context, builder);
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_XOR));
		}
		else{
			throw new UnsupportedOperationException("NO");
		}

	}

	public Type evalType(Scope context) throws LanguageException{
		var leftType = left.evalType(context);
		var rightType = right.evalType(context);

		var compat = Operators.binaryCompatible(operator, leftType.primitive);

		if(leftType.equals(rightType) && compat && leftType.quals.length == 0 && rightType.quals.length == 0){
			if(Operators.isComparison(operator)){
				return new Type(PrimitiveType.BOOL, null);
			}
			else {
				return leftType;
			}
		}
		else {
			LanguageException.checkerError("Cannot apply operator %s to arguments of types: %s and %s", operator.value, leftType, rightType);
			return null;
		}
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

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		if(operator == TokenType.PLUS){
			operand.genIR(context, builder);
		}
		else if(operator == TokenType.MINUS){
			operand.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.NEG));
		}
		else if(operator == TokenType.LOGIC_NOT){
			operand.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.LOGIC_NOT));
		}
		else if(operator == TokenType.TILDE){
			operand.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.BIT_NOT));
		}
		else{
			LanguageException.emitterError("Not possible");
		}
	}

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

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		// throw new UnsupportedOperationException("TODO");
		if(token.type == TokenType.INTEGER){
			builder.addInstruction(new Instruction(OpCode.PUSH, token.intValue));
		}
		else if(token.type == TokenType.FLOAT){
			throw new UnsupportedOperationException("TODO");
		}
		else if(token.type == TokenType.STRING){
			throw new UnsupportedOperationException("TODO");
		}
		else if(token.type == TokenType.CHAR){
			throw new UnsupportedOperationException("TODO");
		}
		else if(token.type == TokenType.TRUE || token.type == TokenType.FALSE){
			builder.addInstruction(new Instruction(OpCode.PUSH, token.type == TokenType.TRUE ? 1 : 0));
		}
		else if(token.type == TokenType.ID){
			throw new UnsupportedOperationException("TODO");
		}
	}

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
			if(info == null){
				LanguageException.checkerError("Symbol not found: %s", token.lexeme);
			}
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



