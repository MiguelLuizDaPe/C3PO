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

	public int dataSize() throws LanguageException{
		var primitiveDataSize = 0;
		switch(primitive){
			case INT: {
				primitiveDataSize = 4;
			} break;
			case FLOAT: {
				primitiveDataSize = 4;
			} break;
			case STRING: {
				Debug.unimplemented();
			} break;
			case CHAR: {
				Debug.unimplemented();
			} break;
			case BOOL: {
				primitiveDataSize = 4;
			} break;
			case VOID: {
				throw LanguageException.emitterError("Incomplete type has no data size");
			}
		}
		int acc = 1;
		for(var q : quals){
			if(q.kind == Qualifier.ARRAY){
				acc *= q.size;
			}
			else{// TODO : POINTERS
				Debug.unimplemented();
			}
		}
			
		return primitiveDataSize * acc;
	}

	public int dataAlignment() throws LanguageException{
		int alignment = 0;
		switch(primitive){
			case INT: {
				alignment = 4;
			} break;
			case FLOAT: {
				alignment = 4;
			} break;
			case STRING: {
				Debug.unimplemented();
			} break;
			case CHAR: {
				Debug.unimplemented();
			} break;
			case BOOL: {
				alignment = 4;
			} break;
			case VOID: {
				throw LanguageException.emitterError("Incomplete type has no data size");
			}
		}
		return alignment;
	}

	public boolean equals(Type other){
		if(this.primitive != other.primitive || this.quals.length != other.quals.length){
			return false;
		}

		for(int i = 0; i < quals.length; i ++){
			if(this.quals[i] != other.quals[i]){
				return false;
			}
		}

		return true;
	}

	public String toString(){
		var sb = new StringBuilder();

		for(int i = 0; i < quals.length; i++){
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

		if(this.quals == null){
			this.quals = new Qualifier[0];
		}
	}

	public static Type fromPrimitiveParserType(ParserType typeExpr) throws LanguageException{
		for(var primType : PrimitiveType.values()){
			if(primType.value.equals(typeExpr.name)){
				return new Type(primType, typeExpr.quals);
			}
		}
		throw LanguageException.checkerError(String.format("Not a builtin type: %s", typeExpr.name));
	}
}

