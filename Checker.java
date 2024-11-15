import java.util.*;
 
interface Checker {
	void checkScopes(Scope previous) throws LanguageException;
}

class ParserType {
	String name; 
	Qualifier[] quals;

	ParserType(String name, Qualifier[] quals){
		this.name = name;
		this.quals = quals;
	}

	public String toString(){
		var sb = new StringBuilder();

		sb.append("<Parser Type> ");
		for(int i = quals.length - 1; i >= 0; i--){
			var mod = quals[i];
			if(mod.kind == Qualifier.ARRAY){
				sb.append(String.format("array(%d) of ", mod.size));
			}
			else if(mod.kind == Qualifier.POINTER) {
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

class SymbolInfo{
	SymbolKind kind;	
	Type type;
	Type[] arguments;
	boolean init;
	boolean used;
	String mangledName;

	SymbolInfo(SymbolKind kind, Type type, Type[] arguments, boolean init, boolean used){
		this.kind = kind;
		this.type = type;
		this.arguments = arguments;
		this.init = init;
		this.used = used;
	}

	static SymbolInfo function(Type type, Type[] arguments){
		return new SymbolInfo(SymbolKind.FUNCTION, type, arguments, true, true);
	}
	static SymbolInfo variable(Type type){
		return new SymbolInfo(SymbolKind.VAR, type, null, false, false);
	}
	static SymbolInfo type(Type innerType){
		return new SymbolInfo(SymbolKind.TYPE, innerType, null, true, true);
	}
	static SymbolInfo parameter(Type type){
		return new SymbolInfo(SymbolKind.PARAMETER, type, null, true, false);
	}
}

enum SymbolKind{
	FUNCTION("fn"),
	VAR("var"),
	TYPE("type"),
	PARAMETER("parameter");

	public String value;
	SymbolKind(String value){
		this.value = value;
	}
}

class Environment{
	HashMap<String, SymbolInfo> entries;

	public Environment(){
		this.entries = new HashMap<String, SymbolInfo>();
	}

	public void addSymbol(String name, SymbolInfo info){
		entries.put(name, info);
	}

	public SymbolInfo getSymbol(String name){
		return entries.get(name);
	}
	
	public boolean hasSymbol(String name){
		return entries.containsKey(name);
	}

	public String toString(){
		var sb = new StringBuilder();
	
		sb.append("- ID\t kind\t| type\t| init\t| used\t| name\t\n");
	
		for(var id : entries.keySet()){
			var info = entries.get(id);
			var size = "?";
			try {
				size = String.format("%d", info.type.dataSize());
			} catch (LanguageException e){}

			sb.append(String.format("%s:\t %s\t| %s\t| %d\t| %d\t| %s\t| %s\n",
				id, info.kind.value ,info.type, info.init ? 1 : 0, info.used ? 1 : 0, size));
			
		}
		return sb.toString();
	}
}
