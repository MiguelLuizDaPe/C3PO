package c3po;

public class AssemblyBuilder {
    StringBuilder readOnlySection;
    StringBuilder dataSection;
    StringBuilder textSection;
    Program program;

    public AssemblyBuilder(Program prog) {
        readOnlySection = new StringBuilder();
        dataSection = new StringBuilder();
        textSection = new StringBuilder();
        program = prog;
    }

    public String build() throws LanguageException {
        buildTextSection();
        generateRODataSection();
        generateDataSection();

        var output = new StringBuilder();
        output.append(".data\n");
        output.append(readOnlySection.toString());
        output.append("\n");
        output.append(dataSection.toString());
        output.append("\n");
        output.append(textSection.toString());
        output.append("\n");
        return output.toString();
    }

    static int rv32AlignmentCode(int align) throws LanguageException{
        switch(align){
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 8: return 3;
            default: throw LanguageException.emitterError("Invalid alignment: %d", align);
        }
    }

    void generateRODataSection() throws LanguageException{
        final String dataRtCode = """
        __NEWLINE: .string "\\n"
        .rodata __TRUE: .string "true"
        .rodata __FALSE: .string "false"
        """;
        readOnlySection.append(dataRtCode);

        final String fmt = """
        .align %d
        %s: .string "%s"
        """;

        for(var entry : program.readOnlyData.entrySet()){
            var data = entry.getValue();
            readOnlySection.append(String.format(fmt, rv32AlignmentCode(data.alignment()), entry.getKey(), data.value()));
        }
    }

    void generateDataSection() throws LanguageException{


        final String fmt = """
        .align %d
        %s: .space %d
        """;
        for (var entry : program.staticSection.entrySet()) {
            var data = entry.getValue();
            dataSection.append(String.format(fmt, rv32AlignmentCode(data.alignment()), entry.getKey(), data.size()));
        }
    }

    void pushImmediate(int word){
        final String fmt = """
        li t0, %d        # %s
        addi sp, sp, -4
        sw t0, (sp)
        """;
        textSection.append(String.format(fmt, word, "Push " + word));
    }

    void store(){
        final String fmt = """
        lw t0, (sp) # Store
        lw t1, 4(sp)
        sw t0, (t1)
        addi sp, sp, 8
        """;
        textSection.append(String.format(fmt));
    }

    void load(){
        final String fmt = """
        lw t0, (sp)  # Load
        lw t1, (t0)
        sw t1, (sp)
        """;
        textSection.append(String.format(fmt));
    }

    void pushLabel(String label){
        final String fmt = """
        la t0, %s       # %s
        addi sp, sp, -4
        sw t0, (sp)
        """;
        textSection.append(String.format(fmt, label, "Push " + label));
    }

    void printInt(){
        final String fmt = """
        lw a0, (sp) # Print_Int
        addi sp, sp, -4
        li a7, 1  
        ecall     
        """;
        textSection.append(String.format(fmt));

    }

    void inputInt(String label){
        final String fmt = """
        li a7, 5 # Input_Int
        ecall
        addi sp, sp, -4
        sw a0, (sp)
        """;
        textSection.append(fmt);
    }

    void newLine(){
        final String fmt = """
        li a7, 4 # NewLine
        la a0, __NEWLINE
        ecall
        """;
        textSection.append(fmt);
    }

    void printStr(){
        final String fmt = """
        li a7, 4 # PRINT_STR
        lw a0, (sp)    # %s
        addi sp, sp, 4
        ecall
        """;
        textSection.append(fmt);
    }

    private static String translateOpCodeToRV32(OpCode op) throws LanguageException {
        var err = LanguageException.emitterError("No direct translation to " + op.value);
        switch (op) {
            case ADD: return "add";
            case BIT_AND: return "and";
            case BIT_NOT: throw err;
            case BIT_OR: return "or";
            case BIT_SH_LEFT: return "sll";
            case BIT_SH_RIGHT: return "srl";
            case BIT_XOR: return "xor";
            case BRANCH_EQUAL_ZERO: return "beqz";
            case BRANCH_NOT_ZERO: return "bnez";
            case CALL: throw err;
            case DIV: return "div";
            case DUP: throw err;
            case EQUALS: throw err;
            case GT: return "sgt";
            case LT: return "slt";
            case GT_EQ: throw err;
            case LT_EQ: throw err;
            case INPUT_INT: throw err;
            case INPUT_STR: throw err;
            case JUMP: return "j";
            case LOAD: throw err;
            case LOGIC_AND: throw err;
            case LOGIC_NOT: throw err;
            case LOGIC_OR: throw err;
            case MOD: throw err;
            case MUL: return "mul";
            case NEG: throw err;
            case NOT_EQUALS: throw err;
            case NOT_ZERO: throw err;
            case POP: throw err;
            case PRINT_INT: throw err;
            case PRINT_STR: throw err;
            case PUSH: throw err;
            case RET: throw err;
            case STORE: throw err;
            case SUB: return "sub";
            case LABEL: throw LanguageException.emitterError("Pseudo instruction has no translation");
        }
        throw LanguageException.emitterError("Bad instruction %s", op);
    }

