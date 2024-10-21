final class ExprStmt implements Statement {
	Expression expression;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
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
		Debug.unimplemented();
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

class TypeExpr {
	String name;
	Qualifier[] quals;

	TypeExpr(String name, Qualifier[] quals){
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

	TypeExpr evalParseType(Scope scope) throws LanguageException{//fiz e rodou, se ta certo é meio foda dizer
		var ok = scope.searchSymbol(this.name);
		if(ok == null){
			LanguageException.checkerError(String.format("Type unidefined: %s", this.name));
		}
		var type = new TypeExpr(ok.type.primitive.value, this.quals);
		
		return type;
	}
}

final class VarDecl implements Statement {
	TypeExpr typeDecl;
	String[] identifiers;
	Expression[] expressions;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	public String toString(){
		var sb = new StringBuilder();
		// TODO: print init exprs
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

	VarDecl(TypeExpr type, String[] identifiers, Expression[] expressions){
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

