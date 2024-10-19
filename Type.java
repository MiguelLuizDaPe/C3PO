enum PrimitiveType {
	INT("int"), FLOAT("float"), STRING("string"), CHAR("char"), BOOL("bool");

	String value;

	PrimitiveType(String value){
		this.value = value;
	}
}

class Type {
	PrimitiveType primitive;
	Modifier[] mods;

	public String toString(){
		var sb = new StringBuilder();

		for(int i = mods.length; i > 0; i--){
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
		sb.append(primitive.value);
		return sb.toString();
	}
}

