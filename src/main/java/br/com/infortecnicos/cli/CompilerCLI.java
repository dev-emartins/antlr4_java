package br.com.infortecnicos.cli;

import br.com.infortecnicos.interfaces.CompilerEngine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
        name = "compiler",
        mixinStandardHelpOptions = true,
        version = "1.1",
        description = "Compilador da linguagem customizada → PCode"
)
public class CompilerCLI implements Callable<Integer> {

    private final CompilerEngine engine;

    public CompilerCLI(CompilerEngine engine) {
        this.engine = engine;
    }

    @Parameters(index = "0", description = "Arquivo fonte (.expr ou .lang)", arity = "0..1")
    private File inputFile;

    @Option(names = {"-i", "--input"}, description = "Arquivo fonte para compilação", required = false)
    private File inputOption;

    @Option(names = {"-o", "--output"}, description = "Arquivo de saída PCode", required = true)
    private File output;

    @Option(names = {"-v", "--verbose"}, description = "Modo verbose (visualizador de árvore)", defaultValue = "false")
    private boolean verbose;

    @Override
    public Integer call() {
        File input = inputOption != null ? inputOption : inputFile;

        if (input == null || !input.exists()) {
            System.err.println("❌ Erro: Arquivo de entrada não encontrado!");
            System.err.println("   Use: java -jar compiler.jar -i seu_arquivo.expr -o saida.pcode");
            System.err.println("   Ou: java -jar compiler.jar seu_arquivo.expr -o saida.pcode");
            return 1;
        }

        try {
            engine.execute(input, output, verbose);
            System.out.println("✅ PCode gerado com sucesso!");
            System.out.println("   Saída: " + output.getAbsolutePath());
            return 0;
        } catch (Exception e) {
            System.err.println("❌ Erro durante a execução:");
            System.err.println("   " + e.getMessage());
            return 1;
        }
    }
}