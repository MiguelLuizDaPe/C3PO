sealed class Statement permits Scope, WhileStmt, IfStmt, FuncDef, InlineStmt {
	public String toString(){
		return "<Statement>";
	}
}

final class FuncDef extends Statement {
	public record ParameterList (TypeExpr[] types, String[] identifiers){}

	String name;
	ParameterList parameters;
	TypeExpr returnType;
	Scope body;
}

final class Scope extends Statement {
	Statement[] statements;
	// Scope parent;
}

final class IfStmt extends Statement {
	Expression condition;
	Scope body;
	// Statement elseBranch;

	IfStmt(Expression cond, Scope body){
		this.condition = cond;
		this.body = body;
	}
}

final class WhileStmt extends Statement {
	Expression condition;
	Scope body;

	WhileStmt(Expression cond, Scope body){
		this.condition = cond;
		this.body = body;
	}
}

