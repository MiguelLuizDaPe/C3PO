class LanguageException extends Exception {
	CompilerStage stage;

	LanguageException(CompilerStage stage, String msg){
		super(msg);
		this.stage = stage;
	}

	static void lexerError(String fmt, Object... args) throws LanguageException {
		throw new LanguageException(CompilerStage.LEXER, String.format(fmt, args));
	}

	static void parserError(String fmt, Object... args) throws LanguageException {
		throw new LanguageException(CompilerStage.PARSER, String.format(fmt, args));
	}

	static void checkerError(String fmt, Object... args) throws LanguageException {
		throw new LanguageException(CompilerStage.CHECKER, String.format(fmt, args));
	}

	static void emitterError(String fmt, Object... args) throws LanguageException {
		throw new LanguageException(CompilerStage.EMITTER, String.format(fmt, args));
	}

	public String toString(){
		return String.format("%s error: %s", stage.value, super.toString());
	}
};
