package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack; // ¡Nuevo import!
import java.util.Comparator; // Para ordenar al imprimir
import java.util.stream.Collectors; // Para ordenar al imprimir

/**
 * Implementación sencilla de una tabla de símbolos para el compilador
 */
public class TablaSimbolos {

    // Clase para representar un símbolo
    public static class Simbolo {
        private String nombre;
        private String tipo;        // int, char, double, void
        private String categoria;   // variable, funcion, parametro
        private int linea;
        private int columna;
        private String ambito;      // global o nombre_funcion o bloque_X
        private List<String> parametros;  // Solo para funciones (lista de tipos de parámetros esperados)
        private Object valor; // Usado para el evaluador de expresiones si es constante
        private boolean usada;
        private boolean inicializada;

        public Simbolo(String nombre, String tipo, String categoria, int linea, int columna, String ambito,  List<String> parametros) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.categoria = categoria;
            this.linea = linea;
            this.columna = columna;
            this.ambito = ambito;
            this.valor = null; // Valor inicial para variables
            this.parametros = parametros != null ? new ArrayList<>(parametros) : new ArrayList<>();
            this.usada = false;
            this.inicializada = false;
        }

        public Simbolo(String nombre, String tipo, String categoria, int linea, int columna, String ambito) {
            this(nombre, tipo, categoria, linea, columna, ambito, new ArrayList<>());
        }

        // Getters
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public String getCategoria() { return categoria; }
        public int getLinea() { return linea; }
        public int getColumna() { return columna; }
        public String getAmbito() { return ambito; }
        public List<String> getParametros() { return parametros; }
        public Object getValor() { return valor; }
        public boolean isUsada() { return usada; }
        public boolean isInicializada() { return inicializada; }

        // Setters
        public void setValor(Object valor) { this.valor = valor; }
        public void setUsada(boolean usada) { this.usada = usada; }
        public void setInicializada(boolean inicializada) { this.inicializada = inicializada; }

