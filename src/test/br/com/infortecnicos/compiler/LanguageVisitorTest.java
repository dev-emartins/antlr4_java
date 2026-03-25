package br.com.infortecnicos.compiler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do LanguageVisitor - Gerador de Código")
class LanguageVisitorTest {

    private LanguageVisitor visitor;

    @BeforeEach
    void setUp() {
        visitor = new LanguageVisitor();
    }

    // TESTES BÁSICOS

    @Test
    @DisplayName("Deve gerar código para declaração simples de variável")
    void deveGerarCodigoParaDeclaracaoSimples() {
        String source = """
            var x = 10;
            """;

        String code = compile(source);

        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 10"));
        assertTrue(code.contains("sto"));
        assertTrue(code.contains("out"));
        assertTrue(code.contains("hlt"));
    }

    @Test
    @DisplayName("Deve gerar código para atribuição")
    void deveGerarCodigoParaAtribuicao() {
        String source = """
            var x = 5;
            x = 20;
            """;

        String code = compile(source);

        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 5"));
        assertTrue(code.contains("sto"));           // declaração
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 20"));
        assertTrue(code.contains("sto"));           // atribuição
    }

    @Test
    @DisplayName("Deve gerar código para expressão aritmética")
    void deveGerarCodigoParaExpressaoAritmetica() {
        String source = """
            var resultado = 2 + 3 * 4;
            """;

        String code = compile(source);

        assertTrue(code.contains("push 2"));
        assertTrue(code.contains("push 3"));
        assertTrue(code.contains("push 4"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("add"));
    }

    @Test
    @DisplayName("Deve gerar código para print de expressão")
    void deveGerarCodigoParaPrint() {
        String source = """
            print(10 + 5);
            """;

        String code = compile(source);

        assertTrue(code.contains("push 10"));
        assertTrue(code.contains("push 5"));
        assertTrue(code.contains("add"));
        assertTrue(code.contains("out"));
    }

    @Test
    @DisplayName("Deve gerar código para input")
    void deveGerarCodigoParaInput() {
        String source = """
            var numero;
            input(numero);
            """;

        String code = compile(source);

        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 0"));   // inicialização default
        assertTrue(code.contains("sto"));
        assertTrue(code.contains("in"));
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("sto"));
    }

    // ESTRUTURAS DE CONTROLE

    @Test
    @DisplayName("Deve gerar código para if simples")
    void deveGerarCodigoParaIf() {
        String source = """
            var x = 10;
            if (x > 5) {
                print(x);
            }
            """;

        String code = compile(source);

        assertTrue(code.contains("fjp L"));
        assertTrue(code.contains("ujp L"));
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("lod"));
        assertTrue(code.contains("push 5"));
        assertTrue(code.contains("grt"));
    }

    @Test
    @DisplayName("Deve gerar código para while")
    void deveGerarCodigoParaWhile() {
        String source = """
            var i = 0;
            while (i < 5) {
                print(i);
                i = i + 1;
            }
            """;

        String code = compile(source);

        assertTrue(code.contains("L0:"));
        assertTrue(code.contains("fjp L1"));
        assertTrue(code.contains("ujp L0"));
    }

    @Test
    @DisplayName("Deve gerar código para for")
    void deveGerarCodigoParaFor() {
        String source = """
            for (var i = 0; i < 3; i = i + 1) {
                print(i);
            }
            """;

        String code = compile(source);

        assertTrue(code.contains("push $0"));     // alocação do i
        assertTrue(code.contains("push 0"));      // init
        assertTrue(code.contains("fjp L"));
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("lod"));
        assertTrue(code.contains("push 1"));
        assertTrue(code.contains("add"));
    }

    // TESTE DE ERRO

    @Test
    @DisplayName("Deve lançar exceção ao usar variável não declarada")
    void deveLancarExcecaoVariavelNaoDeclarada() {
        String source = "print(x);";   // x não foi declarada

        Exception exception = assertThrows(RuntimeException.class, () -> {
            compile(source);
        });

        assertTrue(exception.getMessage().contains("não declarada"));
    }

    // MÉTODO AUXILIAR

    private String compile(String source) {
        try {
            // Cria um arquivo temporário com o código fonte
            Path tempFile = Files.createTempFile("test", ".lang");
            Files.writeString(tempFile, source);

            // Executa o parser + visitor
            var charStream = org.antlr.v4.runtime.CharStreams.fromPath(tempFile);
            var lexer = new LanguageLexer(charStream);
            var tokens = new org.antlr.v4.runtime.CommonTokenStream(lexer);
            var parser = new LanguageParser(tokens);

            var tree = parser.prog();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                fail("Erro de sintaxe no código de teste: " + parser.getNumberOfSyntaxErrors());
            }

            visitor = new LanguageVisitor();  // novo visitor para cada teste
            visitor.visit(tree);

            Files.deleteIfExists(tempFile);
            return visitor.getCode();

        } catch (IOException e) {
            fail("Erro ao criar arquivo temporário: " + e.getMessage());
            return "";
        }
    }
}