sealed class Statement permits Scope, WhileStmt, IfStmt, FuncDef, InlineStmt {
	public String toString(){
		return "<Statement>";
	}
}

final class FuncDef extends Statement {
	public record ParameterList (TypeExpr[] types, String[] identifiers){
		public String toString(){
			if(types.length == 0){
				return "()";
			}

			var sb = new StringBuilder();
			sb.append("(");
			for(int i = 0; i < types.length; i ++){
				sb.append(String.format("%s: %s, ", identifiers[i].toString(), types[i].toString()));
			}
			sb.setLength(sb.length() - 1);
			sb.append(")");
			return sb.toString();
		}
	}

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

	IfStmt(Expression cond, Scope body, Statement elseBranch){
		assert(elseBranch instanceof Scope || elseBranch instanceof IfStmt || this.elseBranch == null);
		this.condition = cond;
		this.body = body;
		this.elseBranch = elseBranch;
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

