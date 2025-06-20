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

    // NUEVO: Pila para gestionar los ámbitos
    private Stack<String> ambitoStack;
    private int bloqueAnonimoCounter; // Para dar nombres únicos a los bloques anónimos

    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
        this.warnings = new ArrayList<>();
        this.errores = new ArrayList<>();
        this.tipoRetornoActual = null;
        this.ambitoStack = new Stack<>();
        this.bloqueAnonimoCounter = 0;
        ambitoStack.push("global"); // El ámbito global es el primero
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

        // Crear símbolo para la función en el ámbito global
        TablaSimbolos.Simbolo funcionSimbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "funcion", linea, columna, "global"
        );
        // Si el nombre de la función ya existe en global, reportar error
        if (!tablaSimbolos.agregar(funcionSimbolo)) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' ya declarada en el ámbito global.");
        }

        // Empujar el ámbito de la función a la pila de ámbitos.
        // Las variables y parámetros dentro de la función pertenecen a este ámbito.
        ambitoStack.push(nombre);

        // Manejo de parámetros: agrégalos al símbolo de la función y a la tabla de símbolos
        if (ctx.parametros() != null) {
            for (CompiladorParser.ParametroContext paramCtx : ctx.parametros().parametro()) {
                String tipoParam = paramCtx.tipo().getText();
                String nombreParam = paramCtx.ID().getText();
                int paramLinea = paramCtx.ID().getSymbol().getLine();
                int paramColumna = paramCtx.ID().getSymbol().getCharPositionInLine();

                // Añadir el tipo del parámetro a la lista de parámetros de la función
                funcionSimbolo.addParametro(tipoParam);

                // Crear símbolo para el parámetro EN EL ÁMBITO DE LA FUNCIÓN
                TablaSimbolos.Simbolo paramSimbolo = new TablaSimbolos.Simbolo(
                        nombreParam, tipoParam, "parametro",
                        paramLinea, paramColumna, ambitoStack.peek() // El ámbito del parámetro es el de la función
                );
                // Los parámetros se consideran inicializados por definición
                paramSimbolo.setInicializada(true);

                if (!tablaSimbolos.agregar(paramSimbolo)) {
                    errores.add("Error semántico en línea " + paramLinea +
                            ": Parámetro duplicado '" + nombreParam + "' en la función '" + nombre + "'.");
                }
            }
        }

        // Guardar el tipo de retorno para verificar las sentencias `return`
        tipoRetornoActual = tipo;
    }

    /**
     * Al salir de una declaración de función
     */
    @Override
    public void exitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        // Verificar si la función no `void` tiene al menos un `return`
        String tipoFuncion = ctx.tipo().getText();
        String nombreFuncion = ctx.ID().getText();

        if (!tipoFuncion.equals("void")) {
            // Aquí solo se verifica si existe *algún* retorno en el bloque.
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

        // Desapilar el ámbito de la función al salir
        ambitoStack.pop();
        tipoRetornoActual = null; // Resetear el tipo de retorno actual
    }

    /**
     * Cuando se encuentra un bloque de código (por ejemplo, `if`, `while`, o bloques anidados)
     */
    @Override
    public void enterBloque(CompiladorParser.BloqueContext ctx) {
        // Empujar un nuevo ámbito para el bloque. Útil para variables declaradas dentro de `if`/`while`.
        ambitoStack.push("bloque_" + bloqueAnonimoCounter++);
    }

    /**
     * Al salir de un bloque de código
     */
    @Override
    public void exitBloque(CompiladorParser.BloqueContext ctx) {
        // Desapilar el ámbito del bloque
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

        // El ámbito de la variable es el tope de la pila de ámbitos
        String ambitoDeDeclaracion = ambitoStack.peek();

        // Crear y agregar el símbolo
        TablaSimbolos.Simbolo simbolo = new TablaSimbolos.Simbolo(
                nombre, tipo, "variable", linea, columna, ambitoDeDeclaracion
        );

        // --- MODIFICACIÓN AQUÍ: EVALUAR Y ALMACENAR VALOR SI SE INICIALIZA CON CONSTANTE ---
        if (ctx.expresion() != null) {
            simbolo.setInicializada(true);
            // Instanciar EvaluadorExpresiones aquí mismo
            EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
            try {
                Object evaluatedValue = tempEvaluador.visit(ctx.expresion());
                if (evaluatedValue != null) {
                    simbolo.setValor(evaluatedValue); // Almacenar el valor si es una constante evaluable
                }
            } catch (Exception e) {
                // Posibles errores de evaluación ya se añaden a 'errores' por EvaluadorExpresiones
                // No es necesario reportar aquí de nuevo, solo asegurar que 'valor' no se setee si hay problema.
            }
        }
        // --- FIN MODIFICACIÓN ---

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

        // Buscar la variable en la pila de ámbitos
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);
        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea + ": Variable '" + nombre + "' no declarada.");
            return;
        }

        // Verificar que sea una variable o parámetro (no una función)
        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar valor a '" + nombre + "' porque no es una variable/parámetro.");
            return;
        }

        // Marcar la variable como inicializada después de una asignación
        simbolo.setInicializada(true);
        simbolo.setUsada(true); // Una asignación también es un uso

        // Obtener el tipo de la expresión asignada (usando el mismo método de tipo)
        String tipoExpresion = getTipoExpresion(ctx.expresion());
        String tipoVariable = simbolo.getTipo();

        if (tipoExpresion.equals("void")) {
            errores.add("Error semántico en línea " + linea + ": No se puede asignar una expresión de tipo 'void'.");
            return;
        }

        // --- MODIFICACIÓN AQUÍ: EVALUAR Y ALMACENAR VALOR DE ASIGNACIÓN SI ES CONSTANTE ---
        // Instanciar EvaluadorExpresiones aquí mismo
        EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
        try {
            Object evaluatedValue = tempEvaluador.visit(ctx.expresion());
            if (evaluatedValue != null) {
                simbolo.setValor(evaluatedValue); // Almacenar el valor si es una constante evaluable
            }
        } catch (Exception e) {
            // Posibles errores de evaluación ya se añaden a 'errores' por EvaluadorExpresiones
            // No es necesario reportar aquí de nuevo.
        }
        // --- FIN MODIFICACIÓN ---

        // Verificar compatibilidad de tipos
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

        // Caso 2: Conversiones implícitas ascendentes ( widening conversions )
        if (tipoVariable.equals("double") && (tipoExpresion.equals("int") || tipoExpresion.equals("char"))) {
            return true;
        }
        if (tipoVariable.equals("int") && tipoExpresion.equals("char")) {
            return true;
        }

        // Caso 3: Conversiones implícitas descendentes ( narrowing conversions ) con advertencia
        if (tipoVariable.equals("int") && tipoExpresion.equals("double")) {
            warnings.add("Advertencia en línea " + linea + ": posible pérdida de datos al asignar 'double' a 'int'.");
            return true; // Permitimos la asignación con advertencia
        }
        // Puedes agregar más casos aquí si tu lenguaje lo permite (e.g., double a char, int a char)

        return false;
    }


    /**
     * Cuando se encuentra una expresión de variable (uso de una variable)
     */
    @Override
    public void enterExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();

        // Buscar la variable usando la pila de ámbitos
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);

        if (simbolo == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Identificador '" + nombre + "' no declarado.");
        } else {
            simbolo.setUsada(true); // Marcar como usada

            // Advertencia si se usa sin inicializar (solo si es una variable)
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

        // Las funciones se declaran en el ámbito global. Búsqueda directa.
        TablaSimbolos.Simbolo simboloFuncion = tablaSimbolos.buscarEnAmbitoDirecto(nombre, "global");

        if (simboloFuncion == null) {
            errores.add("Error semántico en línea " + linea +
                    ": Función '" + nombre + "' no declarada.");
            return;
        }

        // Verificar que el símbolo encontrado sea realmente una función
        if (!simboloFuncion.getCategoria().equals("funcion")) {
            errores.add("Error semántico en línea " + linea +
                    ": '" + nombre + "' no es una función.");
            return;
        }

        // Marcar la función como usada
        simboloFuncion.setUsada(true);

        // Obtener los tipos de los argumentos pasados en la llamada
        List<String> tiposArgumentosRecibidos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (CompiladorParser.ExpresionContext exprCtx : ctx.argumentos().expresion()) {
                tiposArgumentosRecibidos.add(getTipoExpresion(exprCtx)); // Obtener el tipo de cada argumento
            }
        }

        // Verificar el número y tipo de argumentos usando el método de TablaSimbolos
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

        // Verificar compatibilidad del tipo de retorno
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
                // Reutilizamos esTipoCompatibleAsignacion para la compatibilidad de retorno
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
        // Normalmente, los errores sintácticos se capturan en la fase de parsing.
        // Si llegan aquí, indica un problema en la configuración del ErrorListener del parser.
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
            // No llamar a simbolo.setUsada(true) aquí; se hace en enterExpVariable
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(expVar.ID().getText(), ambitoStack);

            // Si el símbolo tiene un valor constante conocido, EvaluadorExpresiones lo usará.
            // Aquí solo devolvemos el tipo, la evaluación real ocurre en EvaluadorExpresiones.
            return simbolo != null ? simbolo.getTipo() : "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpEnteroContext) {
            return "int";

        } else if (ctx instanceof CompiladorParser.ExpDecimalContext) {
            return "double";

        } else if (ctx instanceof CompiladorParser.ExpCaracterContext) {
            return "char";

        } else if (ctx instanceof CompiladorParser.ExpTrueContext || ctx instanceof CompiladorParser.ExpFalseContext) {
            // Si no tienes tipo 'boolean', puedes mapearlo a 'int' (0 para false, 1 para true)
            return "int"; // O el tipo que uses para booleanos

        } else if (ctx instanceof CompiladorParser.ExpFuncionContext) {
            CompiladorParser.ExpFuncionContext expFunc = (CompiladorParser.ExpFuncionContext) ctx;
            // No llamar a simbolo.setUsada(true) aquí; se hace en enterExpFuncion
            TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscarEnAmbitoDirecto(expFunc.ID().getText(), "global");
            return simbolo != null ? simbolo.getTipo() : "desconocido";

        } else if (ctx instanceof CompiladorParser.ExpNegacionContext) {
            CompiladorParser.ExpNegacionContext expNeg = (CompiladorParser.ExpNegacionContext) ctx;
            String tipoOperando = getTipoExpresion(expNeg.expresion());
            // Operador '!' suele aplicarse a booleanos (o ints para 0/1)
            if (tipoOperando.equals("int")) { // Asumiendo int para booleanos
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

            // Verificar si los operandos son válidos
            if (tipoIzq.equals("desconocido") || tipoDer.equals("desconocido")) {
                // Ya se reportó un error, solo propagar el tipo desconocido
                return "desconocido";
            }

            // --- AÑADIR VERIFICACIÓN DE DIVISIÓN POR CERO AQUI ---
            if (operador.equals("/") || operador.equals("%")) {
                Object valorDivisor = null;
                // Intentar evaluar el operando derecho (divisor) para ver si es una constante de valor cero
                try {
                    // El EvaluadorExpresiones necesita el ámbito actual para buscar variables
                    EvaluadorExpresiones tempEvaluador = new EvaluadorExpresiones(tablaSimbolos, ambitoStack, errores, warnings);
                    valorDivisor = tempEvaluador.visit(expBin.expresion(1)); // Evalúa el divisor
                } catch (Exception e) {
                    // Si ocurre una excepción al evaluar (ej. si la expresión no es una constante simple),
                    // simplemente no podemos detectarlo en compile-time y el valorDivisor seguirá siendo null.
                    // Los errores generados por EvaluadorExpresiones ya se añadirán a la lista de errores.
                }

                if (valorDivisor != null) {
                    if ((valorDivisor instanceof Integer && (Integer) valorDivisor == 0) ||
                            (valorDivisor instanceof Double && (Double) valorDivisor == 0.0)) {
                        errores.add("Error semántico en línea " + linea + ": División/Módulo por cero detectada en tiempo de compilación.");
                        return "desconocido"; // Retorna desconocido para indicar un error grave de tipo/semántica
                    }
                }
                // Si valorDivisor es null, significa que el divisor no es una constante evaluable en compile-time (ej. es una variable),
                // en cuyo caso la detección de división por cero debería hacerse en tiempo de ejecución (una fase posterior del compilador).
            }
            // --- FIN VERIFICACIÓN DE DIVISIÓN POR CERO ---


            // Lógica para determinar el tipo resultante y verificar compatibilidad
            switch (operador) {
                case "+": case "-": case "*": case "/": case "%":
                    // Operadores aritméticos: permiten combinaciones numéricas
                    if (!esTipoNumerico(tipoIzq) || !esTipoNumerico(tipoDer)) {
                        errores.add("Error semántico en línea " + linea +
                                ": Operador aritmético '" + operador + "' no puede usarse entre " + tipoIzq + " y " + tipoDer + ".");
                        return "desconocido";
                    }
                    // Reglas de promoción de tipos
                    if (tipoIzq.equals("double") || tipoDer.equals("double")) {
                        return "double";
                    }
                    return "int"; // Si ambos son int o char (char se promueve a int)

                case "&&": case "||":
                    // Operadores lógicos: requieren tipos booleanos (que mapeamos a int)
                    if (!tipoIzq.equals("int") || !tipoDer.equals("int")) {
                        errores.add("Error semántico en línea " + linea +
                                ": Operador lógico '" + operador + "' requiere tipos enteros (int) para booleanos.");
                        return "desconocido";
                    }
                    return "int"; // El resultado es un booleano (int)

                case ">": case "<": case ">=": case "<=": case "==": case "!=":
                    // Operadores de comparación: requieren tipos comparables, el resultado es booleano (int)
                    if (!sonComparables(tipoIzq, tipoDer)) {
                        errores.add("Error semántico en línea " + linea +
                                ": No se pueden comparar tipos " + tipoIzq + " y " + tipoDer + " con '" + operador + "'.");
                        return "desconocido";
                    }
                    return "int"; // El resultado es un booleano (int)

                default:
                    errores.add("Error semántico en línea " + linea + ": Operador binario no soportado '" + operador + "'.");
                    return "desconocido";
            }
        }
        return "desconocido"; // Para cualquier otro tipo de expresión no manejado
    }

    // Auxiliar: Verifica si un tipo es numérico (int, double, char)
    private boolean esTipoNumerico(String tipo) {
        return tipo.equals("int") || tipo.equals("double") || tipo.equals("char");
    }

    // Auxiliar: Verifica si dos tipos son comparables
    private boolean sonComparables(String tipo1, String tipo2) {
        // Permitir comparaciones entre tipos numéricos (int, double, char)
        if (esTipoNumerico(tipo1) && esTipoNumerico(tipo2)) {
            // Exclusión: no permitir comparación directa entre char y double sin casting explícito
            // Esto es una regla de diseño, si tu lenguaje los compara, remueve la excepción
            return !(tipo1.equals("char") && tipo2.equals("double")) &&
                    !(tipo1.equals("double") && tipo2.equals("char"));
        }
        // Permitir comparaciones entre tipos idénticos (ej. "void" == "void" si aplica, aunque raro)
        return tipo1.equals(tipo2);
    }
}