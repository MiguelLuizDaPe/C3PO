enum PrimitiveType {
	INT("int"), FLOAT("float"), STRING("string"), CHAR("char"), BOOL("bool"), VOID("void");

	String value;

	PrimitiveType(String value){
		this.value = value;
	}
}

class Qualifier {
	static final char ARRAY = 'A';
	static final char POINTER = 'P';

	char kind;
	int size;

	private Qualifier(char kind, int size){
		this.kind = kind;
		this.size = size;
	}

	private Qualifier(char kind){
		this.kind = kind;
	}

	static Qualifier pointer(){
		return new Qualifier('P');
	}

	static Qualifier array(int n){
		return new Qualifier('A', n);
	}
}

class Type {
	PrimitiveType primitive;
	Qualifier[] quals;

	public String toString(){
		var sb = new StringBuilder();

		for(int i = quals.length; i > 0; i--){
			var mod = quals[i];
			if(mod.kind == Qualifier.ARRAY){
				sb.append(String.format("array(%d) of ", mod.size));
			}
			else if(mod.kind == Qualifier.POINTER) {
				sb.append(String.format("pointer to "));
			}
			else {
				sb.append(String.format("<UNKNOWN Qualifier>"));
			}
		}
		sb.append(primitive.value);
		return sb.toString();
	}

	public Type(PrimitiveType primitive, Qualifier[] quals){
		this.primitive = primitive;
		this.quals = quals;
	}

	public static Type fromPrimitiveParserType(ParserType typeExpr) throws LanguageException{
		for(var primType : PrimitiveType.values()){
			if(primType.value == typeExpr.name){
				return new Type(primType, typeExpr.quals);
			}
		}
		LanguageException.checkerError(String.format("Not a builtin type: %s", typeExpr.name));
		return null;
	}
}

