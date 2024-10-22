import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;

public class Main {
	public static void main(String[] args) {
		var sourceFile = "source.c3po";
		String source = "{";

		try {
			source += new String(Files.readAllBytes(Paths.get(sourceFile)), StandardCharsets.UTF_8);
			source += "}";
		} catch (IOException e){
			System.err.println(String.format("No such file or directory: '%s'", sourceFile));
			System.exit(1);
		}

		try {
			List<Token> tokens = null;
			tokens = Lexer.tokenize(source, true);
			var root = Parser.parse(tokens.toArray(new Token[tokens.size()]));
			System.out.println(root);
			root.check(null);
			System.out.println(((Scope)root).env);
		} catch(LanguageException e){
			System.err.println(e.toString());
			System.exit(1);
		}
	}
}
