package c3po;

public interface Statement extends IREmmiter {
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

final class IfStmt implements Statement {
	Expression condition;
	Scope body;
	Statement elseBranch; // NOTE: Can *only* be Scope(else) OR another If

	public void check(Scope previous) throws LanguageException{
		if(this.body.env == null){
			this.body.env = new Environment();
		}
		
		this.body.parent = previous;
		
		this.body.check(previous);
		
		if(this.elseBranch != null){
			elseBranch.check(previous);
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

		// System.out.println("piroca");

		var labelId = builder.getUniqueIDLabel();

		String entry = String.format("IF_%d", labelId);
		String elseLabel = String.format("ELSE_%d", labelId);
		String exit = String.format("ENDIF_%d", labelId);

		builder.addInstruction(new Instruction(OpCode.LABEL, entry));// Miguel NOTE: It is not necessary, but helps to find where it starts
		condition.genIR(context, builder);

		if(elseBranch != null){
			builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, elseLabel));
		}else{
			builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, exit));
		}

		body.genIR(context, builder);

		builder.addInstruction(new Instruction(OpCode.JUMP, exit));

		if (elseBranch != null){
			builder.addInstruction(new Instruction(OpCode.LABEL, elseLabel));
			elseBranch.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.JUMP, exit));
		}

		builder.addInstruction(new Instruction(OpCode.LABEL, exit));
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

