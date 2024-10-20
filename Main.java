import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class Main {
	public static void main(String[] args) {
		var sourceFile = "source.c3po";
		String source = "";
		try {
			source = new String(Files.readAllBytes(Paths.get(sourceFile)), StandardCharsets.UTF_8);
		} catch (IOException e){
			System.err.println(String.format("No such file or directory: '%s'", sourceFile));
			System.exit(1);
		}

		try {
			List<Token> tokens = null;
			tokens = Lexer.tokenize(source, true);
			var root = Parser.parse(tokens.toArray(new Token[tokens.size()]));
			// System.out.println(root);
		} catch(LanguageException e){
			System.err.println(e.toString());
			System.exit(1);
		}


		// for(var tok : tokens){
		// 	System.out.println(tok.toString());
		// }

		// 2 + (x - 1)

		// Expression exp = new BinaryExpr(
		// 	new PrimaryExpr(new Token(TokenType.INTEGER, "2", 2)),
		// 	TokenType.PLUS,
		// 	new BinaryExpr(
		// 		new PrimaryExpr(new Token(TokenType.ID, "x")),
		// 		TokenType.MINUS,
		// 		new PrimaryExpr(new Token(TokenType.INTEGER, "1", 1))
		// 	)
		// );
		//
		// System.out.println(exp);
	}
}
