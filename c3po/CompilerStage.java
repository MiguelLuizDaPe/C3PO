package c3po;

enum CompilerStage {
	LEXER("lexer"),
	PARSER("parser"),
	CHECKER("checker"),
	EMITTER("emitter");

	public String value;

	CompilerStage(String value){
		this.value = value;
	}
};
