import java.util.*;

interface IREmmiter {
	public void genIR(Scope context, IRBuilder builder) throws LanguageException;
}

enum OpCode {
	ADD("add"), NEG("neg"), SUB("sub"), MUL("mul"), DIV("div"), MOD("mod"), BIT_NOT("bit_not"), BIT_OR("bit_or"), BIT_AND("bit_and"), BIT_XOR("bit_xor"), 
	BIT_SH_LEFT("bit_sh_left"), BIT_SH_RIGHT("bit_sh_right"), EQUALS("equals"), NOT_EQUALS("not_equals"), LOGIC_NOT("logic_not"), LOGIC_AND("logic_and"), 
	LOGIC_OR("logic_or"), NOT_ZERO("not_zero"),

	PUSH("push"), POP("pop"), DUP("dup"), LOAD("load"), STORE("store"), STORE_IMM("store_imm"), LOAD_ADDR("load_addr"), BRANCH("branch"), 
	JUMP("jump"), CALL("call"), RET("ret"),
	LABEL("label");

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
	String mangledName;
	int alignment = 1;
	int size = 1;
	boolean readOnly = false;
}

class IRBuilder {
	ArrayList<Instruction> instructions;
	private long mangleCounter = 0; /* State used to mangle symbol names */
	ArrayList<SymbolInfo> symbols;

	public String mangleName(String name){
		var nameID = String.format("%s_%d", name, this.manglingID());
		return nameID;
	}

	public long manglingID(){
		mangleCounter += 1;
		return mangleCounter;
	}

	public void addInstruction(Instruction inst){
		instructions.add(inst);
	}

	public IRBuilder(){
		instructions = new ArrayList<Instruction>();
		symbols = new ArrayList<SymbolInfo>();
	}

	Instruction[] build(){
		var insts = instructions.toArray(new Instruction[instructions.size()]);
		return insts;
	}
}

