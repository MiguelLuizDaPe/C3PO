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

	public String toString(){
		var sb = new StringBuilder();
		sb.append("fn " + name);
		sb.append(parameters.toString());
		sb.append(" -> " + returnType.toString());
		sb.append(body.toString());
		return sb.toString();
	}

	FuncDef(String name, ParameterList params, TypeExpr returnType, Scope body){
		this.name = name;
		this.parameters = params;
		this.returnType = returnType;
		this.body = body;
	}
}

final class Scope extends Statement {
	Statement[] statements;
	// Scope parent;

	Scope(Statement[] statements){
		this.statements = statements;
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("{\n");
		for(var stmt : statements){
			sb.append(stmt.toString());
			sb.append("\n");
		}
		sb.append("}");
		return sb.toString();
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

	public String toString(){
		var sb = new StringBuilder();
		sb.append("if ");
		sb.append(condition.toString());
		sb.append(body.toString());
		sb.append("\n");
		if(elseBranch != null){
			sb.append("else ");
			sb.append(elseBranch.toString());
		}
		return sb.toString();
	}
}

final class WhileStmt extends Statement {
	Expression condition;
	Scope body;

	WhileStmt(Expression cond, Scope body){
		this.condition = cond;
		this.body = body;
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("while ");
		sb.append(condition.toString());
		sb.append(body.toString());
		sb.append("\n");
		return sb.toString();
	}

}

