sealed class InlineStmt extends Statement permits VarAssign, VarDecl, Break, Continue, Return, ExprStatement {
};

final class ExprStatement extends InlineStmt {
	Expression expression;
}

final class VarAssign extends InlineStmt {
	Expression left;
	Expression right;

	public String toString(){
		return String.format("%s <- %s", left.toString(), right.toString());
	}

	VarAssign(Expression left, Expression right){
		assert(left != null && right != null);
		this.left = left;
		this.right = right;
	}
}

class Modifier {
	static final char ARRAY = 'A';
	static final char POINTER = 'P';

	char kind;
	int size;

	Modifier(char kind, int size){
		this.kind = kind;
		this.size = size;
	}

	Modifier(char kind){
		this.kind = kind;
	}
}

class TypeExpr {
	String name;
	Modifier[] mods;
}

final class VarDecl extends InlineStmt {
	TypeExpr typeDecl;
	String[] identifiers;
	// Expression[] initialValues;
	// int a, b = 3, c = 5;
	// [a    b c]
	// [null E E]

	public String toString(){
		var sb = new StringBuilder();
		// TODO: print init exprs
		sb.append("var ");
		sb.append(typeDecl.toString());
		sb.append(" (");
		for(var id : identifiers){
			sb.append(id);
			sb.append(" ");
		}
		sb.append(")");
		return sb.toString();
	}
}

final class Break extends InlineStmt {
	public String toString(){
		return "break";
	}
}

final class Continue extends InlineStmt {
	public String toString(){
		return "continue";
	}
}

final class Return extends InlineStmt {
	Expression expr;

	public String toString(){
		return String.format("return %s", expr.toString());
	}
}

