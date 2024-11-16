class AssemblyBuilder {
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

        var output = new StringBuilder();
        output.append(readOnlySection.toString());
        output.append("\n");
        output.append(dataSection.toString());
        output.append("\n");
        output.append(textSection.toString());
        output.append("\n");
        return output.toString();
    }

    void pushImmediate(int word){
        final String fmt = """
        li t0, %d        # %s
        addi sp, sp, -4
        sw t0, (sp)
        """;
        textSection.append(String.format(fmt, word, "Push " + word));
    }

    void pushLabel(String label){
        final String fmt = """
        la t0, %s       # %s
        addi sp, sp, -4
        sw t0, (sp)
        """;
        textSection.append(String.format(fmt, label, "Push " + label));
    }

    public static String translateOpCodeToRV32(OpCode op) throws LanguageException {
        var err = LanguageException.emitterError("No direct translation to " + op.value);
        switch (op) {
            case ADD: return "add";
            case BIT_AND: return "and";
            case BIT_NOT: throw err;
            case BIT_OR: return "or";
            case BIT_SH_LEFT: return "sll";
            case BIT_SH_RIGHT: return "srl";
            case BIT_XOR: return "xor";
            case BRANCH: throw err;
            case CALL: throw err;
            case DIV: return "div";
            case DUP: throw err;
            case EQUALS: throw err;
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

    public void buildTextSection() throws LanguageException {
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
            case NOT_EQUALS:
            break;
            case NOT_ZERO:// NOTE Miguel: maybe not necessary
            break;

            /* Control Flow */
            case BRANCH:
            break;
            case CALL:
            break;
            case JUMP:
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
            case EQUALS:
            break;
            case LABEL:
            break;
            case STORE:
            break;
            case DUP:
            break;
            case LOAD:
            break;
            case RET:
            break;

            /* IO */
            case INPUT_INT:
            break;
            case INPUT_STR:
            break;
            case PRINT_INT:
            break;
            case PRINT_STR:
            break;

            default:
            throw new UnsupportedOperationException(inst.op.value);
            }

            
        }
    }



}
