class Operators {
	record OpInfo(TokenType op, int lbp, int rbp){}

	static final OpInfo[] infixOperators = {
		new OpInfo(TokenType.PLUS,   50, 51),
		new OpInfo(TokenType.MINUS,  50, 51),
		new OpInfo(TokenType.STAR,   60, 61),
		new OpInfo(TokenType.SLASH,  60, 61),
		new OpInfo(TokenType.MODULO, 60, 61),

		new OpInfo(TokenType.BIT_AND,      60, 61),
		new OpInfo(TokenType.TILDE,        60, 61),
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
		new OpInfo(TokenType.TILDE,     0, 90),
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

	record TypeCompat(TokenType op, PrimitiveType[] accept){}

	public static boolean binaryCompatible(TokenType tk, PrimitiveType type){
		for(int i = 0; i < binaryCompat.length; i++){
			var entry = binaryCompat[i];
			if(tk == entry.op){
				for(var acceptType : entry.accept){
					if(type == acceptType){
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean unaryCompatible(TokenType tk, PrimitiveType type){
		for(int i = 0; i < unaryCompat.length; i++){
			var entry = unaryCompat[i];
			if(tk == entry.op){
				for(var acceptType : entry.accept){
					if(type == acceptType){
						return true;
					}
				}
			}
		}
		return false;
	}

	static private final PrimitiveType[] arithTypes = {
		PrimitiveType.INT,
		PrimitiveType.FLOAT,
	};

	static private final PrimitiveType[] bitTypes = {
		PrimitiveType.INT,
	};

	static private final PrimitiveType[] comparableTypes = {
		PrimitiveType.INT,
		PrimitiveType.FLOAT,
		PrimitiveType.CHAR,
		PrimitiveType.STRING, // TODO: Emit special assembly for this shit
		PrimitiveType.BOOL,
	};

	static private final PrimitiveType[] ordenableTypes = {
		PrimitiveType.INT,
		PrimitiveType.FLOAT,
		PrimitiveType.CHAR,
	};

	static private final PrimitiveType[] logicTypes = {
		PrimitiveType.BOOL,
	};

	static private final TypeCompat[] binaryCompat = {
		new TypeCompat(TokenType.PLUS, arithTypes),
		new TypeCompat(TokenType.MINUS, arithTypes),
		new TypeCompat(TokenType.STAR, arithTypes),
		new TypeCompat(TokenType.SLASH, arithTypes),

		new TypeCompat(TokenType.MODULO, bitTypes),

		new TypeCompat(TokenType.TILDE, bitTypes),
		new TypeCompat(TokenType.BIT_AND, bitTypes),
		new TypeCompat(TokenType.BIT_OR, bitTypes),
		new TypeCompat(TokenType.BIT_SH_LEFT, bitTypes),
		new TypeCompat(TokenType.BIT_SH_RIGHT, bitTypes),

		new TypeCompat(TokenType.EQ, comparableTypes),
		new TypeCompat(TokenType.NEQ, comparableTypes),

		new TypeCompat(TokenType.GT, ordenableTypes),
		new TypeCompat(TokenType.LT, ordenableTypes),
		new TypeCompat(TokenType.GT_EQ, ordenableTypes),
		new TypeCompat(TokenType.LT_EQ, ordenableTypes),

		new TypeCompat(TokenType.LOGIC_AND, logicTypes),
		new TypeCompat(TokenType.LOGIC_OR, logicTypes),
	};

	static private final TypeCompat[] unaryCompat = {
		new TypeCompat(TokenType.PLUS, arithTypes),
		new TypeCompat(TokenType.MINUS, arithTypes),

		new TypeCompat(TokenType.TILDE, bitTypes),

		new TypeCompat(TokenType.LOGIC_NOT, logicTypes),
	};
}
