sealed class Statement permits Scope, WhileStmt, IfStmt, InlineStmt {
	public String toString(){
		return "<Statement>";
	}
}

final class Scope extends Statement {
	Statement[] statements;
	// Scope parent;
}

final class IfStmt extends Statement {
	Expression condition;
	Scope body;
	// Statement elseBranch;
}

final class WhileStmt extends Statement {
	Expression condition;
	Scope body;
}

