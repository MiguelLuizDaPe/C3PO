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
	
	ECHO("print");

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

class StaticSectionInfo {
	int alignment = 1;
	int size = 1;
}

record ReadOnlyData(int kind, int size, int alignment, Object initialValue) {
	public static final int STRING = 0;
	public static final int BYTES = 1;
}

class Program {
	final List<Instruction> instructions;
	final Map<String, ReadOnlyData> readOnlyData;
	final Map<String, StaticSectionInfo> staticInfo;

	public Program(
		List<Instruction> instructions,
		Map<String, ReadOnlyData> readOnlyData,
		Map<String, StaticSectionInfo> staticInfo
	){
		this.instructions = instructions;
		this.readOnlyData = readOnlyData;
		this.staticInfo = staticInfo;
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
		return String.format("__str_lit", this.getUniqueID());
	}

	public String addSymbol(String name, SymbolInfo info) throws LanguageException {
		switch (info.kind) {
			case FUNCTION:
				Debug.unimplemented(); break;
			case VAR, PARAMETER: {
				var staticInfo = new StaticSectionInfo();
				var mangledName = mangleName(info.kind, name);
				info.mangledName = mangledName;
				staticInfo.alignment = info.type.dataAlignment();
				staticInfo.size = info.type.dataSize();
				this.staticSection.put(mangledName, staticInfo);
				return mangledName;
			}
			case TYPE: /* Nothing */ break;
		}
		return null;
	}
	
	public String addStringLit(String value){
		var label = mangleName("__str_lit");
		readOnlyData.put(label, new ReadOnlyData(ReadOnlyData.STRING, value.length(), 1, value));
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

