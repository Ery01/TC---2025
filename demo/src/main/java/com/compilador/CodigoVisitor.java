package com.compilador;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

// Ahora extiende CompiladorBaseVisitor<String>
public class CodigoVisitor extends CompiladorBaseVisitor<String> {

    private GeneradorCodigo generador;
    private TablaSimbolos tabla;
    private Stack<String> ambitoStack; // Pila para gestionar los ámbitos en el Visitor
    private Stack<Map<String, String>> loopLabels; // Para manejar etiquetas de break/continue
    private List<String> errores; // Lista para registrar errores durante la generación de TAC
    private List<String> warnings; // Lista para registrar advertencias

    public CodigoVisitor(TablaSimbolos tabla, List<String> errores, List<String> warnings) {
        this.generador = new GeneradorCodigo();
        this.tabla = tabla;
        this.errores = errores;
        this.warnings = warnings;
        this.ambitoStack = new Stack<>();
        this.ambitoStack.push("global"); // El ámbito global es el primero
        this.loopLabels = new Stack<>();
    }

    /**
     * Obtiene el generador de código
     */
    public GeneradorCodigo getGenerador() {
        return generador;
    }

    // Método auxiliar para agregar errores específicos de TAC
    private void addTACError(int line, String message) {
        errores.add("ERROR CÓDIGO INTERMEDIO en línea " + line + ": " + message);
    }

    @Override
    public String visitPrograma(CompiladorParser.ProgramaContext ctx) {
        for (CompiladorParser.SentenciaContext sentencia : ctx.sentencia()) {
            visit(sentencia);
        }
        return null;
    }

    @Override
    public String visitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();

        // Generar etiqueta para el inicio de la función
        generador.genLabel("func_" + nombreFuncion);

        // Empujar el ámbito de la función a la pila de ámbitos del visitor
        ambitoStack.push(nombreFuncion);

        // Procesar parámetros (aunque no generen código intermedio directo, asegura visita a sus subárboles)
        if (ctx.parametros() != null) {
            visit(ctx.parametros());
        }

        // Procesar el bloque de código de la función
        visit(ctx.bloque());

        // Desapilar el ámbito de la función al salir
        ambitoStack.pop();

        // Generar instrucción de fin de función
        generador.getCodigo().add("end_func_" + nombreFuncion);

