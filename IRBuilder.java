import java.util.*;

interface IREmmiter {
	public void genIR(Scope context, IRBuilder builder) throws LanguageException;
}

enum OpCode {
	ADD("add"), NEG("neg"), SUB("sub"), MUL("mul"), DIV("div"), MOD("mod"), BIT_NOT("bit_not"), BIT_OR("bit_or"), BIT_AND("bit_and"), BIT_XOR("bit_xor"),
	BIT_SH_LEFT("bit_sh_left"), BIT_SH_RIGHT("bit_sh_right"), EQUALS("equals"), NOT_EQUALS("not_equals"), LOGIC_NOT("logic_not"), LOGIC_AND("logic_and"),
	LOGIC_OR("logic_or"), NOT_ZERO("not_zero"),

	PUSH("push"), POP("pop"), DUP("dup"), LOAD("load"), STORE("store"),	BRANCH("branch"),

	JUMP("jump"), CALL("call"), RET("ret"),
	LABEL("label"),

	PRINT("print");

	public String value;

	OpCode(String v){
		value = v;
	}

	public String toString(){
		return value;
	}
}

class Instruction {
	OpCode op;
	int word;
	String labelName;

	public String toString(){
		if(op == OpCode.LABEL){
			return labelName + ":";
		}
		if(op == OpCode.PUSH){
			if(labelName != null){
				return op.toString() + " " + labelName;
			}else{
				return op.toString() + " " + word;
			}
		}
		return op.toString();
	}

	public Instruction(OpCode op){
		this.op = op;
	}

	public Instruction(OpCode op, int word) {
		this.op = op;
		this.word = word;
	}

	public Instruction(OpCode op, String labelName) {
		assert(op == OpCode.LABEL);
		this.op = op;
		this.labelName = labelName;
	}
}

record StaticSectionInfo(int alignment, int size) {}

record ReadOnlyData(int size, int alignment, String initialValue) {
}

class Program {
	final List<Instruction> instructions;
	final Map<String, ReadOnlyData> readOnlyData;
	final Map<String, StaticSectionInfo> staticSection;

	public Program(
		List<Instruction> instructions,
		Map<String, ReadOnlyData> readOnlyData,
		Map<String, StaticSectionInfo> staticInfo
	){
		this.instructions = instructions;
		this.readOnlyData = readOnlyData;
		this.staticSection = staticInfo;
	}

	public String toString(){
		var sb = new StringBuilder();
		sb.append("Read only data:\n");
		for(var entry : readOnlyData.entrySet()){
			var data = entry.getValue();
			var line = String.format("  %s: align(%d) size(%d) \"%s\"\n",
				entry.getKey(), data.alignment(), data.size(), data.initialValue());
			sb.append(line);
		}
		sb.append("Static data:\n");
		for(var entry : staticSection.entrySet()){
			var data = entry.getValue();
			var line = String.format("  %s: align(%d) size(%d)\n", entry.getKey(), data.alignment(), data.size());
			sb.append(line);
		}
		sb.append("Text section:\n");
		for(var ins : instructions){
			sb.append("  ");
			sb.append(ins.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

class IRBuilder {
	private long idCounter = 0; /* State used to mangle symbol names */
	ArrayList<Instruction> instructions;
	HashMap<String, ReadOnlyData> readOnlyData;
	HashMap<String, StaticSectionInfo> staticSection;

	public IRBuilder(){
		instructions = new ArrayList<Instruction>();
		staticSection = new HashMap<String, StaticSectionInfo>();
		readOnlyData = new HashMap<String, ReadOnlyData>();
	}

	private String mangleName(SymbolKind kind, String name) throws LanguageException {
		switch (kind) {
		case FUNCTION:
			return name;
		case PARAMETER:
			return String.format("p_%s_%d", name, this.getUniqueID());
		case TYPE:
			return null;
		case VAR:
			return String.format("v_%s_%d", name, this.getUniqueID());
		default:
			throw LanguageException.emitterError("Invalid info spec");
		}
	}

	private String mangleName(String literal) {
		return String.format("__str_lit_%d", this.getUniqueID());
	}

	public String addSymbol(String name, SymbolInfo info) throws LanguageException {
		switch (info.kind) {
			case FUNCTION:
				Debug.unimplemented(); break;
			case VAR, PARAMETER: {
				var mangledName = mangleName(info.kind, name);
				var staticInfo = new StaticSectionInfo(info.type.dataAlignment(), info.type.dataSize());
				info.mangledName = mangledName;
				this.staticSection.put(mangledName, staticInfo);
				return mangledName;
			}
			case TYPE: /* Nothing */ break;
		}
		return null;
	}

	public String addStringLit(String value){
		var label = mangleName("__str_lit");
		var roData = new ReadOnlyData(value.length(), 1, value);
		readOnlyData.put(label, roData);
		return label;
	}

	public long getUniqueID(){
		idCounter += 1;
		return idCounter;
	}

	public void addInstruction(Instruction inst){
		instructions.add(inst);
	}

	public void popInstruction(){
		instructions.remove(instructions.size() - 1);
	}

	public Program build(){
		return new Program(instructions, readOnlyData, staticSection);
	}
}

