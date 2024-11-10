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
}

class Instruction {
	OpCode op;
	int word;
	String labelName;
}

class StaticSection {
	String name;
	int alignment = 1;
	int size = 1;
	boolean readOnly = false;
}

class IRProgram {
    StaticSection staticSection;
    Instruction[] instructions;
}

class IRBuilder {

}
