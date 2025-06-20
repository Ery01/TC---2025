package com.compilador;

import org.antlr.v4.runtime.tree.TerminalNode;
import com.compilador.CompiladorParser.*;
import com.compilador.EvaluadorExpresiones;
import com.compilador.TablaSimbolos;
import com.compilador.TablaSimbolos.Simbolo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack; // ¡Nuevo import!

public class Caminante extends CompiladorBaseVisitor<Object> { // Cambiado a Object porque puede devolver valores evaluados

    private TablaSimbolos tablaSimbolos;
    private List<String> errores;
    private List<String> warnings;

    // Pila de ámbitos para el Visitor.
    // Para simplificar, la instanciamos aquí y la mantenemos manualmente para las visitas.
    private Stack<String> ambitoStack;


    public Caminante(TablaSimbolos tablaSimbolos, List<String> errores, List<String> warnings) {
        this.tablaSimbolos = tablaSimbolos;
        this.errores = errores;
        this.warnings = warnings;
        this.ambitoStack = new Stack<>();
        this.ambitoStack.push("global"); // Iniciar en el ámbito global
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
        ambitoStack.push("bloque_" + System.nanoTime()); // Usar System.nanoTime() para un ID único si no tienes un contador global
        Object result = super.visitBloque(ctx);
        ambitoStack.pop();
        return result;
    }

    @Override
    public Object visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx) {
        EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
        Object resultadoCondicion = evaluador.visit(ctx.expresion());

        // Solo verificar si la condición es una constante booleana
        if (resultadoCondicion instanceof Boolean) {
            boolean condicion = (Boolean) resultadoCondicion;
            if (!condicion) {
                agregarWarning("Advertencia en línea " + ctx.getStart().getLine() + ": Condición falsa constante en bucle `while`. Este bucle nunca se ejecutará.");
            } else {
                agregarWarning("Advertencia en línea " + ctx.getStart().getLine() + ": Condición verdadera constante en bucle `while`. Posible bucle infinito detectado.");
            }
        }
        // Continuar visitando el cuerpo del while para análisis posteriores
        return visit(ctx.bloque());
    }

    @Override
    public Object visitSentenciaFor(CompiladorParser.SentenciaForContext ctx) {
        EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);

        if (ctx.inicializacionDeclaracion != null) {
            visit(ctx.inicializacionDeclaracion);
        } else if (ctx.inicializacionExpresion != null) {
            visit(ctx.inicializacionExpresion);
        }

        Object resultadoCondicion = null;
        if (ctx.condicion != null) {
            resultadoCondicion = evaluador.visit(ctx.condicion);
        }

        if (resultadoCondicion instanceof Boolean) {
            boolean condicion = (Boolean) resultadoCondicion;
            if (!condicion) {
                agregarWarning("Advertencia en línea " + ctx.getStart().getLine() + ": Condición falsa constante en bucle `for`. Este bucle nunca se ejecutará.");
            } else {
                agregarWarning("Advertencia en línea " + ctx.getStart().getLine() + ": Condición verdadera constante en bucle `for`. Posible bucle infinito detectado.");
            }
        }

        if (ctx.actualizacion != null) {
            visit(ctx.actualizacion);
        }

        return visit(ctx.bloque());
    }

    @Override
    public Object visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        // Si Caminante necesita el tipo o valor, busca en la tabla:
        String nombre = ctx.ID().getText();
        Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);
        if (simbolo == null) {
            // Solo lo manejamos para evitar NullPointerException si la ejecución continúa.
            // agregarError("Error (Caminante): variable '" + nombre + "' no declarada. Línea " + ctx.getStart().getLine());
            return null; // O un valor que indique error
        }
        return null; // No retornamos nada, solo visitamos
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
