sealed class InlineStmt extends Statement permits VarAssign, VarDecl, Break, Continue, Return {
};

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

class TypeExpr {
	String id;
	public String toString(){
		return "<type expr>";
	}
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

