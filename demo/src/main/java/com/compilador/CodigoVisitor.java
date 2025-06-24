package com.compilador;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.Map;


public class CodigoVisitor extends CompiladorBaseVisitor<String> {

    private TablaSimbolos tablaSimbolos;
    private GeneradorCodigo generador;
    private List<String> errores;
    private List<String> warnings;

    private Stack<Map<String, String>> loopLabels = new Stack<>();


    public CodigoVisitor(TablaSimbolos tablaSimbolos, List<String> errores, List<String> warnings) {
        this.tablaSimbolos = tablaSimbolos;
        this.generador = new GeneradorCodigo();
        this.errores = errores;
        this.warnings = warnings;
    }

    public GeneradorCodigo getGenerador() {
        return generador;
    }

    /*
    private String generarEtiqueta() {
        return "L" + (etiquetaCounter++);
    }

    private String generarTemporal() {
        return "t" + (temporalCounter++);
    }
    */

    // ====================================================================================================
    // VISITORS DE REGLAS DE GRAMATICA
    // ====================================================================================================

    @Override
    public String visitPrograma(CompiladorParser.ProgramaContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public String visitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        generador.genInstruccion("func_" + nombreFuncion + ":"); // Usar genInstruccion

        visitChildren(ctx); // Visitar el bloque de la funcion
        generador.genInstruccion("end_func_" + nombreFuncion); // Usar genInstruccion

        return null;
    }

    @Override
    public String visitParametro(CompiladorParser.ParametroContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitBloque(CompiladorParser.BloqueContext ctx) {
        visitChildren(ctx);
        return null;
    }

    @Override
    public String visitDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx) {
        String id = ctx.ID().getText();
        if (ctx.expresion() != null) {
            String exprResult = visit(ctx.expresion());
            generador.genAsignacion(id, exprResult); // Usar genAsignacion
        }
        return id;
    }

    @Override
    public String visitAsignacion(CompiladorParser.AsignacionContext ctx) {
        String id = ctx.ID().getText();
        String exprResult = visit(ctx.expresion());
        generador.genAsignacion(id, exprResult); // Usar genAsignacion
        return id;
    }

    // ¡NUEVO MÉTODO! Para manejar asignaciones sin punto y coma (ej. en for-update)
    @Override
    public String visitAsignacionNoPyC(CompiladorParser.AsignacionNoPyCContext ctx) {
        String id = ctx.ID().getText();
        String exprResult = visit(ctx.expresion());
        generador.genAsignacion(id, exprResult); // Usar genAsignacion
        return id;
    }


