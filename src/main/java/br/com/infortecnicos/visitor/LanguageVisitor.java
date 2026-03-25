package br.com.infortecnicos.visitor;

import br.com.infortecnicos.LanguageBaseVisitor;
import br.com.infortecnicos.LanguageParser.*;
import br.com.infortecnicos.ast.MemoryMapper;
import br.com.infortecnicos.ast.ScopeControl;

public class LanguageVisitor extends LanguageBaseVisitor<Void> {

    private final ScopeControl scopes = new ScopeControl();
    private final MemoryMapper mapper = new MemoryMapper();
    private final StringBuilder code = new StringBuilder();
    private int labelCounter = 0;

    private String createLabel() {
        return "L" + labelCounter++;
    }

    public String getCode() {
        return code.toString();
    }

    // PROGRAMA
    @Override
    public Void visitProg(ProgContext ctx) {
        scopes.createScope();

        try {
            for (StatContext stat : ctx.stat()) {
                visit(stat);
            }
            code.append("hlt\n");

        } finally {
            scopes.dropScope();
        }

        return null;
    }

    // STATEMENTS
    @Override public Void visitDecVar(DecVarContext ctx)    { visit(ctx.varDecl()); return null; }
    @Override public Void visitDecAtrib(DecAtribContext ctx){ visit(ctx.assign()); return null; }
    @Override public Void visitDecIf(DecIfContext ctx)      { visit(ctx.ifStat()); return null; }
    @Override public Void visitDecWhile(DecWhileContext ctx){ visit(ctx.whileStat()); return null; }
    @Override public Void visitDecFor(DecForContext ctx)    { visit(ctx.forStat()); return null; }
    @Override public Void visitDecPrint(DecPrintContext ctx){ visit(ctx.printStat()); return null; }
    @Override public Void visitDecInp(DecInpContext ctx)    { visit(ctx.inputStat()); return null; }

    // DECLARAÇÃO DE VARIÁVEL
    @Override
    public Void visitVarDecl(VarDeclContext ctx) {
        if (scopes.getCurrentScope().exists(ctx.IDENT().getText())) {
            throw new RuntimeException(
                    "Variável '%s' já declarada na linha %d."
                            .formatted(ctx.IDENT().getText(), ctx.IDENT().getSymbol().getLine())
            );
        }

        int address = mapper.alloc();

        code.append("push $").append(address).append("\n");

        if (ctx.expr() != null) {
            visit(ctx.expr());
        } else {
            code.append("push 0\n");
        }

        scopes.getCurrentScope().insert(ctx.IDENT().getText(), address);

        code.append("sto\n");

        return null;
    }

    // ATRIBUIÇÃO
    @Override
    public Void visitAssign(AssignContext ctx) {
        var declOpt = scopes.lookup(ctx.IDENT().getText());
        if (declOpt.isEmpty()) {
            throw new RuntimeException("Variável '%s' não declarada na linha %d.".formatted(ctx.IDENT().getText(), ctx.IDENT().getSymbol().getLine()));
        }

        int address = declOpt.get().address();
        code.append("push $").append(address).append("\n");
        visit(ctx.expr());
        code.append("sto\n");
        code.append("push $").append(address).append("\n");
        code.append("lod\n");
        return null;
    }

