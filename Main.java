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

		List<Token> tokens = null;

		try {
			tokens = Lexer.tokenize(source, false);
		} catch(LanguageException e){
			System.err.println(e.toString());
			System.exit(1);
		}

		for(var tok : tokens){
			System.out.println(tok.toString());
		}
	}
}
