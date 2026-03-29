package br.com.infortecnicos.compiler;

import br.com.infortecnicos.LanguageLexer;
import br.com.infortecnicos.LanguageParser;
import br.com.infortecnicos.visitor.LanguageVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do LanguageVisitor - Gerador de Código")
public class LanguageVisitorTest {

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

        assertNotNull(code);
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 10"));
        assertTrue(code.contains("sto"));
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

        assertNotNull(code);
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 5"));
        assertTrue(code.contains("sto"));
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 20"));
        assertTrue(code.contains("sto"));
    }

    @Test
    @DisplayName("Deve gerar código para expressão aritmética")
    void deveGerarCodigoParaExpressaoAritmetica() {
        String source = """
            var resultado = 2 + 3 * 4;
            """;

        String code = compile(source);

        assertNotNull(code);
        assertTrue(code.contains("push 2"));
        assertTrue(code.contains("push 3"));
        assertTrue(code.contains("push 4"));
        assertTrue(code.contains("mul"));
        assertTrue(code.contains("add"));
    }

    @Test
    @DisplayName("Deve gerar loop para exponenciação simples")
    void deveGerarExponenciacaoSimples() {
        String source = """
        var x = 10^2;
        print(x);
        """;

        String code = compile(source);

        assertNotNull(code);

        assertTrue(code.contains("mul"));
        assertTrue(code.contains("fjp"));
        assertTrue(code.contains("ujp"));

        assertTrue(code.contains("push $"));
        assertTrue(code.contains("sto"));
        assertTrue(code.contains("lod"));

        assertTrue(code.contains("out"));
        assertTrue(code.contains("hlt"));
    }

    @Test
    @DisplayName("Deve gerar duas exponenciações (associatividade à direita)")
    void deveGerarExponenciacaoEncadeada() {
        String source = """
        var x = 2^5^3;
        print(x);
        """;

        String code = compile(source);

        assertNotNull(code);

        int countMul = code.split("mul").length - 1;
        assertTrue(countMul >= 2);

        int countFjp = code.split("fjp").length - 1;
        assertTrue(countFjp >= 2);

        int countUjp = code.split("ujp").length - 1;
        assertTrue(countUjp >= 2);
    }

    @Test
    @DisplayName("Deve gerar código para print de expressão")
    void deveGerarCodigoParaPrint() {
        String source = """
            print(10 + 5);
        """;

        String code = compile(source);

        assertNotNull(code);
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

        assertNotNull(code);
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 0"));
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

        assertNotNull(code);
        assertTrue(code.contains("fjp L0"));
        assertTrue(code.contains("L0:"));
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("lod"));
        assertTrue(code.contains("push 5"));
        assertTrue(code.contains("grt"));
    }

    @Test
    @DisplayName("Deve gerar código para if com else")
    void deveGerarCodigoParaIfElse() {
        String source = """
            var x = 10;
            if (x < 5) {
                print(1);
            } else {
                print(2);
            }
            """;

        String code = compile(source);

        assertNotNull(code);

        assertTrue(code.contains("fjp L0"));
        assertTrue(code.contains("ujp L1"));
        assertTrue(code.contains("L0:"));
        assertTrue(code.contains("L1:"));
        assertTrue(code.contains("push 1"));
        assertTrue(code.contains("push 2"));
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

        assertNotNull(code);
        assertTrue(code.contains("L0:"));
        assertTrue(code.contains("fjp L1"));
        assertTrue(code.contains("ujp L0"));
    }

    @Test
    @DisplayName("Deve gerar código para for")
    void deveGerarCodigoParaFor() {
        String source = """
            var i = 0;
            for (i = 0; i < 3; i = i + 1) {
                print(i);
            }
        """;

        String code = compile(source);

        assertNotNull(code);
        assertTrue(code.contains("push $0"));
        assertTrue(code.contains("push 0"));
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
        String source = "print(x);";

        Exception exception = assertThrows(RuntimeException.class, () -> compile(source));

        assertTrue(exception.getMessage().contains("não declarada") ||
                exception.getMessage().contains("declarada") ||
                exception.getMessage().contains("undefined"));
    }

    // METODO AUXILIAR
    private String compile(String source) {
        try {
            Path tempFile = Files.createTempFile("test", ".lang");
            Files.writeString(tempFile, source);

            var charStream = CharStreams.fromPath(tempFile);
            var lexer = new LanguageLexer(charStream);
            var tokens = new CommonTokenStream(lexer);
            var parser = new LanguageParser(tokens);

            var tree = parser.prog();

            if (parser.getNumberOfSyntaxErrors() > 0) {
                fail("Erro de sintaxe no código de teste: " + parser.getNumberOfSyntaxErrors());
            }

            visitor = new LanguageVisitor();
            visitor.visit(tree);

            Files.deleteIfExists(tempFile);
            return visitor.getCode();

        } catch (IOException e) {
            fail("Erro ao criar arquivo temporário: " + e.getMessage());
            return "";
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            fail("Erro inesperado: " + e.getMessage());
            return "";
        }
    }
}