        return null;
    }

    @Override
    public String visitParametro(CompiladorParser.ParametroContext ctx) {
        return null; // Los parámetros se manejan en la declaración de la función.
    }

    @Override
    public String visitParametros(CompiladorParser.ParametrosContext ctx) {
        for (CompiladorParser.ParametroContext paramCtx : ctx.parametro()) {
            visit(paramCtx);
        }
        return null;
    }

    @Override
    public String visitBloque(CompiladorParser.BloqueContext ctx) {
        ambitoStack.push("bloque_" + generador.newLabel()); // Ámbito único para el bloque

        for (CompiladorParser.SentenciaAnidadasContext sentencia : ctx.sentenciaAnidadas()) { // Usar sentenciaAnidadas
            visit(sentencia);
        }

        ambitoStack.pop();
        return null;
    }

    @Override
    public String visitAsignacion(CompiladorParser.AsignacionContext ctx) {
        String variable = ctx.ID().getText();

        // Evaluar la expresión del lado derecho
        String resultadoExpr = visit(ctx.expresion());

        // Generar la asignación
        if (resultadoExpr != null) {
            generador.genAsignacion(variable, resultadoExpr);
        } else {
            addTACError(ctx.getStart().getLine(), "Expresión de asignación nula.");
        }

        return null;
    }

    @Override
    public String visitSentenciaIf(CompiladorParser.SentenciaIfContext ctx) {
        // Evaluar la condición
        String condicion = visit(ctx.expresion());

        // Crear etiquetas
        String labelElse = generador.newLabel();
        String labelFinIf = generador.newLabel();

        // Generar salto condicional: IF_FALSE (condición) GOTO (labelElse)
        if (condicion != null) {
            generador.genIfFalse(condicion, labelElse);
        } else {
            addTACError(ctx.expresion().getStart().getLine(), "Condición IF nula.");
        }

        // Procesar bloque IF (then)
        visit(ctx.bloque(0));

        // Si hay ELSE, necesitamos un GOTO para saltar el bloque ELSE
        if (ctx.ELSE() != null) {
            generador.genGoto(labelFinIf);
            generador.genLabel(labelElse); // Etiqueta para el inicio del bloque ELSE

            visit(ctx.bloque(1)); // Bloque ELSE

            generador.genLabel(labelFinIf); // Etiqueta para el final de todo el IF-ELSE
        } else {
            // Si no hay ELSE, la etiqueta 'else' es el final del IF
            generador.genLabel(labelElse);
        }

        return null;
    }

    @Override
    public String visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx) {
        String labelInicioBucle = generador.newLabel();
        String labelFinBucle = generador.newLabel();

        // Registrar etiquetas de bucle para break/continue
        Map<String, String> currentLoopLabels = new HashMap<>();
        currentLoopLabels.put("breakLabel", labelFinBucle);
        currentLoopLabels.put("continueLabel", labelInicioBucle); // Continue va al inicio del bucle para reevaluar condición
        loopLabels.push(currentLoopLabels);

        // Etiqueta de inicio del bucle
        generador.genLabel(labelInicioBucle);

        // Evaluar la condición
        String condicion = visit(ctx.expresion());
        if (condicion != null) {
            // Si la condición es falsa, saltar al final del bucle
            generador.genIfFalse(condicion, labelFinBucle);
        } else {
            addTACError(ctx.expresion().getStart().getLine(), "Condición WHILE nula.");
        }

        // Procesar bloque del bucle
        visit(ctx.bloque());

        // GOTO incondicional al inicio del bucle para reevaluar la condición
        generador.genGoto(labelInicioBucle);

        // Etiqueta de fin del bucle
        generador.genLabel(labelFinBucle);

        loopLabels.pop(); // Salir del ámbito de etiquetas de bucle
        return null;
    }

    @Override
    public String visitSentenciaFor(CompiladorParser.SentenciaForContext ctx) {
        String labelInicioBucle = generador.newLabel();
        String labelFinBucle = generador.newLabel();
        String labelActualizacion = generador.newLabel(); // Etiqueta para la sección de actualización

        // Registrar etiquetas de bucle para break/continue
        Map<String, String> currentLoopLabels = new HashMap<>();
        currentLoopLabels.put("breakLabel", labelFinBucle);
        currentLoopLabels.put("continueLabel", labelActualizacion); // Continue salta a la actualización
        loopLabels.push(currentLoopLabels);

        // 1. Inicialización (puede ser declaracionVariableInternaFor o expresionNoPuntoComa)
        if (ctx.inicializacionDeclaracion != null) {
            visit(ctx.inicializacionDeclaracion); // Visita la declaración/asignación inicial
        } else if (ctx.inicializacionExpresion != null) {
            visit(ctx.inicializacionExpresion); // Visita la expresión de inicialización
        }

        // Etiqueta de inicio del bucle (antes de la condición)
        generador.genLabel(labelInicioBucle);

        // 2. Condición
        if (ctx.condicion != null) {
            String condicion = visit(ctx.condicion);
            if (condicion != null) {
                // Si la condición es falsa, saltar al final del bucle
                generador.genIfFalse(condicion, labelFinBucle);
            } else {
                addTACError(ctx.condicion.getStart().getLine(), "Condición FOR nula.");
            }
        }

        // 3. Bloque del bucle
        visit(ctx.bloque());

        // Etiqueta para la sección de actualización (a donde salta 'continue')
        generador.genLabel(labelActualizacion);

        // 4. Actualización
        if (ctx.actualizacion != null) {
            visit(ctx.actualizacion);
        }

        // GOTO incondicional al inicio del bucle para reevaluar la condición
        generador.genGoto(labelInicioBucle);

        // Etiqueta de fin del bucle
        generador.genLabel(labelFinBucle);

        loopLabels.pop(); // Salir del ámbito de etiquetas de bucle
        return null;
    }

    @Override
    public String visitDeclaracionVariableInternaFor(CompiladorParser.DeclaracionVariableInternaForContext ctx) {
        String varName = ctx.ID().getText();

        if (ctx.expresion() != null) {
            String exprResult = visit(ctx.expresion());
            if (exprResult != null) {
                generador.genAsignacion(varName, exprResult);
            } else {
                addTACError(ctx.getStart().getLine(), "Expresión de inicialización interna de FOR nula.");
            }
        }
        return null;
    }

    @Override
    public String visitExpresionNoPuntoComa(CompiladorParser.ExpresionNoPuntoComaContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public String visitSentenciaBreak(CompiladorParser.SentenciaBreakContext ctx) {
        if (loopLabels.isEmpty()) {
            addTACError(ctx.getStart().getLine(), "Sentencia 'break' fuera de un bucle.");
            return null;
        }
        String breakLabel = loopLabels.peek().get("breakLabel");
        generador.genGoto(breakLabel);
        return null;
    }

    @Override
    public String visitSentenciaContinue(CompiladorParser.SentenciaContinueContext ctx) {
        if (loopLabels.isEmpty()) {
            addTACError(ctx.getStart().getLine(), "Sentencia 'continue' fuera de un bucle.");
            return null;
        }
        String continueLabel = loopLabels.peek().get("continueLabel");
        generador.genGoto(continueLabel);
        return null;
    }

    @Override
    public String visitRetorno(CompiladorParser.RetornoContext ctx) {
        if (ctx.expresion() != null) {
            String valor = visit(ctx.expresion());
            if (valor != null) {
                generador.getCodigo().add("return " + valor);
            } else {
                addTACError(ctx.getStart().getLine(), "Valor de retorno nulo.");
            }
        } else {
            generador.getCodigo().add("return");
        }
        return null;
    }

    @Override
    public String visitSentenciaLlamadaFuncion(CompiladorParser.SentenciaLlamadaFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();

        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                String argResult = visit(exprCtx);
                if (argResult != null) {
                    generador.getCodigo().add("push " + argResult);
                } else {
                    addTACError(exprCtx.getStart().getLine(), "Argumento nulo en llamada a función.");
                }
            }
        }

        generador.getCodigo().add("call " + nombreFuncion + ", " + (ctx.argumentos() != null ? ctx.argumentos().expresion().size() : 0));

        return null;
    }

    // ========================================================================
    // Expresiones
    // ========================================================================

    @Override
    public String visitExpBinaria(CompiladorParser.ExpBinariaContext ctx) {
        String operador = ctx.operadorBinario().getText();

        String left = visit(ctx.expresion(0));
        String right = visit(ctx.expresion(1));

        if (left != null && right != null) {
            return generador.genOperacionBinaria(operador, left, right);
        } else {
            addTACError(ctx.getStart().getLine(), "Operandos nulos en expresión binaria.");
            return null;
        }
    }

    @Override
    public String visitExpNegacion(CompiladorParser.ExpNegacionContext ctx) {
        String exprResult = visit(ctx.expresion());
        String temp = generador.newTemp();

        if (exprResult != null) {
            generador.getCodigo().add(temp + " = !" + exprResult);
            return temp;
        } else {
            addTACError(ctx.getStart().getLine(), "Expresión nula en negación.");
            return null;
        }
    }

    @Override
    public String visitExpParentizada(CompiladorParser.ExpParentizadaContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public String visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String variable = ctx.ID().getText();
        // Se asume que SimbolosListener y Caminante ya validaron la existencia de la variable.
        // Aquí simplemente retornamos su nombre como operando en TAC.
        return variable;
    }

    @Override
    public String visitExpEntero(CompiladorParser.ExpEnteroContext ctx) {
        String numero = ctx.INTEGER().getText();
        String temp = generador.newTemp();
        generador.genAsignacion(temp, numero); // Asignar el literal a una temporal
        return temp;
    }

    @Override
    public String visitExpDecimal(CompiladorParser.ExpDecimalContext ctx) {
        String decimal = ctx.DECIMAL().getText();
        String temp = generador.newTemp();
        generador.genAsignacion(temp, decimal); // Asignar el literal a una temporal
        return temp;
    }

    @Override
    public String visitExpCaracter(CompiladorParser.ExpCaracterContext ctx) {
        String caracter = ctx.CHARACTER().getText();
        String temp = generador.newTemp();
        generador.genAsignacion(temp, caracter); // Asignar el literal a una temporal
        return temp;
    }

    @Override
    public String visitExpCadena(CompiladorParser.ExpCadenaContext ctx) {
        String cadena = ctx.STRING_LITERAL().getText();
        String temp = generador.newTemp();
        generador.genAsignacion(temp, cadena); // Asignar el literal a una temporal
        return temp;
    }

    @Override
    public String visitExpTrue(CompiladorParser.ExpTrueContext ctx) {
        String temp = generador.newTemp();
        generador.genAsignacion(temp, "true");
        return temp;
    }

    @Override
    public String visitExpFalse(CompiladorParser.ExpFalseContext ctx) {
        String temp = generador.newTemp();
        generador.genAsignacion(temp, "false");
        return temp;
    }

    @Override
    public String visitExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        String nombreFuncion = ctx.ID().getText();
        String tempResult = generador.newTemp(); // Temporal para guardar el valor de retorno

        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                String argResult = visit(exprCtx);
                if (argResult != null) {
                    generador.getCodigo().add("push " + argResult);
                } else {
                    addTACError(exprCtx.getStart().getLine(), "Argumento nulo en llamada a función expresión.");
                }
            }
        }

        // Llamada a función: tempResult = CALL functionName, numArgs
        generador.getCodigo().add(tempResult + " = call " + nombreFuncion + ", " + (ctx.argumentos() != null ? ctx.argumentos().expresion().size() : 0));

        return tempResult;
    }

    @Override
    public String visitDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx) {
        String variable = ctx.ID().getText();

        // Si hay una inicialización, generar la asignación
        if (ctx.expresion() != null) {
            String valorInicial = visit(ctx.expresion());
            if (valorInicial != null) {
                generador.genAsignacion(variable, valorInicial);
            } else {
                addTACError(ctx.getStart().getLine(), "Expresión de inicialización nula en declaración de variable.");
            }
        }
        return null;
    }
}
