import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if(args.length < 2){
            System.err.println(
                "Usage: c3po <command> <file>\n"+
                "\nAvailable Commands:\n"+
                "    compile        Compile to risc-v assembly\n"+
                "    check          Type-check only\n"+
                "    parse          Parse only");
            System.exit(1);
        }

        var option = args[0];
        var file = args[1];
        var parseOnly = false;
        var checkOnly = false;

        if(option.equals("parse")){
            parseOnly = true;
        } else if(option.equals("check")){
            checkOnly = true;
        } else if(option.equals("compile")){
            /* Nothing */
        } else {
            System.err.println("Unkown command: '"+ option + "'");
            System.exit(1);
        }

        try {
            var source = "\n{\n" + new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8) + "\n}\n";

            var tokens = Lexer.tokenize(source, true);
            var ast = Parser.parse(tokens.toArray(new Token[tokens.size()]));//
			((Scope)ast).initAsGlobalScope();
            if(parseOnly){
                return;
            }

            ast.check(null);
            if(checkOnly){
                return;
            }

            var root = (Scope)ast;
            var builder = new IRBuilder();

            root.genIR(root.parent, builder);
            var prog = builder.build();
            for(var i : prog.instructions){
                System.out.println(i); // fodase
            }

            System.out.println(((Scope)ast).env);
        }
        catch(IOException e){
            System.err.println("Could not read file '" + file + "'");
            System.exit(1);
        }
        catch(LanguageException e){
            System.err.println("Compilation failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