    // PRINT
    @Override
    public Void visitPrintStat(PrintStatContext ctx) {
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText().replace("\"", "");
            code.append("push \"").append(str).append("\"\n");
        } else {
            visit(ctx.expr());
        }
        code.append("out\n");
        return null;
    }

    // INPUT
    @Override
    public Void visitInputStat(InputStatContext ctx) {
        var declOpt = scopes.lookup(ctx.IDENT().getText());
        if (declOpt.isEmpty()) {
            throw new RuntimeException("Variável '%s' não declarada na linha %d.".formatted(ctx.IDENT().getText(), ctx.IDENT().getSymbol().getLine()));
        }

        int address = declOpt.get().address();
        code.append("push $").append(address).append("\n");
        code.append("in\n");
        code.append("sto\n");
        return null;
    }

    // IF
    @Override
    public Void visitIfStat(IfStatContext ctx) {
        String elseLabel = createLabel();
        String endLabel = createLabel();

        visit(ctx.cond());
        code.append("fjp ").append(elseLabel).append("\n");

        visit(ctx.block(0));

        if (ctx.ELSE() != null) {
            code.append("ujp ").append(endLabel).append("\n");
            code.append(elseLabel).append(":\n");
            visit(ctx.block(1));
        } else {
            code.append(elseLabel).append(":\n");
        }
        code.append(endLabel).append(":\n");
        return null;
    }

    // WHILE
    @Override
    public Void visitWhileStat(WhileStatContext ctx) {
        String start = createLabel();
        String end = createLabel();

        code.append(start).append(":\n");
        visit(ctx.cond());
        code.append("fjp ").append(end).append("\n");

        visit(ctx.block());

        code.append("ujp ").append(start).append("\n");
        code.append(end).append(":\n");
        return null;
    }

    // FOR
    @Override
    public Void visitForStat(ForStatContext ctx) {
        String start = createLabel();
        String end = createLabel();

        visit(ctx.assign(0));

        code.append(start).append(":\n");
        visit(ctx.cond());
        code.append("fjp ").append(end).append("\n");

        visit(ctx.block());

        visit(ctx.assign(1));

        code.append("ujp ").append(start).append("\n");
        code.append(end).append(":\n");
        return null;
    }

    // BLOCO
    @Override
    public Void visitBlock(BlockContext ctx) {
        scopes.createScope();
        for (StatContext s : ctx.stat()) {
            visit(s);
        }
        scopes.dropScope();
        return null;
    }

    // EXPRESSÕES
    @Override
    public Void visitExp(ExpContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        code.append("pow\n");
        return null;
    }

    @Override
    public Void visitMulDiv(MulDivContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (ctx.getChild(1).getText().equals("/")) {
            code.append("div\n");
        } else {
            code.append("mul\n");
        }
        return null;
    }

    @Override
    public Void visitMaMe(MaMeContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        if (ctx.getChild(1).getText().equals("+")) {
            code.append("add\n");
        } else {
            code.append("sub\n");
        }
        return null;
    }

    @Override public Void visitUnit(UnitContext ctx)        { return visit(ctx.atom()); }
    @Override public Void visitDefNum(DefNumContext ctx)    { code.append("push ").append(ctx.NUMBER().getText()).append("\n"); return null; }

    @Override
    public Void visitDefIden(DefIdenContext ctx) {
        String varName = ctx.IDENT().getText();
        var tk = ctx.IDENT().getSymbol();

        var declOpt = scopes.lookup(varName);
        if (declOpt.isEmpty()) {
            throw new RuntimeException("Variável '%s' não declarada na linha %d.".formatted(varName, tk.getLine()));
        }
        int address = declOpt.get().address();
        code.append("push $").append(address).append("\n");
        code.append("lod\n");
        return null;
    }

    @Override public Void visitDefExp(DefExpContext ctx)    { return visit(ctx.expr()); }

    // CONDIÇÕES
    @Override
    public Void visitOrExpr(OrExprContext ctx) {
        if (ctx.andExpr().size() == 1) return visit(ctx.andExpr(0));

        String end = createLabel();
        for (int i = 0; i < ctx.andExpr().size(); i++) {
            visit(ctx.andExpr(i));
            if (i < ctx.andExpr().size() - 1) {
                code.append("fjp ").append(end).append("\n");
            }
        }
        code.append(end).append(":\n");
        code.append("push 1\n");
        return null;
    }

    @Override
    public Void visitAndExpr(AndExprContext ctx) {
        if (ctx.notExpr().size() == 1) return visit(ctx.notExpr(0));

        String end = createLabel();
        for (int i = 0; i < ctx.notExpr().size(); i++) {
            visit(ctx.notExpr(i));
            if (i < ctx.notExpr().size() - 1) {
                code.append("fjp ").append(end).append("\n");
            }
        }
        code.append(end).append(":\n");
        code.append("push 1\n");
        return null;
    }

    @Override public Void visitExpNo(ExpNoContext ctx)   { visit(ctx.notExpr()); code.append("not\n"); return null; }
    @Override public Void visitExpCom(ExpComContext ctx) { return visit(ctx.comparison()); }
    @Override public Void visitExpT(ExpTContext ctx)     { code.append("push 1\n"); return null; }
    @Override public Void visitExpF(ExpFContext ctx)     { code.append("push 0\n"); return null; }
    @Override public Void visitExpBol(ExpBolContext ctx) { return visit(ctx.cond()); }

    // COMPARAÇÃO
    @Override
    public Void visitComparison(ComparisonContext ctx) {
        visit(ctx.expr(0));
        visit(ctx.expr(1));

        String op = ctx.RELATIONAL().getText();
        switch (op) {
            case "<"  -> code.append("let\n");
            case ">"  -> code.append("grt\n");
            case "<=" -> code.append("lte\n");
            case ">=" -> code.append("gte\n");
            case "==" -> code.append("equ\n");
            case "!=" -> code.append("neq\n");
            default   -> throw new RuntimeException("Operador relacional desconhecido: " + op);
        }
        return null;
    }
}