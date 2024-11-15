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

    public String build(){
        var output = new StringBuilder();
        output.append(readOnlySection.toString());
        output.append(dataSection.toString());
        output.append(textSection.toString());
        return output.toString();
    }
}