    @Override
    public String visitSentenciaLlamadaFuncion(CompiladorParser.SentenciaLlamadaFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                String argTemp = visit(exprCtx);
                generador.genInstruccion("push " + argTemp); // Usar genInstruccion
            }
        }
        generador.genInstruccion("call " + nombreFuncion + ", " + (ctx.argumentos() != null ? ctx.argumentos().expresion().size() : 0)); // Usar genInstruccion
        return null;
    }

    @Override
    public String visitExpBinaria(CompiladorParser.ExpBinariaContext ctx) {
        String left = visit(ctx.expresion(0));
        String right = visit(ctx.expresion(1));
        String op = ctx.operadorBinario().getText(); // Usar genOperacionBinaria que ya crea y retorna la temporal
        return generador.genOperacionBinaria(op, left, right);
    }

    @Override
    public String visitExpNegacion(CompiladorParser.ExpNegacionContext ctx) {
        String exprResult = visit(ctx.expresion());
        String temp = generador.newTemp(); // Generar temporal usando GeneradorCodigo
        generador.genInstruccion(temp + " = !" + exprResult);
        return temp;
    }

    @Override
    public String visitExpParentizada(CompiladorParser.ExpParentizadaContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public String visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        return ctx.ID().getText();
    }

    @Override
    public String visitExpEntero(CompiladorParser.ExpEnteroContext ctx) {
        return ctx.INTEGER().getText();
    }

    @Override
    public String visitExpDecimal(CompiladorParser.ExpDecimalContext ctx) {
        return ctx.DECIMAL().getText();
    }

    @Override
    public String visitExpCaracter(CompiladorParser.ExpCaracterContext ctx) {
        return ctx.CHARACTER().getText();
    }

    @Override
    public String visitExpCadena(CompiladorParser.ExpCadenaContext ctx) {
        return ctx.STRING_LITERAL().getText();
    }

    @Override
    public String visitExpTrue(CompiladorParser.ExpTrueContext ctx) {
        return ctx.TRUE().getText();
    }

    @Override
    public String visitExpFalse(CompiladorParser.ExpFalseContext ctx) {
        return ctx.FALSE().getText();
    }

    @Override
    public String visitExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                String argTemp = visit(exprCtx);
                generador.genInstruccion("push " + argTemp); // Usar genInstruccion
            }
        }
        String returnTemp = generador.newTemp();
        generador.genInstruccion(returnTemp + " = call " + nombreFuncion + ", " + (ctx.argumentos() != null ? ctx.argumentos().expresion().size() : 0)); // Usar genInstruccion
        return returnTemp;
    }

    @Override
    public String visitRetorno(CompiladorParser.RetornoContext ctx) {
        if (ctx.expresion() != null) {
            String exprResult = visit(ctx.expresion());
            generador.genInstruccion("return " + exprResult); // Usar genInstruccion
        } else {
            generador.genInstruccion("return"); // Usar genInstruccion
        }
        return null;
    }

    @Override
    public String visitSentenciaIf(CompiladorParser.SentenciaIfContext ctx) {
        String condicionTemp = visit(ctx.expresion());
        String etiquetaElse = generador.newLabel(); // Usar newLabel
        String etiquetaEndIf = generador.newLabel(); // Usar newLabel

        generador.genIfFalse(condicionTemp, etiquetaElse); // Usar genIfFalse
        visit(ctx.bloque(0)); // Bloque IF

        if (ctx.ELSE() != null) {
            generador.genGoto(etiquetaEndIf); // Usar genGoto
            generador.genLabel(etiquetaElse); // Usar genLabel
            visit(ctx.bloque(1)); // Bloque ELSE
        } else {
            generador.genLabel(etiquetaElse); // Usar genLabel
        }
        generador.genLabel(etiquetaEndIf); // Usar genLabel
        return null;
    }

    @Override
    public String visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx) {
        String etiquetaInicioBucle = generador.newLabel(); // Usar newLabel
        String etiquetaFinBucle = generador.newLabel(); // Usar newLabel

        Map<String, String> currentLoopLabels = new HashMap<>();
        currentLoopLabels.put("breakLabel", etiquetaFinBucle);
        currentLoopLabels.put("continueLabel", etiquetaInicioBucle); // Continue para while salta al inicio para reevaluar
        loopLabels.push(currentLoopLabels);

        generador.genLabel(etiquetaInicioBucle); // Usar genLabel
        String condicionTemp = visit(ctx.expresion());
        generador.genIfFalse(condicionTemp, etiquetaFinBucle); // Usar genIfFalse
        visit(ctx.bloque()); // Cuerpo del bucle
        generador.genGoto(etiquetaInicioBucle); // Usar genGoto
        generador.genLabel(etiquetaFinBucle); // Usar genLabel

        loopLabels.pop();
        return null;
    }

    @Override
    public String visitSentenciaFor(CompiladorParser.SentenciaForContext ctx) {
        String labelInicioBucle = generador.newLabel();
        String labelCuerpoBucle = generador.newLabel();
        String labelActualizacion = generador.newLabel();
        String labelFinBucle = generador.newLabel();

        Map<String, String> currentLoopLabels = new HashMap<>();
        currentLoopLabels.put("breakLabel", labelFinBucle);
        currentLoopLabels.put("continueLabel", labelActualizacion);
        loopLabels.push(currentLoopLabels);

        // 1. Inicialización
        if (ctx.forInit != null) {
            visit(ctx.forInit);
        }

        generador.genGoto(labelInicioBucle);

        // --- Sección de Actualización (donde salta 'continue') ---
        generador.genLabel(labelActualizacion);
        // 4. Actualización
        if (ctx.forUpdate != null) {
            visit(ctx.forUpdate);
        }
        generador.genGoto(labelInicioBucle);


        // --- Sección de Condición ---
        generador.genLabel(labelInicioBucle);
        // 2. Condición
        if (ctx.forCond != null) {
            String condicionTemp = visit(ctx.forCond);
            generador.genIfFalse(condicionTemp, labelFinBucle);
        } else {
            generador.genIfFalse("true", labelFinBucle);
        }

        // --- Sección del Cuerpo del Bucle ---
        generador.genLabel(labelCuerpoBucle);
        visit(ctx.bloque()); // 3. Bloque del bucle
        generador.genGoto(labelActualizacion);

        // --- Sección de Fin del Bucle ---
        generador.genLabel(labelFinBucle);

        loopLabels.pop();
        return null;
    }

    @Override
    public String visitSentenciaBreak(CompiladorParser.SentenciaBreakContext ctx) {
        if (loopLabels.isEmpty()) {
            errores.add("Error semantico en linea " + ctx.start.getLine() + ": 'break' fuera de un bucle.");
            return null;
        }
        generador.genGoto(loopLabels.peek().get("breakLabel")); // Usar genGoto
        return null;
    }

    @Override
    public String visitSentenciaContinue(CompiladorParser.SentenciaContinueContext ctx) {
        if (loopLabels.isEmpty()) {
            errores.add("Error semantico en linea " + ctx.start.getLine() + ": 'continue' fuera de un bucle.");
            return null;
        }
        generador.genGoto(loopLabels.peek().get("continueLabel")); // Usar genGoto
        return null;
    }

    @Override
    public String visitExpresionNoPuntoComa(CompiladorParser.ExpresionNoPuntoComaContext ctx) {
        return visit(ctx.expresion());
    }


    /*
    private static class LoopLabels {
        private String etiquetaFinBucle;
        private String etiquetaInicioContinuar;

        public LoopLabels(String endLabel, String continueLabel) {
            this.etiquetaFinBucle = endLabel;
            this.etiquetaInicioContinuar = continueLabel;
        }

        public String getEtiquetaFinBucle() {
            return etiquetaFinBucle;
        }

        public String getEtiquetaInicioContinuar() {
            return etiquetaInicioContinuar;
        }
    }
    */
}