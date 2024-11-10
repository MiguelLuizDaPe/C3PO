import java.util.*;

interface Checker{
	void checkScopes(Scope previous) throws LanguageException;
	// void checkScopes(Scope previous, FuncDef currentFunc);
}

class SymbolInfo{
	SymbolKind kind;	
	Type type;
	Type[] arguments;
	boolean init;
	boolean used;
	StaticSection sectionInfo;

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

		sb.append("- ID\t kind\t| type\t| init\t| used\t|-\n");
		for(var id : entries.keySet()){
			var info = entries.get(id);
			sb.append(String.format("%s:\t %s\t| %s\t| %d\t| %d\t|\n", id, info.kind.value ,info.type, info.init ? 1 : 0, info.used ? 1 : 0));
		}
		return sb.toString();
	}
}
