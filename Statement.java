import java.util.*;

sealed interface Statement permits Scope, WhileStmt, IfStmt, FuncDef, ForStmt, DoStmt, VarAssign, VarDecl, Break, Continue, Return, ExprStmt {
	public void check(Scope previous) throws LanguageException;
	// public void checkScopes(Scope previous, FuncDef currentFunc);
}

final class FuncDef implements Statement {
	String name;
	ParameterList parameters;
	TypeExpr returnType;
	Scope body;


	public void check(Scope previous) throws LanguageException{
		// Debug.unimplemented();
		if(this.body.env == null){
			this.body.env = new Environment();
		}

		this.body.parent = previous;

		// for(var statement : this.statements){
		var returnTypeK = returnType.evalParseType(body);
		// var fnInfo = SymbolInfo(SymbolKind.FUNCTION, )
		
		var argTypes = new ArrayList<TypeExpr>();


		var types = this.parameters.types();
		var ids = this.parameters.ids();

		for(int i = 0; i < types.length; i++){
			var k = types[i].evalParseType(previous);
			argTypes.add(k);
			var t = Type.fromPrimitiveTypeExpr(types[i]);
			body.defineSymbol(ids[i], SymbolInfo.parameter(t));
		}




		statement.check(this);
		// }	 

	}
	
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
		public TypeExpr[] types(){
 			return this.types;  
		}

		public String[] ids(){
  			return this.identifiers;
		}
	}

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

final class Scope implements Statement{
	Statement[] statements;
	Scope parent;
	Environment env;

	public void check(Scope previous) throws LanguageException{
		// Debug.unimplemented();	
		if(this.env == null){
			this.env = new Environment();
		}

		this.parent = previous;

		for(var statement : this.statements){
			statement.check(this);
		}
	}

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

	SymbolInfo searchSymbol(String name){
		if (this.env.hasSymbol(name)){
			var info = this.env.getSymbol(name);
			// info.used = info.used || increaseUsage;
			return info;
		}
		
		return this.parent.searchSymbol(name);
	}

	void defineSymbol(String name, SymbolInfo info) throws LanguageException{
		if(searchSymbol(name) != null){
			LanguageException.checkerError(String.format("Symbol %s is already defined", name));
		}
		this.env.addSymbol(name, info);
	}


	// public void check(Scope previous) throws LanguageException{
	// 	if(this.env == null){
	// 		this.env = new Environment();
	// 	}
	//
	// 	this.parent = previous;
	//
	// 	for(var statement : this.statements){
	// 		statement.check(this);
	// 	}	
	// }
}

final class IfStmt implements Statement {
	Expression condition;
	Scope body;
	Statement elseBranch; // NOTE: Can *only* be Scope(else) OR another If

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}

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

final class ForStmt implements Statement{
	Statement first;
	Expression condition;
	Statement after;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	ForStmt(Statement first, Expression condition, Statement after, Scope body){
		this.first = first;
		this.condition = condition;
		this.after = after;
		this.body = body;
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("for ");
		sb.append("first ");
		sb.append(first.toString());
		sb.append("condition ");
		sb.append(condition.toString());
		sb.append("after ");
		sb.append(after.toString());
		sb.append(body.toString());
		sb.append("\n");
		return sb.toString();
	}
}

final class DoStmt implements Statement {
	Expression condition;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
	DoStmt(Expression cond, Scope body){
		this.condition = cond;
		this.body = body;
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("do while ");
		sb.append(condition.toString());
		sb.append(body.toString());
		sb.append("\n");
		return sb.toString();
	}

}

final class WhileStmt implements Statement {
	Expression condition;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		Debug.unimplemented();
	}
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

