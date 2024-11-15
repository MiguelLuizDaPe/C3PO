final class ExprStmt implements Statement {
	Expression expression;

	public void check(Scope previous) throws LanguageException{
		expression.evalType(previous);
	}

	public String toString(){
		return expression.toString();
	}

	ExprStmt(Expression e){
		assert(e != null);
		this.expression = e;
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		expression.genIR(context, builder);
	}
}

final class VarAssign implements Statement {
	Expression left;
	Expression right;

    public void check(Scope previous) throws LanguageException{
        var leftType = left.evalType(previous);
        var rightType = right.evalType(previous);

		if(!leftType.equals(rightType)){
			throw LanguageException.checkerError("Cannot assign object of type %s with value of type %s", leftType, rightType);
		}

		var isLvalue = (left instanceof PrimaryExpr) || (left instanceof IndexExpr);
		if(!isLvalue){
			throw LanguageException.checkerError("Cannot assign to non L-value object of type %s", leftType);
		}
	}

	public String toString(){
		return String.format("%s <- %s", left.toString(), right.toString());
	}

	VarAssign(Expression left, Expression right){
		assert(left != null && right != null);
		this.left = left;
		this.right = right;
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		if(left instanceof PrimaryExpr left){
			var info = context.searchSymbol(left.token.lexeme);
			var mangledName = builder.addSymbol(left.token.lexeme, info);

			builder.addInstruction(new Instruction(OpCode.PUSH, mangledName));
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.STORE));
		}
		else if (left instanceof IndexExpr){
			left.genIR(context, builder);
			builder.popInstruction();
			right.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.STORE));
		}
		else {
			throw new RuntimeException("Unreachable code");
		}
	}
}

final class VarDecl implements Statement {
	ParserType typeDecl;
	String[] identifiers;
	Expression[] expressions;

	public void check(Scope previous) throws LanguageException{
		var t = Type.fromPrimitiveParserType(this.typeDecl);

		if(t.primitive == PrimitiveType.VOID){
			throw LanguageException.checkerError("Cannot instantiate variable of incomplete type void");
		}

		for(int i = 0; i < this.identifiers.length; i++){
			var id = identifiers[i];
			var initExpr = this.expressions[i];

			var sym = SymbolInfo.variable(t);

			if(initExpr != null){
				sym.init = true;
				var rhsType = initExpr.evalType(previous);
				if(!rhsType.equals(t)){
					throw LanguageException.checkerError(String.format("Cannot initialize variable of type %s with expression of type %s", t, rhsType));
				}
			}else{
				System.out.println("Variable not initialized "+id);
			}

			previous.defineSymbol(id, sym);
		}
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("var ");
		sb.append(typeDecl.toString());
		sb.append(" {\n");
		for(int i = 0; i < identifiers.length; i++){
			var id = identifiers[i];
			var expr = expressions[i];
			if(expr == null){
				sb.append(id + "\n");
			} else {
				sb.append(id + " = "+ expr.toString() + "\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	VarDecl(ParserType type, String[] identifiers, Expression[] expressions){
		assert(identifiers.length == expressions.length);
		this.typeDecl = type;
		this.identifiers = identifiers;
		this.expressions = expressions;
	}


	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		// throw new UnsupportedOperationException("Unimplemented method 'genIR'");
		for(var i = 0; i < identifiers.length; i++){
			var id = identifiers[i];
			var expr = expressions[i];

			var info = context.searchSymbol(id);
			var mangledName = builder.addSymbol(id, info);

			if(expr != null){
				builder.addInstruction(new Instruction(OpCode.PUSH, mangledName));
				expr.genIR(context, builder);
				builder.addInstruction(new Instruction(OpCode.STORE));
			}
		}

	}
}

final class Break implements Statement {
	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		return "break";
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
	}
}

final class Continue implements Statement {
	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		return "continue";
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
	}
}

final class Return implements Statement {
	Expression expr;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		return String.format("return %s", expr.toString());
	}

	Return (Expression expr){
		this.expr = expr;
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
	}
}

final class EchoStmt implements Statement{
	Expression expr;

	EchoStmt(Expression expr){
		this.expr = expr;
	}

	public String toString(){
		return String.format("print <- %s", expr.toString());
	}

	public void check(Scope previous) throws LanguageException{
		// throw new UnsupportedOperationException("MERDAAAA");
		expr.evalType(previous);
		return;
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		// throw new UnsupportedOperationException("genIR Print não implementado, orelha seca");
		expr.genIR(context, builder);
		builder.addInstruction(new Instruction(OpCode.PRINT));
	}
}

final class InputStmt implements Statement{
	Expression input;

	InputStmt(Expression input){
		this.input = input;
	}

	public void check(Scope previous) throws LanguageException{
		// throw new UnsupportedOperationException("check Input não implementado");
		var inputType = input.evalType(previous);
		if(input instanceof BinaryExpr){
			LanguageException.checkerError("Not valid");
		}
		else if(input instanceof IndexExpr){

		}

	}
	public void genIR(Scope context, IRBuilder builder) throws LanguageException{
		throw new UnsupportedOperationException("genIR Input não implementado");
	}
}

