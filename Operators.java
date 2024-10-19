class Operators {
	record OpInfo(TokenType op, int lbp, int rbp){}

	static final OpInfo[] infixOperators = {
		new OpInfo(TokenType.PLUS,   50, 51),
		new OpInfo(TokenType.MINUS,  50, 51),
		new OpInfo(TokenType.STAR,   60, 61),
		new OpInfo(TokenType.SLASH,  60, 61),
		new OpInfo(TokenType.MODULO, 60, 61),

		new OpInfo(TokenType.BIT_AND,      60, 61),
		new OpInfo(TokenType.BIT_OR,       50, 51),
		new OpInfo(TokenType.BIT_SH_LEFT,  60, 61),
		new OpInfo(TokenType.BIT_SH_RIGHT, 60, 61),

		new OpInfo(TokenType.LOGIC_AND, 20, 21),
		new OpInfo(TokenType.LOGIC_OR,  10, 11),

		new OpInfo(TokenType.EQ,    30, 31),
		new OpInfo(TokenType.NEQ,   30, 31),
		new OpInfo(TokenType.GT,    30, 31),
		new OpInfo(TokenType.GT_EQ, 30, 31),
		new OpInfo(TokenType.LT,    30, 31),
		new OpInfo(TokenType.LT_EQ, 30, 31),
	};

	static final OpInfo[] prefixOperators = {
		new OpInfo(TokenType.PLUS,      0, 90),
		new OpInfo(TokenType.MINUS,     0, 90),
		new OpInfo(TokenType.BIT_NOT,   0, 90),
		new OpInfo(TokenType.LOGIC_NOT, 0, 80),
	};

	static final OpInfo[] postfixOperators = {
		new OpInfo(TokenType.SQUARE_OPEN, 110, 0),
		new OpInfo(TokenType.PAREN_OPEN,  100, 0),
	};

	record Power(int left, int right){
		public int lbp(){ return left; }
		public int rbp(){ return right; }
	}

	static Power infixPower(TokenType t) {
		for(var entry : infixOperators){
			if(entry.op == t){
				return new Power(entry.lbp, entry.rbp);
			}
		}
		return null;
	}

	static Power prefixPower(TokenType t) {
		for(var entry : prefixOperators){
			if(entry.op == t){
				return new Power(entry.lbp, entry.rbp);
			}
		}
		return null;
	}

	static Power postfixPower(TokenType t) {
		for(var entry : postfixOperators){
			if(entry.op == t){
				return new Power(entry.lbp, entry.rbp);
			}
		}
		return null;
	}
}
