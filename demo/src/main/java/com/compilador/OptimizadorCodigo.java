package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

/**
 * Clase para aplicar optimizaciones al código de tres direcciones (TAC).
 */
public class OptimizadorCodigo {

    // --- Constantes de Color ANSI ---
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    // Patrones regex para parsear instrucciones TAC
    private static final Pattern PATRON_ASIGNACION = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (.+)$");
    private static final Pattern PATRON_OPERACION_BINARIA = Pattern.compile("^(t\\d+) = ([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|\".*\"|'.*'|true|false) ([+\\-*/%<>=!&|]{1,2}) ([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|\".*\"|'.*'|true|false)$");
    private static final Pattern PATRON_OPERACION_UNARIA = Pattern.compile("^(t\\d+) = !([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|true|false)$");
    private static final Pattern PATRON_ASIGNACION_LITERAL = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (\\d+(\\.\\d+)?|\".*\"|'.*'|true|false)$");

    public OptimizadorCodigo() {
        System.out.println(CYAN + "✨ OPTIMIZADOR: Iniciado." + RESET);
    }

    /**
     * Aplica un conjunto de optimizaciones al código TAC dado.
     * @param codigoTac La lista de instrucciones TAC sin optimizar.
     * @return La lista de instrucciones TAC optimizadas.
     */
    public List<String> optimizar(List<String> codigoTac) {
        List<String> codigoOptimizado = new ArrayList<>(codigoTac);

        System.out.println(CYAN + "✨ OPTIMIZADOR: Aplicando optimizaciones..." + RESET);

        for (int i = 0; i < 3; i++) {
            codigoOptimizado = aplicarPropagacionYPlegadoConstantes(codigoOptimizado);
            codigoOptimizado = aplicarSimplificacionExpresiones(codigoOptimizado);
            codigoOptimizado = aplicarEliminacionCodigoMuerto(codigoOptimizado);
        }

        System.out.println(CYAN + "✨ OPTIMIZADOR: Optimizaciones completadas." + RESET);
        return codigoOptimizado;
    }

