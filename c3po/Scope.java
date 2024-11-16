package c3po;

public final class Scope implements Statement{
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