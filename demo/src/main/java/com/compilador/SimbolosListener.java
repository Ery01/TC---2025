package com.compilador;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Listener mejorado para construir la tabla de símbolos y realizar verificación de tipos
 */
public class SimbolosListener extends CompiladorBaseListener {

    private TablaSimbolos tablaSimbolos;
    private List<String> warnings;
    private List<String> errores;
    private String tipoRetornoActual; // Para verificar return dentro de funciones

    private Stack<String> ambitoStack;
    private int bloqueAnonimoCounter; // Para dar nombres unicos a los bloques anonimos

    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
        this.warnings = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.tipoRetornoActual = null;
        this.ambitoStack = new Stack<>();
        this.bloqueAnonimoCounter = 0;
        ambitoStack.push("global");
    }

    /**
     * Obtiene la tabla de símbolos construida
     */
    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    /**
     * Obtiene la lista de errores semánticos
     */
    public List<String> getErrores() {
        return errores;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Cuando se encuentra una declaración de función
     */
    @Override
    public void enterDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();

        TablaSimbolos.Simbolo funcionSimbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "funcion", linea, columna, "global"
        );
        if (!tablaSimbolos.agregar(funcionSimbolo)) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' ya declarada en el ámbito global.");
        }

        ambitoStack.push(nombre);

        if (ctx.parametros() != null) {
            for (CompiladorParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                String tipoParam = paramCtx.tipo().getText();
                String nombreParam = paramCtx.ID().getText();
                int paramLinea = paramCtx.ID().getSymbol().getLine();
                int paramColumna = paramCtx.ID().getSymbol().getCharPositionInLine();

                funcionSimbolo.addParametro(tipoParam);

                TablaSimbolos.Simbolo paramSimbolo = new TablaSimbolos.Simbolo(
                        nombreParam, tipoParam, "parametro",
                        paramLinea, paramColumna, ambitoStack.peek()
                );
                paramSimbolo.setInicializada(true);

                if (!tablaSimbolos.agregar(paramSimbolo)) {
                    errores.add("Error semántico en línea " + paramLinea +
                            ": Parámetro duplicado '" + nombreParam + "' en la función '" + nombre + "'.");
                }
            }
        }
        tipoRetornoActual = tipo;
    }

    /**
     * Al salir de una declaración de función
     */
    @Override
    public void exitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        String tipoFuncion = ctx.tipo().getText();
        String nombreFuncion = ctx.ID().getText();

        if (!tipoFuncion.equals("void")) {
            boolean tieneReturn = false;
            if (ctx.bloque() != null) {
                for (CompiladorParser.SentenciaAnidadasContext sentenciaAnidadasContext : ctx.bloque().sentenciaAnidadas()) {
                    if (sentenciaAnidadasContext.retorno() != null) {
                        tieneReturn = true;
                        break;
                    }
                }
            }
            if (!tieneReturn) {
                errores.add("Error semántico en función '" + nombreFuncion + "' (línea " + ctx.ID().getSymbol().getLine() +
                        "): Función con tipo de retorno '" + tipoFuncion + "' debe tener al menos una sentencia return.");
            }
        }
        ambitoStack.pop();
        tipoRetornoActual = null;
    }

    /**
     * Cuando se encuentra un bloque de código (por ejemplo, `if`, `while`, o bloques anidados)
     */
    @Override
    public void enterBloque(CompiladorParser.BloqueContext ctx) {
        ambitoStack.push("bloque_" + bloqueAnonimoCounter++);
    }

    /**
     * Al salir de un bloque de código
     */
    @Override
    public void exitBloque(CompiladorParser.BloqueContext ctx) {
        ambitoStack.pop();
    }

    /**
     * Cuando se encuentra una declaración de variable
     */
    @Override
    public void enterDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx) {
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();

        String ambitoDeDeclaracion = ambitoStack.peek();

        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "variable", linea, columna, ambitoDeDeclaracion
        );

        if (ctx.expresion() != null) {
            simbolo.setInicializada(true);
            EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
            try {
                Object evaluatedValue = tempEvaluador.visit(ctx.expresion());
                if (evaluatedValue != null) {
                    simbolo.setValor(evaluatedValue); // Almacenar el valor si es una constante evaluable
                }
            } catch (Exception e) {

            }
        }

        if (!tablaSimbolos.agregar(simbolo)) {
            errores.add("Error semántico en línea " + linea +
                    ": Variable '" + nombre + "' ya declarada en el ámbito '" + ambitoDeDeclaracion + "'.");
        }
    }

    /**
     * Cuando se encuentra una asignación
     */
    @Override
    public void enterAsignacion(CompiladorParser.AsignacionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + ": Variable '" + nombre + "' no declarada.");
            return;
        }

        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar valor a '" + nombre + "' porque no es una variable/parámetro.");
            return;
        }

        simbolo.setInicializada(true);
        simbolo.setUsada(true);

        String tipoExpresion = getTipoExpresion(ctx.expresion());
        String tipoVariable = simbolo.getTipo();

        if (tipoExpresion.equals("void")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar una expresión de tipo 'void'.");
            return;
        }

        EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
        try {
            Object evaluatedValue = tempEvaluador.visit(ctx.expresion());
            if (evaluatedValue != null) {
                simbolo.setValor(evaluatedValue);
            }
        } catch (Exception e) {

        }

        if (!esTipoCompatibleAsignacion(tipoVariable, tipoExpresion, linea)) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar '" + tipoExpresion + "' a '" + tipoVariable + "'.");
        }
    }

    /**
     * Función auxiliar para verificar compatibilidad de tipos en asignaciones.
     * @param tipoVariable Tipo de la variable a la que se asigna.
     * @param tipoExpresion Tipo de la expresión que se asigna.
     * @param linea Número de línea para advertencias.
     * @return true si los tipos son compatibles, false en caso contrario.
     */
    private boolean esTipoCompatibleAsignacion(String tipoVariable, String tipoExpresion, int linea) {
        // Caso 1: Tipos iguales → siempre válido
        if (tipoVariable.equals(tipoExpresion)) {
            return true;
        }

        // Caso 2: Conversiones implícitas ascendentes
        if (tipoVariable.equals("double") && (tipoExpresion.equals("int") || tipoExpresion.equals("char"))) {
            return true;
        }
        if (tipoVariable.equals("int") && tipoExpresion.equals("char")) {
            return true;
        }

        // Caso 3: Conversiones implícitas descendentes con advertencia
        if (tipoVariable.equals("int") && tipoExpresion.equals("double")) {
            warnings.add("Advertencia en línea " + linea + ": posible pérdida de datos al asignar 'double' a 'int'.");
            return true; // Permitimos la asignación con advertencia
        }

        return false;
    }


    /**
     * Cuando se encuentra una expresión de variable (uso de una variable)
     */
    @Override
    public void enterExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);

        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Identificador '" + nombre + "' no declarado.");
        } else {
            simbolo.setUsada(true); // Marcar como usada

            if (simbolo.getCategoria().equals("variable") && !simbolo.isInicializada()) {
                warnings.add("Advertencia en línea " + linea + ": Variable '" + nombre + "' utilizada sin estar inicializada.");
            }
        }
    }

    /**
     * Cuando se encuentra una llamada a función
     */
    @Override
    public void enterExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        TablaSimbolos.Simbolo simboloFuncion = tablaSimbolos.buscarEnAmbitoDirecto(nombre, "global");

        if (simboloFuncion == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' no declarada.");
            return;
        }

        if (!simboloFuncion.getCategoria().equals("funcion")) {
            errores.add("Error semántico en línea " + linea +
                    ": '" + nombre + "' no es una función.");
            return;
        }

        simboloFuncion.setUsada(true);

        List<String> tiposArgumentosRecibidos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                tiposArgumentosRecibidos.add(getTipoExpresion(exprCtx));
            }
        }

        if (!tablaSimbolos.verificarParametros(nombre, tiposArgumentosRecibidos)) {
            errores.add("Error semántico en línea " + linea +
                    ": La llamada a la función '" + nombre + "' no coincide con la declaración esperada.");
        }
    }

    /**
     * Cuando se encuentra una sentencia `return`
     */
    @Override
    public void enterRetorno(CompiladorParser.RetornoContext ctx) {
        int linea = ctx.getStart().getLine();

        if (tipoRetornoActual == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Sentencia 'return' fuera de una función.");
            return;
        }

        String tipoRetornadoPorExpresion = ctx.expresion() != null ? getTipoExpresion(ctx.expresion()) : "void";

        if (tipoRetornoActual.equals("void")) {
            if (ctx.expresion() != null) {
                errores.add("Error semántico en línea " + linea +
                        ": Función 'void' no debe retornar un valor.");
            }
        } else {
            if (ctx.expresion() == null) {
                errores.add("Error semántico en línea " + linea +
                        ": Función con tipo de retorno '" + tipoRetornoActual +
                        "' debe retornar un valor.");
            } else if (!esTipoCompatibleAsignacion(tipoRetornoActual, tipoRetornadoPorExpresion, linea)) {
                errores.add("Error semántico en línea " + linea +
                        ": El tipo de retorno esperado '" + tipoRetornoActual +
                        "' no es compatible con el tipo retornado '" + tipoRetornadoPorExpresion + "'.");
            }
        }
    }

    /**
     * Al encontrar un nodo de error en el árbol de análisis sintáctico.
     * Estos errores ya deberían ser capturados por el parser y no deberían llegar aquí si todo va bien.
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        errores.add("Error sintáctico en línea " + node.getSymbol().getLine() +
                ": Token inesperado o inválido: '" + node.getText() + "'");
    }

    /**
     * Método auxiliar para determinar el tipo de una expresión.
     * ¡IMPORTANTE!: Este método solo DETERMINA EL TIPO, no modifica la tabla de símbolos (no marca como usada/inicializada).
     * Es crucial que las marcas de uso/inicialización se hagan en `enterExpVariable` etc.
     */
    private String getTipoExpresion(CompiladorParser.ExpresionContext ctx) {
        if (ctx instanceof CompiladorParser.ExpVariableContext) {
            CompiladorParser.ExpVariableContext expVar = (CompiladorParser.ExpVariableContext) ctx;
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expVar.ID().getText(), ambitoStack);

            return simbolo != null ? simbolo.getTipo() : "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpEnteroContext) {
            return "int";

        } else if (ctx instanceof CompiladorParser.ExpDecimalContext) {
            return "double";

        } else if (ctx instanceof CompiladorParser.ExpCaracterContext) {
            return "char";

        } else if (ctx instanceof CompiladorParser.ExpTrueContext || ctx instanceof CompiladorParser.ExpFalseContext) {
            return "int";

        } else if (ctx instanceof CompiladorParser.ExpFuncionContext) {
            CompiladorParser.ExpFuncionContext expFunc = (CompiladorParser.ExpFuncionContext) ctx;
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscarEnAmbitoDirecto(expFunc.ID().getText(), "global");
            return simbolo != null ? simbolo.getTipo() : "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpNegacionContext) {
            CompiladorParser.ExpNegacionContext expNeg = (CompiladorParser.ExpNegacionContext) ctx;
            String tipoOperando = getTipoExpresion(expNeg.expresion());
            if (tipoOperando.equals("int")) {
                return "int";
            }
            errores.add("Error semántico en línea " + expNeg.getStart().getLine() +
                    ": Operador '!' no aplicable a tipo '" + tipoOperando + "'.");
            return "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpParentizadaContext) {
            CompiladorParser.ExpParentizadaContext expPar = (CompiladorParser.ExpParentizadaContext) ctx;
            return getTipoExpresion(expPar.expresion());

        } else if (ctx instanceof CompiladorParser.ExpBinariaContext) {
            CompiladorParser.ExpBinariaContext expBin = (CompiladorParser.ExpBinariaContext) ctx;
            String tipoIzq = getTipoExpresion(expBin.expresion(0));
            String tipoDer = getTipoExpresion(expBin.expresion(1));
            String operador = expBin.operadorBinario().getText();
            int linea = ctx.getStart().getLine();

            if (tipoIzq.equals("desconocido") || tipoDer.equals("desconocido")) {
                return "desconocido";
            }

            // --- AÑADIR VERIFICACIÓN DE DIVISIÓN POR CERO AQUI ---
            if (operador.equals("/") || operador.equals("%")) {
                Object valorDivisor = null;
                try {
                    EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
                    valorDivisor = tempEvaluador.visit(expBin.expresion(1));
                } catch (Exception e) {

                }

                if (valorDivisor != null) {
                    if ((valorDivisor instanceof Integer && (Integer) valorDivisor == 0) ||
                            (valorDivisor instanceof Double && (Double) valorDivisor == 0.0)) {
                        errores.add("Error semántico en línea " + linea + ": División/Módulo por cero detectada en tiempo de compilación.");
                        return "desconocido";
                    }
                }
            }

            switch (operador) {
                case "+": case "-": case "*": case "/": case "%":
                    if (!esTipoNumerico(tipoIzq) || !esTipoNumerico(tipoDer)) {
                        errores.add("Error semántico en línea " + linea +
                                ": Operador aritmético '" + operador + "' no puede usarse entre " + tipoIzq + " y " + tipoDer + ".");
                        return "desconocido";
                    }
                    if (tipoIzq.equals("double") || tipoDer.equals("double")) {
                        return "double";
                    }
                    return "int";

                case "&&": case "||":
                    if (!tipoIzq.equals("int") || !tipoDer.equals("int")) {
                        errores.add("Error semántico en línea " + linea +
                                ": Operador lógico '" + operador + "' requiere tipos enteros (int) para booleanos.");
                        return "desconocido";
                    }
                    return "int";

                case ">": case "<": case ">=": case "<=": case "==": case "!=":
                    if (!sonComparables(tipoIzq, tipoDer)) {
                        errores.add("Error semántico en línea " + linea +
                                ": No se pueden comparar tipos " + tipoIzq + " y " + tipoDer + " con '" + operador + "'.");
                        return "desconocido";
                    }
                    return "int";

                default:
                    errores.add("Error semántico en línea " + linea + ": Operador binario no soportado '" + operador + "'.");
                    return "desconocido";
            }
        }
        return "desconocido";
    }

    // Auxiliar: Verifica si un tipo es numérico (int, double, char)
    private boolean esTipoNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("double") || tipo.equals("char");
    }

    // Auxiliar: Verifica si dos tipos son comparables
    private boolean sonComparables(String tipo1, String tipo2) {
        if (esTipoNumerico(tipo1) && esTipoNumerico(tipo2)) {
            return !(tipo1.equals("char") && tipo2.equals("double")) &&
                    !(tipo1.equals("double") && tipo2.equals("char"));
        }
        return tipo1.equals(tipo2);
    }
}