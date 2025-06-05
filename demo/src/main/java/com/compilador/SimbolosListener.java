package com.compilador;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listener mejorado para construir la tabla de símbolos y realizar verificación de tipos
 */
public class SimbolosListener extends CompiladorBaseListener {

    private TablaSimbolos tablaSimbolos;
    private List<String> warnings;
    private List<String> errores;
    private String tipoRetornoActual; // Para verificar return


    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
        this.warnings = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.tipoRetornoActual = null;
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
        // Obtener información de la función
        String nombre = ctx.ID().getText();
        String tipo = ctx.tipo().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();

        // Crear símbolo para la función
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "funcion", linea, columna, "global"
        );

        // Agregar parámetros si existen
        if (ctx.parametros() != null) {
            for (CompiladorParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                String tipoParam = paramCtx.tipo().getText();
                String nombreParam = paramCtx.ID().getText();

                // Agregar tipo de parámetro a la función
                simbolo.addParametro(tipoParam);

                // Crear símbolo para el parámetro
                TablaSimbolos.Simbolo paramSimbolo = new TablaSimbolos.Simbolo(
                        nombreParam, tipoParam, "parametro",
                        paramCtx.ID().getSymbol().getLine(),
                        paramCtx.ID().getSymbol().getCharPositionInLine(),
                        nombre  // El ámbito del parámetro es el nombre de la función
                );

                // Agregar el parámetro a la tabla de símbolos
                if (!tablaSimbolos.agregar(paramSimbolo)) {
                    errores.add("Error semántico en línea " + paramCtx.ID().getSymbol().getLine() +
                            ": Parámetro duplicado '" + nombreParam + "'");
                }
            }
        }

        // Agregar la función a la tabla de símbolos
        if (!tablaSimbolos.agregar(simbolo)) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' ya declarada");
        }

        // Cambiar el ámbito actual
        tablaSimbolos.setAmbito(nombre);

        // Guardar el tipo de retorno para verificar las sentencias return
        tipoRetornoActual = tipo;
    }

    /**
     * Al salir de una declaración de función
     */
    @Override
    public void exitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        // Verificar si la función no void tiene al menos un return
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();

        if (!tipo.equals("void")) {
            // Podríamos hacer un análisis más profundo para garantizar que todos los caminos tienen return
            // pero eso requeriría un análisis de flujo de control más complejo
            boolean tieneReturn = false;

            for (int i = 0; i < ctx.bloque().sentencia().size(); i++) {
                if (ctx.bloque().sentencia(i).retorno() != null) {
                    tieneReturn = true;
                    break;
                }
            }

            if (!tieneReturn) {
                errores.add("Error semántico en función '" + nombre + "': Función con tipo de retorno '" +
                        tipo + "' debe tener al menos una sentencia return");
            }
        }

        // Restaurar el ámbito global y el tipo de retorno
        tablaSimbolos.setAmbito("global");
        tipoRetornoActual = null;
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

        // Crear y agregar el símbolo
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "variable", linea, columna, tablaSimbolos.getAmbito()
        );

        if (!tablaSimbolos.agregar(simbolo)) {
            errores.add("Error semántico en línea " + linea +
                    ": Variable '" + nombre + "' ya declarada en este ámbito");
        }
    }

    /**
     * Cuando se encuentra una asignación
     */
    @Override
    public void enterAsignacion(CompiladorParser.AsignacionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        // Verificar si la variable existe
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + ": Variable '" + nombre + "' no declarada");
            return;
        }

        // Verificar que sea una variable o parámetro (no una función)
        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar valor a '" + nombre + "' porque no es una variable");
            return;
        }

        // Obtener el tipo de la expresión asignada
        String tipoExpresion = getTipoExpresion(ctx.expresion());
        String tipoVariable = simbolo.getTipo();

        if (tipoExpresion.equals("void")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar una expresión de tipo void");
            return;
        }

        // Verificar compatibilidad de tipos
        if (!esTipoCompatble(tipoVariable, tipoExpresion, linea)) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar '" + tipoExpresion + "' a '" + tipoVariable + "'");
        }

    }

    private boolean esTipoCompatble(String tipoVariable, String tipoExpresion, int linea) {
        // Caso 1: Tipos iguales → siempre válido
        if (tipoVariable.equals(tipoExpresion)) {
            return true;
        }

        // Caso 2: Asignación numérica (ej: int → double, char → int)
        if (tipoVariable.equals("double") && (tipoExpresion.equals("int") || tipoExpresion.equals("char"))) {
            return true;
        }
        if (tipoVariable.equals("int") && tipoExpresion.equals("char")) {
            return true;
        }

        // Warning: asignar double a int (posible pérdida de datos)
        if (tipoVariable.equals("int") && tipoExpresion.equals("double")) {
            warnings.add("Advertencia en línea " + linea + ": posible pérdida de datos asignando 'double' a 'int'");
            return true; // Permitís la asignación con warning
        }

        return false;
    }

    /**
     * Cuando se encuentra una expresión de variable
     */
    @Override
    public void enterExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Identificador '" + nombre + "' no declarado");
        }
    }

    /**
     * Cuando se encuentra una llamada a función
     */
    @Override
    public void enterExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        // Verificar si la función existe
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' no declarada");
            return;
        }

        // Verificar que sea una función
        if (!simbolo.getCategoria().equals("funcion")) {
            errores.add("Error semántico en línea " + linea +
                    ": '" + nombre + "' no es una función");
            return;
        }

        // Verificar número de argumentos
        int numArgumentosEsperados = simbolo.getParametros().size();
        int numArgumentosRecibidos = ctx.argumentos() == null ? 0 : ctx.argumentos().expresion().size();

        if (numArgumentosEsperados != numArgumentosRecibidos) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' espera " + numArgumentosEsperados +
                    " argumentos, pero recibió " + numArgumentosRecibidos);
        }
        // Para una verificación completa de tipos, necesitaríamos determinar el tipo de cada expresión
    }

    /**
     * Cuando se encuentra una sentencia return
     */
    @Override
    public void enterRetorno(CompiladorParser.RetornoContext ctx) {
        if (tipoRetornoActual == null) {
            errores.add("Error semántico en línea " + ctx.getStart().getLine() +
                    ": Sentencia return fuera de una función");
            return;
        }

        // Verificar compatibilidad del tipo de retorno
        if (tipoRetornoActual.equals("void")) {
            if (ctx.expresion() != null) {
                errores.add("Error semántico en línea " + ctx.getStart().getLine() +
                        ": Función void no debe retornar un valor");
            }
        } else {
            if (ctx.expresion() == null) {
                errores.add("Error semántico en línea " + ctx.getStart().getLine() +
                        ": Función con tipo de retorno '" + tipoRetornoActual +
                        "' debe retornar un valor");
            }
            // Una verificación completa requeriría determinar el tipo de la expresión
        }
    }

    /**
     * Al encontrar un nodo de error en el árbol de análisis sintáctico
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        errores.add("Error sintáctico en token: " + node.getText());
    }

    /**
     * Método para determinar el tipo de una expresión (implementación básica)
     * Una implementación completa requeriría más lógica para evaluar expresiones complejas
     */
    private String getTipoExpresion(CompiladorParser.ExpresionContext ctx) {
        if (ctx instanceof CompiladorParser.ExpVariableContext) {
            CompiladorParser.ExpVariableContext expVar = (CompiladorParser.ExpVariableContext) ctx;
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expVar.ID().getText());
            return simbolo != null ? simbolo.getTipo() : "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpEnteroContext) {
            return "int";

        } else if (ctx instanceof CompiladorParser.ExpDecimalContext) {
            return "double";

        } else if (ctx instanceof CompiladorParser.ExpCaracterContext) {
            return "char";

        } else if (ctx instanceof CompiladorParser.ExpFuncionContext) {
            CompiladorParser.ExpFuncionContext expFunc = (CompiladorParser.ExpFuncionContext) ctx;
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expFunc.ID().getText());
            if (simbolo == null) return "desconocido";

            // Verificación: cantidad de argumentos y sus tipos
            List<CompiladorParser.ExpresionContext> args = expFunc.argumentos() != null ? expFunc.argumentos().expresion() : new ArrayList<>();
            List<String> tiposArgs = args.stream().map(this::getTipoExpresion).collect(Collectors.toList());

            if (!tablaSimbolos.verificarParametros(expFunc.ID().getText(), tiposArgs)) {
                errores.add("Error semántico en línea " + expFunc.ID().getSymbol().getLine() +
                        ": Los argumentos no coinciden con la declaración de la función '" + expFunc.ID().getText() + "'");
            }

            return simbolo.getTipo();

        } else if (ctx instanceof CompiladorParser.ExpNegacionContext) {
            // Suponemos que `!expr` siempre espera un booleano, pero si no hay tipo boolean en tu lenguaje,
            // podés considerar cualquier tipo numérico como válido.
            CompiladorParser.ExpNegacionContext expNeg = (CompiladorParser.ExpNegacionContext) ctx;
            String tipo = getTipoExpresion(expNeg.expresion());
            return tipo;

        } else if (ctx instanceof CompiladorParser.ExpParentizadaContext) {
            CompiladorParser.ExpParentizadaContext expPar = (CompiladorParser.ExpParentizadaContext) ctx;
            return getTipoExpresion(expPar.expresion());

        } else if (ctx instanceof CompiladorParser.ExpBinariaContext) {
            CompiladorParser.ExpBinariaContext expBin = (CompiladorParser.ExpBinariaContext) ctx;
            String tipoIzq = getTipoExpresion(expBin.expresion(0));
            String tipoDer = getTipoExpresion(expBin.expresion(1));
            String operador = expBin.operadorBinario().getText();

            // Comparadores y operadores lógicos → booleano (pero como no tenés bool, usamos int para true/false)
            if (operador.equals(">") || operador.equals(">=") || operador.equals("<") || operador.equals("<=") ||
                    operador.equals("==") || operador.equals("!=") ||
                    operador.equals("&&") || operador.equals("||")) {
                return "int"; // o "bool" si lo definís más adelante
            }

            // Operadores matemáticos
            if ((tipoIzq.equals("double") || tipoDer.equals("double"))) {
                return "double";
            } else if ((tipoIzq.equals("int") || tipoDer.equals("int"))) {
                return "int";
            } else {
                return "desconocido";
            }
        }

        return "desconocido";
    }


    @Override
    public void enterExpBinaria(CompiladorParser.ExpBinariaContext ctx) {
        // 1. Obtener los tipos de los operandos izquierdo y derecho
        String tipoIzq = getTipoExpresion(ctx.expresion(0));
        String tipoDer = getTipoExpresion(ctx.expresion(1));
        String operador = ctx.operadorBinario().getText();
        int linea = ctx.getStart().getLine();

        // 2. Verificar si los operandos son válidos (no "desconocido")
        if (tipoIzq.equals("desconocido") || tipoDer.equals("desconocido")) {
            errores.add("Error semántico en línea " + linea + ": Operación con tipo no reconocido");
            return;
        }

        // 3. Verificar compatibilidad según el operador
        switch (operador) {
            case "+": case "-": case "*": case "/": case "%":
                // Operadores aritméticos: permiten combinaciones numéricas (int, double, char)
                if (!esTipoNumerico(tipoIzq) || !esTipoNumerico(tipoDer)) {
                    errores.add("Error semántico en línea " + linea +
                            ": Operador '" + operador + "' no puede usarse entre " + tipoIzq + " y " + tipoDer);
                }
                break;

            case "&&": case "||":
                // Operadores lógicos: solo permiten booleanos (en muchos lenguajes) o enteros (como en C)
                if (!tipoIzq.equals("int") || !tipoDer.equals("int")) {
                    errores.add("Error semántico en línea " + linea +
                            ": Operador lógico '" + operador + "' requiere tipos enteros (int)");
                }
                break;

            case ">": case "<": case ">=": case "<=": case "==": case "!=":
                // Operadores de comparación: permiten tipos compatibles pero no mezclar char con double
                if (!sonComparables(tipoIzq, tipoDer)) {
                    errores.add("Error semántico en línea " + linea +
                            ": No se pueden comparar " + tipoIzq + " y " + tipoDer + " con '" + operador + "'");
                }
                break;

            default:
                errores.add("Error semántico en línea " + linea + ": Operador no soportado '" + operador + "'");
        }
    }

    // Verifica si un tipo es numérico (int, double, char)
    private boolean esTipoNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("double") || tipo.equals("char");
    }

    // Verifica si dos tipos son comparables entre sí
    private boolean sonComparables(String tipo1, String tipo2) {
        // Permitir comparaciones entre tipos numéricos, pero no char con double
        if (esTipoNumerico(tipo1) && esTipoNumerico(tipo2)) {
            return !(tipo1.equals("char") && tipo2.equals("double")) &&
                    !(tipo1.equals("double") && tipo2.equals("char"));
        }
        // Comparaciones entre tipos iguales (ej: int == int)
        return tipo1.equals(tipo2);
    }
}