class LanguageException extends Exception {
	CompilerStage stage;

	LanguageException(CompilerStage stage, String msg){
		super(msg);
		this.stage = stage;
	}

	public String toString(){
		return String.format("%s error: %s", stage.value, super.toString());
	}
};
