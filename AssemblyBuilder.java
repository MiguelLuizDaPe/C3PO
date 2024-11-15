class AssemblyBuilder {
    StringBuilder readOnlySection;
    StringBuilder dataSection;
    StringBuilder textSection;

    public AssemblyBuilder() {
        readOnlySection = new StringBuilder();
        dataSection = new StringBuilder();
        textSection = new StringBuilder();
    }

    public String build(){
        var output = new StringBuilder();
        output.append(readOnlySection.toString());
        output.append(dataSection.toString());
        output.append(textSection.toString());
        return "";
    }
}