    void arithBinary(OpCode op) throws LanguageException {
        final String fmt = """
        lw t0, (sp)    # %s
        lw t1, 4(sp)
        %s t0, t1, t0
        addi sp, sp, 4
        sw t0, (sp)
        """;
        textSection.append(String.format(fmt, op.value, translateOpCodeToRV32(op)));
    }

    void comparison(OpCode op) throws LanguageException{
        final String fmtOrder = """
        lw t0, (sp)    # %s
        lw t1, 4(sp)
        %s t0, t1, t0
        addi sp, sp, 4
        sw t0, (sp)
        """;
        final String fmtPartialOrder = """
        lw t0, (sp)    # %s
        lw t1, 4(sp)
        %s t0, t1, t0
        xori t0, t0, 1
        addi sp, sp, 4
        sw t0, (sp)
        """;
        final String fmtEquality = """
        lw t0, (sp)    # %s
        lw t1, 4(sp)
        sub t0, t1, t0
        seqz t0, t0
        addi sp, sp, 4
        sw t0, (sp)
        """;
        final String fmtNonEquality = """
        lw t0, (sp)    # %s
        lw t1, 4(sp)
        sub t0, t1, t0
        snez t0, t0
        addi sp, sp, 4
        sw t0, (sp)
        """;
        if(op == OpCode.GT){
            textSection.append(String.format(fmtOrder, op.value, "sgt"));
        }
        else if(op == OpCode.LT){
            textSection.append(String.format(fmtOrder, op.value, "slt"));
        }
        else if(op == OpCode.GT_EQ){
            textSection.append(String.format(fmtPartialOrder, op.value, "slt"));
        }
        else if(op == OpCode.LT_EQ){
            textSection.append(String.format(fmtPartialOrder, op.value, "sgt"));
        }
        else if(op == OpCode.EQUALS){
            textSection.append(String.format(fmtEquality, op.value));
        }
        else if(op == OpCode.NOT_EQUALS){
            textSection.append(String.format(fmtNonEquality, op.value));
        }
    }

    void labelSet(String label) throws LanguageException{
        final String fmt = """
        %s:
        """;

        textSection.append(String.format(fmt, label));
    }

    void jumpLabel(String label) throws LanguageException{
        final String fmt = """
        j %s
        """;

        textSection.append(String.format(fmt, label));
    }

    void branchZero(OpCode op,String label) throws LanguageException{
        final String fmt = """        
        lw t0, (sp)    # %s
        addi sp, sp, 4
        %s t0, %s
        """;
        textSection.append(String.format(fmt, "if_entry", translateOpCodeToRV32(op), label));
    }

    public void buildTextSection() throws LanguageException {
        textSection.append(".text\n");
        for(var inst : program.instructions){
            switch (inst.op) {
            /* Arithmetic */
            case ADD,SUB,MUL,DIV,MOD,BIT_AND,BIT_OR,BIT_SH_LEFT,BIT_SH_RIGHT,BIT_XOR:
                arithBinary(inst.op);
            break;
            case NEG:
            break;
            case BIT_NOT:
            break;

            /* Logic */
            case LOGIC_AND:
            break;
            case LOGIC_NOT:
            break;
            case LOGIC_OR:
            break;
            case NOT_ZERO:// NOTE Miguel: maybe not necessary
            break;
            case GT,LT,GT_EQ,LT_EQ,EQUALS,NOT_EQUALS:
                comparison(inst.op);
            break;

            /* Control Flow */
            case BRANCH_EQUAL_ZERO, BRANCH_NOT_ZERO:
                branchZero(inst.op, inst.labelName);
            break;
            case CALL:
            break;
            case JUMP:
                jumpLabel(inst.labelName);
            break;

            /* Memory */
            case PUSH:
                if(inst.labelName != null){
                    pushLabel(inst.labelName);
                }
                else {
                    pushImmediate(inst.word);
                }
            break;
            case POP:
            break;
            case LABEL:
                labelSet(inst.labelName);
            break;
            case DUP:
            break;
            case STORE:
                store();
            break;
            case LOAD:
                load();
            break;
            case RET:
            break;

            /* IO */
            case INPUT_INT:
                inputInt(inst.labelName);
            break;
            case INPUT_STR:
            break;
            case PRINT_INT:
                printInt();
                newLine();
            break;
            case PRINT_STR:
                printStr();
                newLine();
            break;

            default:
            throw new UnsupportedOperationException(inst.op.value);
            }

            
        }
    }



}
