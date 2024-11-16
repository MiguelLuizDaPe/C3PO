package c3po;

public class LanguageException extends Exception {
	CompilerStage stage;

	LanguageException(CompilerStage stage, String msg){
		super(msg);
		this.stage = stage;
	}

	static LanguageException lexerError(String fmt, Object... args) throws LanguageException {
		return new LanguageException(CompilerStage.LEXER, String.format(fmt, args));
	}

	static LanguageException parserError(String fmt, Object... args) throws LanguageException {
		return new LanguageException(CompilerStage.PARSER, String.format(fmt, args));
	}

	static LanguageException checkerError(String fmt, Object... args) throws LanguageException {
		return new LanguageException(CompilerStage.CHECKER, String.format(fmt, args));
	}

	static LanguageException emitterError(String fmt, Object... args) throws LanguageException {
		return new LanguageException(CompilerStage.EMITTER, String.format(fmt, args));
	}

	public String toString(){
		return String.format("%s error: %s", stage.value, super.toString());
	}
};
