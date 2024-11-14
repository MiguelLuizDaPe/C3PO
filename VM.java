import java.util.*;

class VMException extends RuntimeException {
	public VMException(String msg){
		super(msg);
	}
}

class Program {
	Instruction[] instructions;
	StaticSectionInfo[] staticData;
};

class VM {
	int stackPtr;
	int progCounter;
	int[] stack;
	int[] dataSection;
	HashMap<String, Integer> dataLabels;
	HashMap<String, Integer> jumpLabels;
	Program program;

	public VM(int stackSize){
		stack = new int[stackSize];

		jumpLabels = new HashMap<String, Integer>();
		dataLabels = new HashMap<String, Integer>();
	}
	
	void displayDataSection(){
		for(int i = 0; i < dataSection.length; i ++){
			System.out.println(String.format("%8x ", dataSection[i]));
		}
	}

	int execute(){
		while(step());
		displayDataSection();

		if(stackPtr < 1){
			return 0;
		}
		return pop();
	}

	private boolean labelInUse(String label){
		return dataLabels.containsKey(label) || jumpLabels.containsKey(label);
	}

	void loadProgram(Program program){
		reset();
		this.program = program;
		for(int i = 0; i < program.instructions.length; i ++){
			var instruction = program.instructions[i];
			if(instruction.op == OpCode.LABEL){
				var name = instruction.labelName.trim();
				if(name.length() == 0){
					throw new VMException("Empty label");
				}
				if(labelInUse(name)){
					throw new VMException("Label already in use");
				}
				jumpLabels.put(name, i);
			}
		}

		var staticDataBuf = new ArrayList<Integer>();

		for(var data : program.staticData){
			int last = staticDataBuf.size();
			System.out.println(String.format("%s : %d", data.mangledName, data.size));
			for(int i = 0; i < data.size; i++){
				staticDataBuf.add(0);
			}
			if(labelInUse(data.mangledName)){
				throw new VMException("Label already in use");
			}
			dataLabels.put(data.mangledName, last);
		}

		dataSection = new int[staticDataBuf.size()];
		for(int i = 0; i < dataSection.length; i ++){
			dataSection[i] = staticDataBuf.get(i).intValue();
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
		if(stackPtr < 1){
			throw new VMException("Stack overflow. (Stack empty)");
		}
		stackPtr -= 1;
		int val = stack[stackPtr];
		return val;
	}

	void push(int val){
		if(stackPtr >= stack.length){
			throw new VMException("Stack overflow. (Stack full)");
		}
		stack[stackPtr] = val;
		stackPtr += 1;
	}

	int top(){
		return stack[stackPtr - 1];
	}

	boolean step(){
		if(progCounter >= program.instructions.length){
			return false;
		}
		var instruction = program.instructions[progCounter];
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
				// Debug.unimplemented();
				System.out.println("LOAD");// NOTE Miguel : tentei
				var addr = pop();
				push(dataSection[addr]);
			} break;

			case POP: {
				pop();
			} break;

			case PUSH: {
				if(instruction.labelName == null){
					push(instruction.word);
				}
				else {
					var name = instruction.labelName.trim();
					if(dataLabels.containsKey(name)){
						push(dataLabels.get(name).intValue());
					}
					else if(jumpLabels.containsKey(name)){
						push(jumpLabels.get(name).intValue());
					}
					else {
						throw new VMException("Unkown label");
					}
				}
			} break;

			case STORE: {
				var value = pop();
				var addr = pop();
				dataSection[addr] = value;
			} break;

			case ECHO: {
				var out = pop();
				System.out.println("Print: " + out);
			} break;

			/* Other */
			case LABEL: {
				/* Pseudo instruction, just carry on my wayward son */
			} break;

			default: {
				throw new VMException("Invalid instruction");
			}
		}
		// System.out.print("sp:"+stackPtr + " |");
		// for(int i = 0; i < stackPtr; i ++){
		// 	System.out.print(stack[i] + " ");
		// } System.out.println();


		return true;
	}

}
