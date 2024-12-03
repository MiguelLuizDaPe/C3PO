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
		
		var ok = this.condition.evalType(previous).equals(new Type(PrimitiveType.BOOL, null));
		if(!ok){
			throw LanguageException.checkerError("Condition must be of boolean type");
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
		var labelId = builder.getUniqueIDLabel();

		String entry_label = String.format("IF_%d", labelId);
		String elseLabel = String.format("ELSE_%d", labelId);
		String exit_label = String.format("ENDIF_%d", labelId);

		builder.addInstruction(new Instruction(OpCode.LABEL, entry_label));// Miguel NOTE: It is not necessary, but helps to find where it starts
		condition.genIR(context, builder);

		if(elseBranch != null){
			builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, elseLabel));
		}else{
			builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, exit_label));
		}

		body.genIR(context, builder);

		builder.addInstruction(new Instruction(OpCode.JUMP, exit_label));

		if (elseBranch != null){
			builder.addInstruction(new Instruction(OpCode.LABEL, elseLabel));
			elseBranch.genIR(context, builder);
			builder.addInstruction(new Instruction(OpCode.JUMP, exit_label));
		}

		builder.addInstruction(new Instruction(OpCode.LABEL, exit_label));
	}
}

final class ForStmt implements Statement{
	Statement first;
	Expression condition;
	Statement after;
	Scope body;

	public void check(Scope previous) throws LanguageException{// Miguel TODO: Maybe let use variable or null in first
		this.body.check(previous);

		this.first.check(body);
		this.after.check(previous);

		var ok = this.condition.evalType(previous).equals(new Type(PrimitiveType.BOOL, null));
		if(!ok){
			throw LanguageException.checkerError("Condition must be of boolean type");
		}
		this.body.parent = previous;
	}
	ForStmt(Statement first, Expression condition, Statement after, Scope body){
		assert(first instanceof VarDecl || first instanceof VarAssign);
		assert(after instanceof VarAssign);// Miguel NOTE: Dont know if this asserts make sense
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
		var id = builder.getUniqueIDLabel();
		String entry_label = String.format("FOR_%d", id);
		String check_label = String.format("CHECKFOR_%d", id);
		String body_label = String.format("BODYFOR_%d", id);
		String exit_label = String.format("ENDFOR_%d", id);

		builder.addInstruction(new Instruction(OpCode.LABEL, entry_label));
		first.genIR(context, builder);
		condition.genIR(context, builder);
		builder.addInstruction(new Instruction(OpCode.BRANCH_NOT_ZERO, body_label));

		builder.addInstruction(new Instruction(OpCode.LABEL, check_label));
		condition.genIR(context, builder);
		builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, exit_label));
		
		builder.addInstruction(new Instruction(OpCode.LABEL, body_label));
		this.body.genIR(context, builder);
		after.genIR(context, builder);
		builder.addInstruction(new Instruction(OpCode.JUMP, check_label));

		builder.addInstruction(new Instruction(OpCode.LABEL, exit_label));
	}
}

final class DoStmt implements Statement {
	Expression condition;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		var ok = this.condition.evalType(previous).equals(new Type(PrimitiveType.BOOL, null));
		if(!ok){
			throw LanguageException.checkerError("Condition must be of boolean type");
		}
		
		this.body.parent = previous;
		this.body.check(previous);
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
		var id = builder.getUniqueIDLabel();
		String entry_label = String.format("WHILE_%d", id);
		String exit_label = String.format("ENDWHILE_%d", id);

		builder.addInstruction(new Instruction(OpCode.LABEL, entry_label));
		body.genIR(context, builder);

		condition.genIR(context, builder);
		
		builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, exit_label));
		builder.addInstruction(new Instruction(OpCode.JUMP, entry_label));
		
		builder.addInstruction(new Instruction(OpCode.LABEL, exit_label));
	}

}

final class WhileStmt implements Statement {
	Expression condition;
	Scope body;

	public void check(Scope previous) throws LanguageException{
		var ok = this.condition.evalType(previous).equals(new Type(PrimitiveType.BOOL, null));
		if(!ok){
			throw LanguageException.checkerError("Condition must be of boolean type");
		}
		
		this.body.parent = previous;
		this.body.check(previous);
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
		var id = builder.getUniqueIDLabel();
		String entry_label = String.format("WHILE_%d", id);
		String exit_label = String.format("ENDWHILE_%d", id);

		builder.addInstruction(new Instruction(OpCode.LABEL, entry_label));
		condition.genIR(context, builder);
		
		builder.addInstruction(new Instruction(OpCode.BRANCH_EQUAL_ZERO, exit_label));

		body.genIR(context, builder);
		builder.addInstruction(new Instruction(OpCode.JUMP, entry_label));
		
		builder.addInstruction(new Instruction(OpCode.LABEL, exit_label));
	}

}

