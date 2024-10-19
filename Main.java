import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class Main {
	public static void main(String[] args) {
		// var sourceFile = "source.c3po";
		// String source = "";
		// try {
		// 	source = new String(Files.readAllBytes(Paths.get(sourceFile)), StandardCharsets.UTF_8);
		// } catch (IOException e){
		// 	System.err.println(String.format("No such file or directory: '%s'", sourceFile));
		// 	System.exit(1);
		// }
		//
		// List<Token> tokens = null;
		//
		// try {
		// 	tokens = Lexer.tokenize(source, false);
		// } catch(LanguageException e){
		// 	System.err.println(e.toString());
		// 	System.exit(1);
		// }
		//
		// for(var tok : tokens){
		// 	System.out.println(tok.toString());
		// }

		// 2 + (x - 1)

		Expression exp = new BinaryExpr(
			new PrimaryExpr(new Token(TokenType.INTEGER, "2", 2)),
			TokenType.PLUS,
			new BinaryExpr(
				new PrimaryExpr(new Token(TokenType.ID, "x")),
				TokenType.MINUS,
				new PrimaryExpr(new Token(TokenType.INTEGER, "1", 1))
			)
		);

		System.out.println(exp);
	}
}
