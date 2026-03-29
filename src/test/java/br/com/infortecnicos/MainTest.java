package br.com.infortecnicos;

import br.com.infortecnicos.compiler.LanguageVisitorTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        LanguageVisitorTest.class
})
@DisplayName("Suite de Testes do Compilador")
public class MainTest {

    @Test
    @DisplayName("Executando todos os testes do compilador")
    void runAllTests() {
        System.out.println("Executando suite de testes do compilador...");
    }
}