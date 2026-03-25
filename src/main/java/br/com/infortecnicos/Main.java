package br.com.infortecnicos;

import br.com.infortecnicos.cli.CompilerCLI;
import br.com.infortecnicos.compiler.Antlr4Basic;
import br.com.infortecnicos.interfaces.CompilerEngine;
import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        CompilerEngine engine = new Antlr4Basic();
        CompilerCLI cli = new CompilerCLI(engine);

        int exitCode = new CommandLine(cli)
                .setExecutionExceptionHandler(new CompilerExecutionExceptionHandler())
                .execute(args);

        System.exit(exitCode);
    }

    private static class CompilerExecutionExceptionHandler
            implements CommandLine.IExecutionExceptionHandler {

        @Override
        public int handleExecutionException(Exception ex,
                                            CommandLine commandLine,
                                            CommandLine.ParseResult parseResult) throws Exception {

            commandLine.getErr().println("Erro durante a execução:");
            commandLine.getErr().println("  " + ex.getMessage());

            if (ex instanceof RuntimeException) {
                ex.printStackTrace(commandLine.getErr());
            }

            return CommandLine.ExitCode.SOFTWARE;
        }
    }
}