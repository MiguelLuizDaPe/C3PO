final class ExprStmt implements Statement {
	Expression expression;

	public void check(Scope previous) throws LanguageException{
		// Debug.unimplemented();
		expression.evalType(previous);
	}

	public String toString(){
		return expression.toString();
	}

	ExprStmt(Expression e){
		assert(e != null);
		this.expression = e;
	}
}

final class VarAssign implements Statement {
	Expression left;
	Expression right;

    public void check(Scope previous) throws LanguageException{
        var leftType = left.evalType(previous);
        var rightType = right.evalType(previous);
		
		if(left instanceof PrimaryExpr){
			if(!leftType.equals(rightType)){
				LanguageException.checkerError("Cannot assign type %s to a value of type %s", leftType, rightType);
			}
			var e = (PrimaryExpr)left;
			if(e.token.type == TokenType.ID){
				var sym = previous.searchSymbol(e.token.lexeme);
				sym.init = true;
			}
		}
		if(left instanceof IndexExpr){

			if(!leftType.equals(rightType)){
				LanguageException.checkerError("Cannot assign type %s to a value of type %s", leftType, rightType);
			}
			var e = (IndexExpr)left;
			var y = (PrimaryExpr)e.array;
			if(y.token.type == TokenType.ID){
				var sym = previous.searchSymbol(y.token.lexeme);
				sym.init = true;//isso é bem bobo e não sei se devia estar assim
			}
		}
		//TODO: else if (left insteanceof Indexing)
	}
	public String toString(){
		return String.format("%s <- %s", left.toString(), right.toString());
	}

	VarAssign(Expression left, Expression right){
		assert(left != null && right != null);
		this.left = left;
		this.right = right;
	}
}

class ParserType {
	String name; 
	Qualifier[] quals;

	ParserType(String name, Qualifier[] quals){
		this.name = name;
		this.quals = quals;
	}

	public String toString(){
		var sb = new StringBuilder();

		sb.append("<Parser Type> ");
		for(int i = quals.length - 1; i >= 0; i--){
			var mod = quals[i];
			if(mod.kind == Qualifier.ARRAY){
				sb.append(String.format("array(%d) of ", mod.size));
			}
			else if(mod.kind == Qualifier.POINTER) {
				sb.append(String.format("pointer to "));
			}
			else {
				sb.append(String.format("<UNKNOWN MODIFIER>"));
			}
		}
		sb.append(name);
		return sb.toString();
	}
}

final class VarDecl implements Statement {
	ParserType typeDecl;
	String[] identifiers;
	Expression[] expressions;

	public void check(Scope previous) throws LanguageException{
		var t = Type.fromPrimitiveParserType(this.typeDecl);

		if(t.primitive == PrimitiveType.VOID){
			LanguageException.checkerError("Cannot instantiate variable of incomplete type void");
		}

		for(int i = 0; i < this.identifiers.length; i++){
			var id = identifiers[i];
			var initExpr = this.expressions[i];

			var sym = SymbolInfo.variable(t);

			if(initExpr != null){
				sym.init = true;
				var rhsType = initExpr.evalType(previous);
				if(!rhsType.equals(t)){
					LanguageException.checkerError(String.format("Cannot initialize variable of type %s with expression of type %s", t, rhsType));
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
}

final class Break implements Statement {
	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		return "break";
	}
}

final class Continue implements Statement {
	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		return "continue";
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
}

