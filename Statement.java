interface Statement extends IREmmiter {
	public void check(Scope previous) throws LanguageException;
}

final class FuncDef implements Statement {
	String name;
	ParameterList parameters;
	ParserType returnType;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		if(this.body.env == null){
			this.body.env = new Environment();
		}

		this.body.parent = previous;

		var returnType = Type.fromPrimitiveParserType(this.returnType);

		var parserTypes = this.parameters.types();
		var ids = this.parameters.ids();

		var argTypes = new Type[ids.length];
		for(int i = 0; i < parserTypes.length; i++){
			var t = Type.fromPrimitiveParserType(parserTypes[i]);
			argTypes[i] = t;
			body.defineSymbol(ids[i], SymbolInfo.parameter(t));
		}
		var funcInfo = SymbolInfo.function(returnType, argTypes);

		body.parent.defineSymbol(this.name, funcInfo);
		body.check(this.body.parent);
	}

	public record ParameterList (ParserType[] types, String[] identifiers){
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
		public ParserType[] types(){
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

	FuncDef(String name, ParameterList params, ParserType returnType, Scope body){
		this.name = name;
		this.parameters = params;
		this.returnType = returnType;
		this.body = body;
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
	}

}

final class Scope implements Statement{
	public Statement[] statements;
	public Scope parent;
	public Environment env;

	public void initAsGlobalScope(){
		if(this.env == null){
			this.env = new Environment();
		}
		for(var primType : PrimitiveType.values()){
			env.addSymbol(primType.value, SymbolInfo.type(new Type(primType, null)));
		}
	}

	public void check(Scope previous) throws LanguageException{
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

		if(this.parent == null){
			return null;
		}
		return this.parent.searchSymbol(name);
	}

	void defineSymbol(String name, SymbolInfo info) throws LanguageException{
		if(searchSymbol(name) != null){
			throw LanguageException.checkerError(String.format("Symbol %s is already defined", name));
		}
		this.env.addSymbol(name, info);
	}

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		for(var stmt : statements){
			stmt.genIR(this, builder);
		}
	}

}

final class IfStmt implements Statement {
	Expression condition;
	Scope body;
	Statement elseBranch; // NOTE: Can *only* be Scope(else) OR another If

	public void check(Scope previous) throws LanguageException{
		// Debug.unimplemented();
		if(this.body.env == null){
			this.body.env = new Environment();
		}

		this.body.parent = previous;

		this.body.check(previous);
		var curr = this.elseBranch;

		while(curr != null){
			curr.check(previous);
			if(curr instanceof Scope){ // NOTE: This is the instance of an Else
				curr = null;
			}
		}

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

	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
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
	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
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
	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
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
	public void genIR(Scope context, IRBuilder builder) throws LanguageException {
		throw new UnsupportedOperationException("Unimplemented method 'genIR'");
	}

}

