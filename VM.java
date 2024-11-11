import java.util.*;

class VMException extends RuntimeException {
	public VMException(String msg){
		super(msg);
	}
}

class VM{
	int stackPtr;
	int progCounter;
	Instruction[] program;
	int[] staticMemory;
	int[] stack;
	HashMap<String, Integer> dataLabels;
	HashMap<String, Integer> jumpLabels;

	public VM(int stackSize){
		stack = new int[stackSize];
		staticMemory = new int[1 * 1024 * 1024];

		jumpLabels = new HashMap<String, Integer>();
		dataLabels = new HashMap<String, Integer>();
	}

	int execute(){
		while(step());
		return pop();
	}

	void loadProgram(Instruction[] program){
		reset();
		this.program = program;
		for(int i = 0; i < program.length; i ++){
			var instruction = program[i];
			if(instruction.op == OpCode.LABEL){
				var name = instruction.labelName.trim();
				if(name.length() == 0){
					throw new VMException("Empty label");
				}
				if(jumpLabels.containsKey(name)){
					throw new VMException("Label already exists");
				}
				jumpLabels.put(name, i);
			}
		}
	}

	void reset(){
		progCounter = 0;
		stackPtr = 0;
		jumpLabels.clear();
		dataLabels.clear();
		for(int i = 0; i < stack.length; i++){
			stack[i] = 0;
		}
	}

	int pop(){
		if(stackPtr == 0){
			throw new VMException("Stack overflow.");
		}
		stackPtr -= 1;
		int val = stack[stackPtr];
		return val;
	}

	void push(int val){
		if(stackPtr >= stack.length){
			throw new VMException("Stack overflow.");
		}
		stack[stackPtr] = val;
		stackPtr += 1;
	}

	int top(){
		return stack[stackPtr - 1];
	}

	boolean step(){
		if(progCounter >= program.length){
			return false;
		}
		var instruction = program[progCounter];
		progCounter += 1;
		switch(instruction.op){
			/* Arithmetic */
			case ADD: {
				var b = pop();
				var a = pop();
				push(a + b);
			} break;
			
			case SUB: {
				var b = pop();
				var a = pop();
				push(a - b);
			} break;

			case MUL: {
				var b = pop();
				var a = pop();
				push(a * b);

			} break;

			case DIV: {
				var b = pop();
				var a = pop();
				push(a / b);
			} break;

			case MOD: {
				var b = pop();
				var a = pop();
				push(a % b);

			} break;

			case NEG: {
				var a= pop();
				push(-a);
			} break;

			/* Bitwise */
			case BIT_AND: {
				var b = pop();
				var a = pop();
				push(a & b);
			} break;

			case BIT_XOR: {
				var b = pop();
				var a = pop();
				push(a ^ b);
			} break;

			case BIT_OR: {
				var b = pop();
				var a = pop();
				push(a | b);
			} break;

			case BIT_SH_LEFT: {
				var b = pop();
				var a = pop();
				push(a << b);
			} break;

			case BIT_SH_RIGHT: {
				var b = pop();
				var a = pop();
				push(a >> b);
			} break;

			case BIT_NOT: {
				var a = pop();
				push(~a);
			} break;

			/* Logic */
			case EQUALS: {
				var b = pop();
				var a = pop();
				push(a == b ? 1 : 0);			
			} break;

			case NOT_EQUALS: {
				var b = pop();
				var a = pop();
				push(a != b ? 1 : 0);			
			} break;

			case NOT_ZERO: {
				var a = pop();
				push(a != 0 ? 1 : 0);
			} break;

			case LOGIC_AND: {
				Debug.unimplemented();	
			} break;

			case LOGIC_NOT: {
				Debug.unimplemented();
			} break;

			case LOGIC_OR: {
				Debug.unimplemented();
			} break;

			/* Control flow */
			case JUMP: {
				Debug.unimplemented();
			} break;

			case BRANCH: {
				Debug.unimplemented();
			} break;

			case CALL: {
				Debug.unimplemented();
			} break;

			case RET: {
				Debug.unimplemented();
			} break;

			/* Memory */
			case DUP: {
				push(top());
			} break;

			case LOAD: {
				Debug.unimplemented();
			} break;

			case LOAD_ADDR: {
				Debug.unimplemented();
			} break;

			case POP: {
				pop();
			} break;

			case PUSH: {
				push(instruction.word);
			} break;

			case STORE: {
				Debug.unimplemented();
			} break;

			case STORE_IMM: {
				Debug.unimplemented();
			} break;

			/* Other */
			case LABEL: {
				/* Pseudo instruction, just carry on my wayward son */
			} break;

			default: {
				throw new VMException("Invalid instruction");
			}
		}
		return true;
	}

}
