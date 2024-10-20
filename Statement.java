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

	Scope(Statement[] statements){
		this.statements = statements;
	}
}


final class IfStmt extends Statement {
	Expression condition;
	Scope body;
	Statement elseBranch; // NOTE: Can *only* be Scope(else) OR another If

	IfStmt(Expression cond, Scope body){
		this.condition = cond;
		this.body = body;
	}

	public void appendElseBranch(Statement statement){
		assert(statement instanceof Scope || statement instanceof IfStmt || this.elseBranch == null);
		this.elseBranch = statement;
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

