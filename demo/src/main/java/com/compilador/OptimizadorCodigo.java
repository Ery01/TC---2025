package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase para aplicar optimizaciones al código de tres direcciones (TAC).
 */
public class OptimizadorCodigo {

    // Patrones regex para parsear instrucciones TAC
    // Asignación: VAR = EXPR (ej: a = t0, t0 = 5, t1 = a + b)
    private static final Pattern PATRON_ASIGNACION = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (.+)$");
    // Operación binaria: TEMP = OP1 OPERADOR OP2
    private static final Pattern PATRON_OPERACION_BINARIA = Pattern.compile("^(t\\d+) = ([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|\".*\"|'.*'|true|false) ([+\\-*/%<>=!&|]{1,2}) ([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|\".*\"|'.*'|true|false)$");
    // Operación unaria (negación): TEMP = !OP1
    private static final Pattern PATRON_OPERACION_UNARIA = Pattern.compile("^(t\\d+) = !([a-zA-Z_][a-zA-Z0-9_]*|t\\d+|\\d+(\\.\\d+)?|true|false)$");
    // Asignación simple (literal): TEMP = LITERAL
    private static final Pattern PATRON_ASIGNACION_LITERAL = Pattern.compile("^(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*) = (\\d+(\\.\\d+)?|\".*\"|'.*'|true|false)$");

    public OptimizadorCodigo() {
        System.out.println("✨ OPTIMIZADOR: Iniciado.");
    }

