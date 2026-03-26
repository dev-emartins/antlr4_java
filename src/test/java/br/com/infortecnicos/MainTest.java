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
        // Este método é apenas para garantir que a suite seja executada
        // Os testes são executados automaticamente pelas anotações @Suite
        System.out.println("Executando suite de testes do compilador...");
    }
}