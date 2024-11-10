enum OpCode{
	ADD, NEG, SUB, MUL, DIV, MOD, BIT_NOT, BIT_OR, BIT_AND, BIT_XOR, BIT_SH_LEFT, BIT_SH_RIGHT, EQUALS, NOT_EQUALS, LOGIC_NOT, LOGIC_AND, LOGIC_OR, NOT_ZERO,
	PUSH, POP, DUP, LOAD, STORE, LOAD_IMM, STORE_IMM, LOAD_ADDR, BRANCH, JUMP, CALL, RET,
	LABEL,
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
