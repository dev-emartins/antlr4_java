package br.com.infortecnicos.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import br.com.infortecnicos.visitor.LanguageVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import br.com.infortecnicos.LanguageLexer;
import br.com.infortecnicos.LanguageParser;
import br.com.infortecnicos.gui.GuiVizualizerTask;
import br.com.infortecnicos.interfaces.CompilerEngine;

public class Antlr4Basic implements CompilerEngine {

    @Override
    public void execute(File input, File output, boolean verbose) throws IOException {

        var charStream = CharStreams.fromPath(input.toPath());

        var lexer = new LanguageLexer(charStream);
        var tokens = new CommonTokenStream(lexer);
        var parser = new LanguageParser(tokens);

        var tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Erro: O código contém " + parser.getNumberOfSyntaxErrors() + " erro(s) de sintaxe.");
            return;
        }

        var visitor = new LanguageVisitor();
        visitor.visit(tree);

        String generatedCode = visitor.getCode();

        Files.writeString(output.toPath(), generatedCode);

        System.out.println("✅ Código gerado com sucesso!");
        System.out.println("   Arquivo de saída: " + output.getAbsolutePath());

        if (verbose) {
            System.out.println("Abrindo visualizador de árvore sintática...");
            var guiTask = new GuiVizualizerTask(parser, tree);
            guiTask.run();
        }
    }
}