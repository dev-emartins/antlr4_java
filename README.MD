# compilador-pcode
Projeto da disciplina de Compiladores que implementa uma linguagem de programação simplificada com suporte a variáveis, estruturas de controle (if e while), expressões aritméticas e condicionais, convertendo o código para P-Code utilizando ANTLR e Java.

## Funcionalidades
- Declaração e atribuição de variáveis do tipo number
- Estruturas de controle (if e while)
- Expressões aritméticas e condicionais
- Entrada e saída de dados (input e print)
- Geração de código intermediário em P-Code

## Tecnologias
- Java
- ANTLR4
- Maven
- P-Code Machine

## Comandos Úteis

``` shell
# Compilar no Windows
.\mvnw clean install

# Compilar no Linux/Mac
./mvnw clean install

# Parse exemplo
java -jar target/compiler-1.1.0-jar-with-dependencies.jar -i fibonacci.expr -o fibonacci.pcode

# Executar na pcode com verbose por padrão
java -jar pcode.jar -i=fibonacci.pcode

# Executar na pcode sem verbose
java -jar pcode.jar -i=fibonacci.pcode -d=false

```