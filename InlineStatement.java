sealed class InlineStmt extends Statement permits VarAssign, VarDecl, Break, Continue, Return, ExprStmt {
};

final class ExprStmt extends InlineStmt {
	Expression expression;

	ExprStmt(Expression e){
		assert(e != null);
		this.expression = e;
	}
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

	private Modifier(char kind, int size){
		this.kind = kind;
		this.size = size;
	}

	private Modifier(char kind){
		this.kind = kind;
	}

	static Modifier pointer(){
		return new Modifier('P');
	}

	static Modifier array(int n){
		return new Modifier('A', n);
	}
}

class TypeExpr {
	String name;
	Modifier[] mods;

	TypeExpr(String name, Modifier[] mods){
		this.name = name;
		this.mods = mods;
	}

	public String toString(){
		var sb = new StringBuilder();

		sb.append("<Parser Type> ");
		for(int i = mods.length - 1; i >= 0; i--){
			var mod = mods[i];
			if(mod.kind == Modifier.ARRAY){
				sb.append(String.format("array(%d) of ", mod.size));
			}
			else if(mod.kind == Modifier.POINTER) {
				sb.append(String.format("pointer to "));
			}
			else {
				sb.append(String.format("<UNKNOWN MODIFIER>"));
			}
		}
		sb.append(name);
		return sb.toString();
	}
}

final class VarDecl extends InlineStmt {
	TypeExpr typeDecl;
	String[] identifiers;
	Expression[] expressions;

	public String toString(){
		var sb = new StringBuilder();
		// TODO: print init exprs
		sb.append("var ");
		sb.append(typeDecl.toString());
		sb.append(" {\n");
		for(int i = 0; i < identifiers.length; i++){
			var id = identifiers[i];
			var expr = expressions[i];
			if(expr == null){
				sb.append(id + "\n");
			} else {
				sb.append(id + " = "+ expr.toString() + "\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	VarDecl(TypeExpr type, String[] identifiers, Expression[] expressions){
		assert(identifiers.length == expressions.length);
		this.typeDecl = type;
		this.identifiers = identifiers;
		this.expressions = expressions;
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

	Return (Expression expr){
		this.expr = expr;
	}
}