    /**
     * Aplica un conjunto de optimizaciones al código TAC dado.
     * @param codigoTac La lista de instrucciones TAC sin optimizar.
     * @return La lista de instrucciones TAC optimizadas.
     */
    public List<String> optimizar(List<String> codigoTac) {
        List<String> codigoOptimizado = new ArrayList<>(codigoTac); // Trabajar sobre una copia

        System.out.println("✨ OPTIMIZADOR: Aplicando optimizaciones...");

        // Iterar varias veces para maximizar el efecto (ej. la propagación puede habilitar más eliminaciones)
        for (int i = 0; i < 3; i++) { // Ejecutar 3 pasadas de optimización
            codigoOptimizado = aplicarPropagacionYPlegadoConstantes(codigoOptimizado);
            codigoOptimizado = aplicarSimplificacionExpresiones(codigoOptimizado); // Aplica simplificación después de plegado
            codigoOptimizado = aplicarEliminacionCodigoMuerto(codigoOptimizado);
        }

        System.out.println("✨ OPTIMIZADOR: Optimizaciones completadas.");
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
        Map<String, Object> valoresConstantes = new HashMap<>(); // Almacena (variable -> valor_constante)

        System.out.println("  -> Aplicando Propagación/Plegado de Constantes...");

        for (String instruccion : codigo) {
            String instruccionOriginal = instruccion; // Mantener la instrucción original para mensajes

            // 1. Reemplazar variables/temporales con sus valores constantes conocidos
            for (Map.Entry<String, Object> entrada : valoresConstantes.entrySet()) {
                String nombreVar = entrada.getKey();
                Object valorVar = entrada.getValue();

                // Reemplazar la variable/temporal con su valor constante
                // Cuidado: solo reemplazar si es una variable completa, no parte de un ID más largo
                instruccion = instruccion.replaceAll("\\b" + Pattern.quote(nombreVar) + "\\b", String.valueOf(valorVar));
            }

            // 2. Intentar parsear y procesar la instrucción
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
            Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);
            Matcher matcherAsignacionLiteral = PATRON_ASIGNACION_LITERAL.matcher(instruccion);

            if (matcherOperacionBinaria.matches()) {
                String varTemporal = matcherOperacionBinaria.group(1);
                String op1Str = matcherOperacionBinaria.group(2);
                String operador = matcherOperacionBinaria.group(4);
                String op2Str = matcherOperacionBinaria.group(5);

                // Intentar plegado de constantes
                Object op1 = parsearLiteral(op1Str);
                Object op2 = parsearLiteral(op2Str);

                if (op1 != null && op2 != null) {
                    try {
                        Object resultado = evaluarOperacionBinaria(op1, operador, op2);
                        if (resultado != null) {
                            String nuevaInstruccionResultado = varTemporal + " = " + resultado;
                            if (!nuevaInstruccionResultado.equals(instruccion)) {
                                System.out.println("    - Plegada: '" + instruccionOriginal + "' a '" + nuevaInstruccionResultado + "'");
                            }
                            instruccion = nuevaInstruccionResultado;
                            valoresConstantes.put(varTemporal, resultado); // Actualiza el valor constante para la temporal
                        }
                    } catch (ArithmeticException e) {
                        // División por cero en plegado, mantener la instrucción original para que el runtime la maneje
                        System.err.println("  ⚠️ Advertencia de optimización (Plegado): " + e.getMessage() + " en '" + instruccionOriginal + "'");
                    } catch (Exception e) {
                        // Otros errores de evaluación en plegado, mantener la instrucción original
                    }
                }
                nuevoCodigo.add(instruccion);
            } else if (matcherOperacionUnaria.matches()) {
                String varTemporal = matcherOperacionUnaria.group(1);
                String opStr = matcherOperacionUnaria.group(2);
                Object op = parsearLiteral(opStr);

                if (op != null) {
                    try {
                        Object resultado = evaluarOperacionUnaria(op, "!"); // Solo soporta '!' por ahora
                        if (resultado != null) {
                            String nuevaInstruccionResultado = varTemporal + " = " + resultado;
                            if (!nuevaInstruccionResultado.equals(instruccion)) {
                                System.out.println("    - Plegada (Unaria): '" + instruccionOriginal + "' a '" + nuevaInstruccionResultado + "'");
                            }
                            instruccion = nuevaInstruccionResultado;
                            valoresConstantes.put(varTemporal, resultado);
                        }
                    } catch (Exception e) {
                        // Errores en plegado unario
                    }
                }
                nuevoCodigo.add(instruccion);
            }
            else if (matcherAsignacionLiteral.matches()) {
                String nombreVar = matcherAsignacionLiteral.group(1);
                String valorLiteralStr = matcherAsignacionLiteral.group(2);
                Object valorLiteral = parsearLiteral(valorLiteralStr);

                if (valorLiteral != null) {
                    valoresConstantes.put(nombreVar, valorLiteral); // Guardar valor constante
                } else {
                    // Para cadenas y caracteres, simplemente almacenamos el string literal.
                    if (valorLiteralStr.startsWith("\"") || valorLiteralStr.startsWith("'")) {
                        valoresConstantes.put(nombreVar, valorLiteralStr);
                    }
                }
                nuevoCodigo.add(instruccion);
            } else if (matcherAsignacion.matches()) {
                String nombreVar = matcherAsignacion.group(1);
                String valorAsignado = matcherAsignacion.group(2);

                // Si el valor asignado es una temporal cuyo valor ya es una constante, propagarla
                if (valoresConstantes.containsKey(valorAsignado)) {
                    Object valor = valoresConstantes.get(valorAsignado);
                    String nuevaInstruccionAsignacion = nombreVar + " = " + valor;
                    if (!nuevaInstruccionAsignacion.equals(instruccion)) {
                        System.out.println("    - Propagada: '" + instruccionOriginal + "' a '" + nuevaInstruccionAsignacion + "'");
                    }
                    instruccion = nuevaInstruccionAsignacion;
                    valoresConstantes.put(nombreVar, valor); // La variable ahora también es una constante
                } else if (valoresConstantes.containsKey(nombreVar)) {
                    // Si la variable que se está asignando era una constante, pero ahora se le asigna algo no constante,
                    // dejar de considerarla constante.
                    valoresConstantes.remove(nombreVar);
                }
                nuevoCodigo.add(instruccion);
            } else {
                // Para otras instrucciones (labels, goto, call, return, push), simplemente las añadimos
                nuevoCodigo.add(instruccion);
                // Si la instrucción es una llamada a función o un salto, invalida la información de constantes
                // ya que podría haber efectos secundarios. En una optimización más compleja, esto sería más granular.
                if (instruccion.matches(".*(call|goto|if !.*|return).*")) {
                    // En un compilador real, se usaría un análisis más preciso para invalidar constantes afectadas.
                    // Para esta implementación simple, simplemente no podemos garantizar que las constantes sigan siendo válidas
                    // a través de llamadas a funciones o saltos arbitrarios.
                    // valoresConstantes.clear(); // Conservador: borrar todas las constantes conocidas
                    // Para evitar perder oportunidades de optimización en bucles, no limpiar todo siempre.
                    // Esto es un tradeoff entre simplicidad y agresividad de la optimización.
                }
            }
        }
        return nuevoCodigo;
    }

    /**
     * Aplica eliminación de código muerto (asignaciones a temporales no usadas).
     * Nota: Esta es una eliminación de código muerto muy simple y conservadora.
     * Una DCE completa requiere análisis de vivo (liveness analysis) y grafos de flujo de control.
     * @param codigo Lista de instrucciones TAC.
     * @return Nueva lista con código muerto simple eliminado.
     */
    private List<String> aplicarEliminacionCodigoMuerto(List<String> codigo) {
        List<String> codigoFinalOptimizado = new ArrayList<>();
        List<String> instruccionesEliminadas = new ArrayList<>();

        // Paso para identificar las temporales no usadas
        // Recorremos el código hacia adelante para encontrar usos futuros de temporales.
        // Esto es una simplificación de un análisis de vivo (liveness analysis) real.

        // Creamos un set de todas las temporales definidas.
        Map<String, Boolean> temporalesDefinidas = new HashMap<>(); // temporal -> está definida en alguna asignación
        // Creamos un set de todas las temporales usadas.
        Map<String, Boolean> temporalesUsadas = new HashMap<>(); // temporal -> está usada en alguna expresión

        // Primera pasada: Identificar todas las temporales definidas y usadas
        for (String instruccion : codigo) {
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
            Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);

            String varDefinida = null;
            if (matcherAsignacion.matches()) {
                varDefinida = matcherAsignacion.group(1);
            } else if (matcherOperacionBinaria.matches()) {
                varDefinida = matcherOperacionBinaria.group(1);
            } else if (matcherOperacionUnaria.matches()) {
                varDefinida = matcherOperacionUnaria.group(1);
            }

            if (varDefinida != null && varDefinida.startsWith("t")) {
                temporalesDefinidas.put(varDefinida, true);
            }

            // Identificar usos en el lado derecho de asignaciones, operaciones, condiciones, llamadas, retornos.
            Pattern patronUsoVar = Pattern.compile("\\b(t\\d+|[a-zA-Z_][a-zA-Z0-9_]*)\\b");
            Matcher matcherUso = patronUsoVar.matcher(instruccion);
            while (matcherUso.find()) {
                String varUsada = matcherUso.group(1);
                // Asegurarse de que no estamos registrando la propia variable definida en la instrucción actual
                // (ej. si 'x = x + 1', 'x' es definida y usada, pero la definición no es muerta por sí misma).
                // Y que no sea un literal o una etiqueta.
                if (parsearLiteral(varUsada) == null && !varUsada.matches("L\\d+")) {
                    temporalesUsadas.put(varUsada, true);
                }
            }
        }

        // Segunda pasada: Construir el código optimizado, eliminando las temporales no usadas.
        // Una temporal 'tX' se considera muerta si se define, pero nunca se usa *después* de su última definición.
        // Para esta implementación simple, eliminamos una asignación a 'tX' si 'tX' nunca se usa en *todo el código*.
        // Esto es una heurística agresiva y simplificada.

        System.out.println("  -> Aplicando Eliminación de Código Muerto (simple)...");

        for (String instruccion : codigo) {
            Matcher matcherAsignacion = PATRON_ASIGNACION.matcher(instruccion);
            Matcher matcherOperacionBinaria = PATRON_OPERACION_BINARIA.matcher(instruccion);
            Matcher matcherOperacionUnaria = PATRON_OPERACION_UNARIA.matcher(instruccion);

            String varDefinida = null;
            if (matcherAsignacion.matches()) {
                varDefinida = matcherAsignacion.group(1);
            } else if (matcherOperacionBinaria.matches()) {
                varDefinida = matcherOperacionBinaria.group(1);
            } else if (matcherOperacionUnaria.matches()) {
                varDefinida = matcherOperacionUnaria.group(1);
            }

            boolean debeMantenerse = true;
            if (varDefinida != null && varDefinida.startsWith("t")) { // Solo considerar temporales para eliminación
                if (!temporalesUsadas.getOrDefault(varDefinida, false)) {
                    // Si la temporal fue definida pero nunca usada en ninguna parte del código, es código muerto.
                    instruccionesEliminadas.add(instruccion);
                    debeMantenerse = false;
                }
            }

            if (debeMantenerse) {
                codigoFinalOptimizado.add(instruccion);
            }
        }

        instruccionesEliminadas.forEach(inst -> System.out.println("    - Eliminada instrucción muerta: " + inst));

        return codigoFinalOptimizado;
    }


    /**
     * Aplica la simplificación de expresiones aritméticas y lógicas básicas.
     * Busca patrones como `x + 0`, `x * 1`, etc.
     * @param codigo Lista de instrucciones TAC.
     * @return Nueva lista con expresiones simplificadas.
     */
    private List<String> aplicarSimplificacionExpresiones(List<String> codigo) {
        List<String> nuevoCodigo = new ArrayList<>();
        System.out.println("  -> Aplicando Simplificación de Expresiones...");

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
                    if (op2 instanceof Integer && (Integer)op2 == 0) { // x + 0 = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    } else if (op1 instanceof Integer && (Integer)op1 == 0) { // 0 + x = x
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    }
                } else if (operador.equals("-")) {
                    if (op2 instanceof Integer && (Integer)op2 == 0) { // x - 0 = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("*")) {
                    if (op1 instanceof Integer && (Integer)op1 == 1) { // 1 * x = x
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    } else if (op2 instanceof Integer && (Integer)op2 == 1) { // x * 1 = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    } else if ((op1 instanceof Integer && (Integer)op1 == 0) || (op2 instanceof Integer && (Integer)op2 == 0)) { // x * 0 = 0
                        nuevaInstruccion = varTemporal + " = 0";
                    }
                } else if (operador.equals("/")) {
                    if (op2 instanceof Integer && (Integer)op2 == 1) { // x / 1 = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("&&")) {
                    if (op1 instanceof Boolean && !(Boolean)op1) { // false && x = false
                        nuevaInstruccion = varTemporal + " = false";
                    } else if (op2 instanceof Boolean && !(Boolean)op2) { // x && false = false
                        nuevaInstruccion = varTemporal + " = false";
                    } else if (op1 instanceof Boolean && (Boolean)op1) { // true && x = x
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    } else if (op2 instanceof Boolean && (Boolean)op2) { // x && true = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                } else if (operador.equals("||")) {
                    if (op1 instanceof Boolean && (Boolean)op1) { // true || x = true
                        nuevaInstruccion = varTemporal + " = true";
                    } else if (op2 instanceof Boolean && (Boolean)op2) { // x || true = true
                        nuevaInstruccion = varTemporal + " = true";
                    } else if (op1 instanceof Boolean && !(Boolean)op1) { // false || x = x
                        nuevaInstruccion = varTemporal + " = " + op2Str;
                    } else if (op2 instanceof Boolean && !(Boolean)op2) { // x || false = x
                        nuevaInstruccion = varTemporal + " = " + op1Str;
                    }
                }
                if (!nuevaInstruccion.equals(instruccionOriginal)) {
                    System.out.println("    - Simplificada: '" + instruccionOriginal + "' a '" + nuevaInstruccion + "'");
                }
            }
            // Para otras instrucciones (asignaciones literales ya simplificadas por plegado)
            nuevoCodigo.add(nuevaInstruccion);
        }
        return nuevoCodigo;
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
                // Si es un ID, temporal, string o char literal, no es un literal numérico/booleano para plegado aquí.
                // Los string/char literals se manejan en propagación como strings.
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
        } else if (operador.equals("!") && op instanceof Integer) { // Asumiendo que !0=1, !cualquierOtro=0
            return (Integer)op == 0 ? 1 : 0;
        }
        throw new IllegalArgumentException("Operación unaria no soportada o tipo incompatible: " + operador + op.getClass().getSimpleName());
    }
}
