class LanguageException extends Exception {
	CompilerStage stage;

	LanguageException(CompilerStage stage, String msg){
		super(msg);
		this.stage = stage;
	}

	static void lexerError(String msg) throws LanguageException {
		throw new LanguageException(CompilerStage.LEXER, msg);
	}

	static void parserError(String msg) throws LanguageException {
		throw new LanguageException(CompilerStage.PARSER, msg);
	}

	static void checkerError(String msg) throws LanguageException {
		throw new LanguageException(CompilerStage.CHECKER, msg);
	}

	static void emitterError(String msg) throws LanguageException {
		throw new LanguageException(CompilerStage.EMITTER, msg);
	}

	public String toString(){
		return String.format("%s error: %s", stage.value, super.toString());
	}
};