    /**
     * Aplica la propagación de constantes y el plegado de constantes.
     * Reemplaza variables con valores constantes conocidos y evalúa expresiones con solo constantes.
     * @param codigo Lista de instrucciones TAC.
     * @return Nueva lista con propagación y plegado aplicados.
     */
    private List<String> aplicarPropagacionYPlegadoConstantes(List<String> codigo) {
        List<String> nuevoCodigo = new ArrayList<>();
        Map<String, Object> valoresConstantes = new HashMap<>();

        System.out.println(BLUE + "  -> Aplicando Propagación/Plegado de Constantes..." + RESET);

        for (String instruccion : codigo) {
            String instruccionOriginal = instruccion;
            String instruccionModificada = instruccion;

            // Paso 1: Propagar constantes en el lado derecho de la instrucción.
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
            Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);
            Matcher matcherAsignacionLiteral = PATRON_ASIGNACION_LITERAL.matcher(instruccion);

            if (matcherAsignacion.matches()) {
                String lhs = matcherAsignacion.group(1);
                String rhs = matcherAsignacion.group(2);
                for (Map.Entry<String, Object> entrada : valoresConstantes.entrySet()) {
                    String nombreVar = entrada.getKey();
                    Object valorVar = entrada.getValue();
                    rhs = rhs.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
                }
                instruccionModificada = lhs + " = " + rhs;
            } else if (matcherOperacionBinaria.matches()) {
                String lhs = matcherOperacionBinaria.group(1);
                String op1 = matcherOperacionBinaria.group(2);
                String op = matcherOperacionBinaria.group(4);
                String op2 = matcherOperacionBinaria.group(5);
                for (Map.Entry<String, Object> entrada : valoresConstantes.entrySet()) {
                    String nombreVar = entrada.getKey();
                    Object valorVar = entrada.getValue();
                    op1 = op1.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
                    op2 = op2.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
                }
                instruccionModificada = lhs + " = " + op1 + " " + op + " " + op2;
            } else if (matcherOperacionUnaria.matches()) {
                String lhs = matcherOperacionUnaria.group(1);
                String op1 = matcherOperacionUnaria.group(2);
                for (Map.Entry<String, Object> entrada : valoresConstantes.entrySet()) {
                    String nombreVar = entrada.getKey();
                    Object valorVar = entrada.getValue();
                    op1 = op1.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
                }
                instruccionModificada = lhs + " = !" + op1;
            } else {
                // Para otras instrucciones (if, goto, return, push, etc.), propagar en toda la línea.
                for (Map.Entry<String, Object> entrada : valoresConstantes.entrySet()) {
                    String nombreVar = entrada.getKey();
                    Object valorVar = entrada.getValue();
                    instruccionModificada = instruccionModificada.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
                }
            }


            // Paso 2: Plegado de constantes y actualización de valores constantes.
            matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccionModificada);
            matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccionModificada);
            matcherAsignacionLiteral = PATRON_ASIGNACION_LITERAL.matcher(instruccionModificada);
            matcherAsignacion = PATRON_ASIGNACION.matcher(instruccionModificada);

            if (matcherOperacionBinaria.matches()) {
                String varTemporal = matcherOperacionBinaria.group(1);
                String op1Str = matcherOperacionBinaria.group(2);
                String operador = matcherOperacionBinaria.group(4);
                String op2Str = matcherOperacionBinaria.group(5);

                Object op1 = parsearLiteral(op1Str);
                Object op2 = parsearLiteral(op2Str);

                if (op1 != null && op2 != null) {
                    try {
                        Object resultado = evaluarOperacionBinaria(op1, operador, op2);
                        if (resultado != null) {
                            String nuevaInstruccionResultado = varTemporal + " = " + resultado;
                            if (!nuevaInstruccionResultado.equals(instruccionOriginal)) {
                                System.out.println(GREEN + "    - Plegada: '" + instruccionOriginal + "' a '" + nuevaInstruccionResultado + "'" + RESET);
                            }
                            instruccionModificada = nuevaInstruccionResultado;
                            valoresConstantes.put(varTemporal, resultado);
                        }
                    } catch (ArithmeticException e) {
                        System.err.println(YELLOW + "  ⚠️ Advertencia de optimización (Plegado): " + e.getMessage() + " en '" + instruccionOriginal + "'" + RESET);
                    } catch (Exception e) {

                    }
                }
                nuevoCodigo.add(instruccionModificada);
            } else if (matcherOperacionUnaria.matches()) {
                String varTemporal = matcherOperacionUnaria.group(1);
                String opStr = matcherOperacionUnaria.group(2);
                Object op = parsearLiteral(opStr);

                if (op != null) {
                    try {
                        Object resultado = evaluarOperacionUnaria(op, "!");
                        if (resultado != null) {
                            String nuevaInstruccionResultado = varTemporal + " = " + resultado;
                            if (!nuevaInstruccionResultado.equals(instruccionOriginal)) {
                                System.out.println(GREEN + "    - Plegada (Unaria): '" + instruccionOriginal + "' a '" + nuevaInstruccionResultado + "'" + RESET);
                            }
                            instruccionModificada = nuevaInstruccionResultado;
                            valoresConstantes.put(varTemporal, resultado);
                        }
                    } catch (Exception e) {

                    }
                }
                nuevoCodigo.add(instruccionModificada);
            }
            else if (matcherAsignacionLiteral.matches()) {
                String nombreVar = matcherAsignacionLiteral.group(1);
                String valorLiteralStr = matcherAsignacionLiteral.group(2);
                Object valorLiteral = parsearLiteral(valorLiteralStr);

                if (valorLiteral != null) {
                    valoresConstantes.put(nombreVar, valorLiteral);
                } else {
                    if (valorLiteralStr.startsWith("\"") || valorLiteralStr.startsWith("'")) {
                        valoresConstantes.put(nombreVar, valorLiteralStr);
                    }
                }
                nuevoCodigo.add(instruccionModificada);
            } else if (matcherAsignacion.matches()) {
                String nombreVar = matcherAsignacion.group(1);
                String valorAsignado = matcherAsignacion.group(2);

                Object literalAsignado = parsearLiteral(valorAsignado);
                if (literalAsignado != null) {
                    valoresConstantes.put(nombreVar, literalAsignado);
                    instruccionModificada = nombreVar + " = " + literalAsignado;
                } else if (valoresConstantes.containsKey(valorAsignado)) {
                    Object val = valoresConstantes.get(valorAsignado);
                    String nuevaInstruccionAsignacion = nombreVar + " = " + val;
                    if (!nuevaInstruccionAsignacion.equals(instruccionOriginal)) {
                        System.out.println(GREEN + "    - Propagada: '" + instruccionOriginal + "' a '" + nuevaInstruccionAsignacion + "'" + RESET);
                    }
                    instruccionModificada = nuevaInstruccionAsignacion;
                    valoresConstantes.put(nombreVar, val);
                } else if (valoresConstantes.containsKey(nombreVar)) {
                    valoresConstantes.remove(nombreVar);
                }
                nuevoCodigo.add(instruccionModificada);
            } else {
                nuevoCodigo.add(instruccionModificada);
            }
        }
        return nuevoCodigo;
    }

    /**
     * Aplica la simplificación de expresiones aritméticas y lógicas básicas.
     * Busca patrones como `x + 0`, `x * 1`, etc.
     * @param codigo Lista de instrucciones TAC.
     * @return Nueva lista con expresiones simplificadas.
     */
    private List<String> aplicarSimplificacionExpresiones(List<String> codigo) {
        List<String> nuevoCodigo = new ArrayList<>();
        System.out.println(BLUE + "  -> Aplicando Simplificación de Expresiones..." + RESET);

        for (String instruccion : codigo) {
            Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
            String nuevaInstruccion = instruccion;
            String instruccionOriginal = instruccion;

            if (matcherOperacionBinaria.matches()) {
                String varTemporal = matcherOperacionBinaria.group(1);
                String op1Str = matcherOperacionBinaria.group(2);
                String operador = matcherOperacionBinaria.group(4);
                String op2Str = matcherOperacionBinaria.group(5);

                Object op1 = parsearLiteral(op1Str);
                Object op2 = parsearLiteral(op2Str);

                // Reglas de simplificación
                if (operador.equals("+")) {
                    // x + 0 = x
                    if (op2 instanceof Integer && (Integer)op2 == 0) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                    // 0 + x = x
                    else if (op1 instanceof Integer && (Integer)op1 == 0) {
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    }
                } else if (operador.equals("-")) {
                    // x - 0 = x
                    if (op2 instanceof Integer && (Integer)op2 == 0) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("*")) {
                    // 1 * x = x
                    if (op1 instanceof Integer && (Integer)op1 == 1) {
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    }
                    // x * 1 = x
                    else if (op2 instanceof Integer && (Integer)op2 == 1) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                    // x * 0 = 0
                    else if ((op1 instanceof Integer && (Integer)op1 == 0) || (op2 instanceof Integer && (Integer)op2 == 0)) {
                        nuevaInstruccion = varTemporal + " = 0";
                    }
                } else if (operador.equals("/")) {
                    // x / 1 = x
                    if (op2 instanceof Integer && (Integer)op2 == 1) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("&&")) {
                    // false && x = false
                    if (op1 instanceof Boolean && !(Boolean)op1) {
                        nuevaInstruccion = varTemporal + " = false";
                    }
                    // x && false = false
                    else if (op2 instanceof Boolean && !(Boolean)op2) {
                        nuevaInstruccion = varTemporal + " = false";
                    }
                    // true && x = x
                    else if (op1 instanceof Boolean && (Boolean)op1) {
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    }
                    // x && true = x
                    else if (op2 instanceof Boolean && (Boolean)op2) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("||")) {
                    // true || x = true
                    if (op1 instanceof Boolean && (Boolean)op1) {
                        nuevaInstruccion = varTemporal + " = true";
                    }
                    // x || true = true
                    else if (op2 instanceof Boolean && (Boolean)op2) {
                        nuevaInstruccion = varTemporal + " = true";
                    }
                    // false || x = x
                    else if (op1 instanceof Boolean && !(Boolean)op1) {
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    }
                    // x || false = x
                    else if (op2 instanceof Boolean && !(Boolean)op2) {
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                }

                if (!nuevaInstruccion.equals(instruccionOriginal)) {
                    System.out.println(GREEN + "    - Simplificada: '" + instruccionOriginal + "' a '" + nuevaInstruccion + "'" + RESET);
                }
            }
            nuevoCodigo.add(nuevaInstruccion);
        }
        return nuevoCodigo;
    }


    /**
     * Aplica eliminación de código muerto.
     * Identifica y elimina asignaciones a temporales o variables cuyo valor nunca se utiliza
     * después de esa asignación.
     * @param codigo Lista de instrucciones TAC.
     * @return Nueva lista con código muerto eliminado.
     */
    private List<String> aplicarEliminacionCodigoMuerto(List<String> codigo) {
        List<String> codigoOptimizadoFinal = new ArrayList<>();
        Set<String> instruccionesEliminadasLog = new HashSet<>();
        System.out.println(BLUE + "  -> Aplicando Eliminación de Código Muerto..." + RESET);

        Set<String> variablesVivas = new HashSet<>();

        for (int i = codigo.size() - 1; i >= 0; i--) {
            String instruccion = codigo.get(i);

            Pattern patronUsoVar = Pattern.compile("\\b(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*)\\b");
            Matcher matcherUso = patronUsoVar.matcher(instruccion);
            Set<String> usosEnEstaInstruccion = new HashSet<>();
            while (matcherUso.find()) {
                String var = matcherUso.group(1);
                if (parsearLiteral(var) == null && !var.matches("L\\d+")) {
                    usosEnEstaInstruccion.add(var);
                }
            }

            String varDefinida = null;
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            if (matcherAsignacion.matches()) {
                varDefinida = matcherAsignacion.group(1);
            } else {
                Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
                Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);
                if (matcherOperacionBinaria.matches()) {
                    varDefinida = matcherOperacionBinaria.group(1);
                } else if (matcherOperacionUnaria.matches()) {
                    varDefinida = matcherOperacionUnaria.group(1);
                }
            }
            if (instruccion.contains(" = call ") || instruccion.contains(" = pop ")) {
                Matcher callPopMatcher = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (call|pop) .*").matcher(instruccion);
                if (callPopMatcher.matches()) {
                    varDefinida = callPopMatcher.group(1);
                }
            }

            variablesVivas.addAll(usosEnEstaInstruccion);

            if (varDefinida != null && parsearLiteral(varDefinida) == null && !varDefinida.matches("L\\d+")) {
                variablesVivas.remove(varDefinida);
            }
        }

        for (int i = 0; i < codigo.size(); i++) {
            String instruccion = codigo.get(i);

            String varDefinida = null;
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            if (matcherAsignacion.matches()) {
                varDefinida = matcherAsignacion.group(1);
            } else {
                Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
                Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);
                if (matcherOperacionBinaria.matches()) {
                    varDefinida = matcherOperacionBinaria.group(1);
                } else if (matcherOperacionUnaria.matches()) {
                    varDefinida = matcherOperacionUnaria.group(1);
                }
            }
            if (instruccion.contains(" = call ") || instruccion.contains(" = pop ")) {
                Matcher callPopMatcher = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (call|pop) .*").matcher(instruccion);
                if (callPopMatcher.matches()) {
                    varDefinida = callPopMatcher.group(1);
                }
            }

            boolean esCodigoMuerto = false;

            if (varDefinida != null && !varDefinida.matches("L\\d+")) {
                if (!variablesVivas.contains(varDefinida)) {
                    if (varDefinida.startsWith("t")) {
                        esCodigoMuerto = true;
                    }
                }
            }

            if (instruccion.contains(":") || instruccion.startsWith("goto ") || instruccion.startsWith("if ") ||
                    instruccion.startsWith("return ") || instruccion.startsWith("push ") || instruccion.contains(" = call ") || instruccion.contains(" = pop ")) {
                esCodigoMuerto = false;
            }

            if (esCodigoMuerto) {
                instruccionesEliminadasLog.add(instruccion);
            } else {
                codigoOptimizadoFinal.add(instruccion);
            }
        }
        instruccionesEliminadasLog.forEach(inst -> System.out.println(RED + "    - Eliminada instrucción muerta: " + inst + RESET));

        return codigoOptimizadoFinal;
    }

    /**
     * Intenta parsear una cadena como un literal (Integer, Double, Boolean).
     * @param s Cadena a parsear.
     * @return El objeto literal o null si no es un literal reconocido.
     */
    private Object parsearLiteral(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                if ("true".equalsIgnoreCase(s)) {
                    return true;
                } else if ("false".equalsIgnoreCase(s)) {
                    return false;
                }
                return null;
            }
        }
    }

    /**
     * Evalúa una operación binaria entre dos objetos literales.
     * Soporta Integer y Double.
     * @param op1 Primer operando.
     * @param operador Operador.
     * @param op2 Segundo operando.
     * @return Resultado de la operación.
     * @throws ArithmeticException Si hay división por cero.
     * @throws IllegalArgumentException Si los tipos o el operador no son compatibles.
     */
    private Object evaluarOperacionBinaria(Object op1, String operador, Object op2) {
        if (op1 instanceof Integer && op2 instanceof Integer) {
            int val1 = (Integer) op1;
            int val2 = (Integer) op2;
            switch (operador) {
                case "+": return val1 + val2;
                case "-": return val1 - val2;
                case "*": return val1 * val2;
                case "/":
                    if (val2 == 0) throw new ArithmeticException("División por cero (plegado)");
                    return val1 / val2;
                case "%":
                    if (val2 == 0) throw new ArithmeticException("Módulo por cero (plegado)");
                    return val1 % val2;
                case "<": return val1 < val2;
                case ">": return val1 > val2;
                case "<=": return val1 <= val2;
                case ">=": return val1 >= val2;
                case "==": return val1 == val2;
                case "!=": return val1 != val2;
            }
        } else if (op1 instanceof Double || op2 instanceof Double) {
            double val1 = (op1 instanceof Integer) ? ((Integer) op1).doubleValue() : (Double) op1;
            double val2 = (op2 instanceof Integer) ? ((Integer) op2).doubleValue() : (Double) op2;
            switch (operador) {
                case "+": return val1 + val2;
                case "-": return val1 - val2;
                case "*": return val1 * val2;
                case "/":
                    if (val2 == 0.0) throw new ArithmeticException("División por cero (plegado)");
                    return val1 / val2;
                case "%":
                    if (val2 == 0.0) throw new ArithmeticException("Módulo por cero (plegado)");
                    return val1 % val2;
                case "<": return val1 < val2;
                case ">": return val1 > val2;
                case "<=": return val1 <= val2;
                case ">=": return val1 >= val2;
                case "==": return val1 == val2;
                case "!=": return val1 != val2;
            }
        } else if (op1 instanceof Boolean && op2 instanceof Boolean) {
            boolean val1 = (Boolean) op1;
            boolean val2 = (Boolean) op2;
            switch (operador) {
                case "&&": return val1 && val2;
                case "||": return val1 || val2;
                case "==": return val1 == val2;
                case "!=": return val1 != val2;
            }
        }
        throw new IllegalArgumentException("Operación no soportada o tipos incompatibles para plegado: " + op1.getClass().getSimpleName() + " " + operador + " " + op2.getClass().getSimpleName());
    }

    /**
     * Evalúa una operación unaria.
     * @param op Operando.
     * @param operador Operador.
     * @return Resultado de la operación.
     */
    private Object evaluarOperacionUnaria(Object op, String operador) {
        if (operador.equals("!") && op instanceof Boolean) {
            return !(Boolean) op;
        } else if (operador.equals("!") && op instanceof Integer) {
            return (Integer)op == 0 ? 1 : 0;
        }
        throw new IllegalArgumentException("Operación unaria no soportada o tipo incompatible: " + operador + op.getClass().getSimpleName());
    }
}