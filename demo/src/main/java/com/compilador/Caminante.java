package com.compilador;

import com.compilador.CompiladorParser.*;
import com.compilador.TablaSimbolos.Simbolo;

import java.util.List;
import java.util.Stack;

public class Caminante extends CompiladorBaseVisitor<Object> {

    public static final String RESET = "\u001B[0m";
    public static final String YELLOW = "\u001B[33m";

    private TablaSimbolos tablaSimbolos;
    private List<String> errores;
    private List<String> warnings;

    private Stack<String> ambitoStack;


    public Caminante(TablaSimbolos tablaSimbolos, List<String> errores, List<String> warnings) {
        this.tablaSimbolos = tablaSimbolos;
        this.errores = errores;
        this.warnings = warnings;
        this.ambitoStack = new Stack<>();
        this.ambitoStack.push("global");
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    public void agregarWarning(String warning) {
        warnings.add(warning);
    }

    public List<String> getErrores() {
        return errores;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    // ====================================================================================
    // Métodos `visit` de Caminante para análisis de flujo de control o evaluación
    // ====================================================================================

    @Override
    public Object visitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        ambitoStack.push(ctx.ID().getText());
        Object result = super.visitDeclaracionFuncion(ctx);
        ambitoStack.pop();
        return result;
    }

    @Override
    public Object visitBloque(BloqueContext ctx) {
        ambitoStack.push("bloque_" + System.nanoTime());
        Object result = super.visitBloque(ctx);
        ambitoStack.pop();
        return result;
    }

    @Override
    public Object visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx) {
        EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
        Object resultadoCondicion = evaluador.visit(ctx.expresion());

        if (resultadoCondicion instanceof Boolean) {
            boolean condicion = (Boolean) resultadoCondicion;
            if (!condicion) {
                agregarWarning(YELLOW + "Advertencia en línea " + ctx.getStart().getLine() + ": Condición falsa constante en bucle `while`. Este bucle nunca se ejecutará." + RESET);
            } else {
                agregarWarning(YELLOW + "Advertencia en línea " + ctx.getStart().getLine() + ": Condición verdadera constante en bucle `while`. Posible bucle infinito detectado." + RESET);
            }
        }
        return visit(ctx.bloque());
    }

    @Override
    public Object visitSentenciaFor(CompiladorParser.SentenciaForContext ctx) {
        EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);

        // 1. Inicializacion (ctx.forInit ahora es de tipo ForInitPartContext)
        if (ctx.forInit != null) {
            visit(ctx.forInit);
        }

        Object resultadoCondicion = null;
        // 2. Condicion (ctx.forCond ahora es de tipo ExpresionContext)
        if (ctx.forCond != null) {
            resultadoCondicion = evaluador.visit(ctx.forCond);
        }

        if (resultadoCondicion instanceof Boolean) {
            boolean condicion = (Boolean) resultadoCondicion;
            if (!condicion) {
                agregarWarning(YELLOW + "Advertencia en línea " + ctx.getStart().getLine() + ": Condición falsa constante en bucle `for`. Este bucle nunca se ejecutará." + RESET);
            } else {
                agregarWarning(YELLOW + "Advertencia en línea " + ctx.getStart().getLine() + ": Condición verdadera constante en bucle `for`. Posible bucle infinito detectado." + RESET);
            }
        }

        // 3. Actualizacion (ctx.forUpdate ahora es de tipo ForUpdatePartContext)
        if (ctx.forUpdate != null) {
            visit(ctx.forUpdate);
        }

        // 4. Bloque del bucle
        return visit(ctx.bloque());
    }


    @Override
    public Object visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);
        if (simbolo == null) {
            return null;
        }
        return null;
    }

    @Override
    public Object visitAsignacion(AsignacionContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public Object visitExpBinaria(ExpBinariaContext ctx) {
        return super.visitExpBinaria(ctx);
    }
}