        // Agregar un parámetro a una función (solo para el símbolo de la función)
        public void addParametro(String tipo) {
            this.parametros.add(tipo);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-15s %-10s %-15s %-8d %-10d %-15s %-12b %-12b",
                    nombre, tipo, categoria, linea, columna, ambito, inicializada, usada));

            if (categoria.equals("funcion") && !parametros.isEmpty()) {
                sb.append(" (");
                for (int i = 0; i < parametros.size(); i++) {
                    sb.append(parametros.get(i));
                    if (i < parametros.size() - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(")");
            }

            return sb.toString();
        }
    }

    // Usaremos un mapa para almacenar símbolos, con una clave compuesta de nombre y ámbito
    private Map<String, Simbolo> simbolosMap; // Clave: "nombre_ambito"

    /**
     * Constructor
     */
    public TablaSimbolos() {
        this.simbolosMap = new HashMap<>();
    }

    // Eliminamos setAmbito y getAmbito ya que el manejo de ámbito se hará con una pila externa
    // y la búsqueda los usará como parámetro.

    /**
     * Agrega un símbolo a la tabla.
     * La clave interna es `nombre_ambito` para permitir mismos nombres en diferentes ámbitos.
     * @param simbolo Símbolo a agregar
     * @return true si se agregó correctamente, false si ya existía en el mismo ámbito
     */
    public boolean agregar(Simbolo simbolo) {
        String clave = simbolo.getNombre() + "_" + simbolo.getAmbito();
        if (simbolosMap.containsKey(clave)) {
            return false; // Ya existe un símbolo con este nombre en este ámbito
        }
        simbolosMap.put(clave, simbolo);
        return true;
    }

    /**
     * Busca un símbolo por nombre utilizando la pila de ámbitos.
     * Busca desde el ámbito más interno (cima de la pila) hacia el global.
     * @param nombre Nombre del símbolo a buscar
     * @param ambitoStack Pila de ámbitos actual
     * @return El símbolo encontrado o null si no existe
     */
    public Simbolo buscar(String nombre, Stack<String> ambitoStack) {
        // Recorre la pila de ámbitos desde el más interno (cima) hasta el global
        for (int i = ambitoStack.size() - 1; i >= 0; i--) {
            String currentAmbito = ambitoStack.get(i);
            String clave = nombre + "_" + currentAmbito;
            if (simbolosMap.containsKey(clave)) {
                return simbolosMap.get(clave);
            }
        }
        // Si no se encuentra en ningún ámbito específico,
        // podrías querer buscar funciones que siempre son globales y podrían no tener "global" en su clave
        // dependiendo de cómo las agregues.
        // Si las funciones se agregan explícitamente con ámbito "global", la búsqueda anterior las encontrará.
        return null;
    }

    /**
     * Busca un símbolo por nombre y ámbito específico.
     * Útil para buscar funciones (que suelen estar en "global") o para depuración.
     * @param nombre Nombre del símbolo
     * @param ambito Ámbito donde buscar
     * @return El símbolo encontrado o null si no existe
     */
    public Simbolo buscarEnAmbitoDirecto(String nombre, String ambito) {
        String clave = nombre + "_" + ambito;
        return simbolosMap.get(clave);
    }


    /**
     * Obtiene todos los símbolos en la tabla.
     * @return Una colección de todos los símbolos.
     */
    public List<Simbolo> getTodosSimbolos() {
        return new ArrayList<>(simbolosMap.values());
    }

    /**
     * Imprime la tabla de símbolos.
     */
    public void imprimir() {
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        System.out.printf("%-15s %-10s %-15s %-8s %-10s %-15s %-12s %-12s %s\n",
                "NOMBRE", "TIPO", "CAT.", "LÍNEA", "COLUMNA", "ÁMBITO", "INICIALIZADA", "USADA", "PARÁMETROS");
        System.out.println("--------------------------------------------------------------------------------------------------------------------");

        // Ordenar los símbolos para una salida consistente
        simbolosMap.values().stream()
                .sorted(Comparator.comparing(Simbolo::getAmbito)
                        .thenComparing(Simbolo::getLinea)
                        .thenComparing(Simbolo::getColumna))
                .forEach(System.out::println);
    }

    /**
     * Verifica la compatibilidad de los parámetros de una llamada a función.
     * @param nombreFuncion Nombre de la función
     * @param tiposArgumentos Tipos de los argumentos pasados en la llamada
     * @return true si los parámetros coinciden, false en caso contrario
     */
    public boolean verificarParametros(String nombreFuncion, List<String> tiposArgumentos) {
        // Asumimos que las funciones siempre se buscan en el ámbito "global"
        Simbolo funcionSimbolo = buscarEnAmbitoDirecto(nombreFuncion, "global");

        if (funcionSimbolo == null || !funcionSimbolo.getCategoria().equals("funcion")) {
            return false; // La función no existe o no es una función
        }

        List<String> tiposEsperados = funcionSimbolo.getParametros();

        if (tiposEsperados.size() != tiposArgumentos.size()) {
            return false; // Número de argumentos diferente
        }

        for (int i = 0; i < tiposEsperados.size(); i++) {
            if (!esTipoCompatible(tiposEsperados.get(i), tiposArgumentos.get(i))) {
                return false; // Tipos no compatibles
            }
        }
        return true;
    }

    /**
     * Auxiliar para la compatibilidad de tipos (puedes expandirlo).
     */
    private boolean esTipoCompatible(String esperado, String real) {
        if (esperado.equals(real)) return true;
        // int se puede asignar a double
        if (esperado.equals("double") && real.equals("int")) return true;
        // char se puede asignar a int o double
        if (esperado.equals("int") && real.equals("char")) return true;
        if (esperado.equals("double") && real.equals("char")) return true;

        return false;
    }
}