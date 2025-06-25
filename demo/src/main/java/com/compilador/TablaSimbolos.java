package com.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Comparator;

/**
 * Implementación sencilla de una tabla de símbolos para el compilador
 */
public class TablaSimbolos {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static class Simbolo {
        private String nombre;
        private String tipo;        // int, char, double, void, bool
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
    private Map<String, Simbolo> simbolosMap; // Clave: "nombre_ambito"

    /**
     * Constructor
     */
    public TablaSimbolos() {
        this.simbolosMap = new HashMap<>();
    }

    /**
     * Agrega un símbolo a la tabla.
     * La clave interna es `nombre_ambito` para permitir mismos nombres en diferentes ámbitos.
     * @param simbolo Símbolo a agregar
     * @return true si se agregó correctamente, false si ya existía en el mismo ámbito
     */
    public boolean agregar(Simbolo simbolo) {
        String clave = simbolo.getNombre() + "_" + simbolo.getAmbito();
        if (simbolosMap.containsKey(clave)) {
            return false;
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
        for (int i = ambitoStack.size() - 1; i >= 0; i--) {
            String currentAmbito = ambitoStack.get(i);
            String clave = nombre + "_" + currentAmbito;
            if (simbolosMap.containsKey(clave)) {
                return simbolosMap.get(clave);
            }
        }
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
     * Imprime la tabla de símbolos con formato y colores.
     */
    public void imprimir() {
        System.out.printf(MAGENTA + BOLD + "%-15s %-10s %-15s %-8s %-10s %-15s %-12s %-12s %s\n" + RESET,
                "NOMBRE", "TIPO", "CAT.", "LÍNEA", "COLUMNA", "ÁMBITO", "INICIALIZADA", "USADA", "PARÁMETROS");
        System.out.println(MAGENTA + BOLD + "--------------------------------------------------------------------------------------------------------------------" + RESET);

        simbolosMap.values().stream().sorted(Comparator.comparing(Simbolo::getAmbito)
                        .thenComparing(Simbolo::getLinea)
                        .thenComparing(Simbolo::getColumna))
                .forEach(simbolo -> {
                    String colorSimbolo = RESET;

                    if (!simbolo.isUsada() && simbolo.getCategoria().equals("variable")) {
                        colorSimbolo = YELLOW;
                    } else if (simbolo.getCategoria().equals("funcion") || simbolo.getCategoria().equals("parametro")) {
                        colorSimbolo = BLUE;
                    } else if (simbolo.getCategoria().equals("variable") && simbolo.isUsada()) {
                        colorSimbolo = WHITE;
                    }

                    System.out.printf("%s%-15s %-10s %-15s %-8d %-10d %-15s ",
                            colorSimbolo,
                            simbolo.getNombre(),
                            simbolo.getTipo(),
                            simbolo.getCategoria(),
                            simbolo.getLinea(),
                            simbolo.getColumna(),
                            simbolo.getAmbito());

                    System.out.print((simbolo.isInicializada() ? GREEN : RED) + String.format("%-12b", simbolo.isInicializada()) + RESET + " ");
                    System.out.print((simbolo.isUsada() ? GREEN : RED) + String.format("%-12b", simbolo.isUsada()) + RESET);

                    if (simbolo.getCategoria().equals("funcion") && !simbolo.getParametros().isEmpty()) {
                        StringBuilder params = new StringBuilder(" (");
                        for (int i = 0; i < simbolo.getParametros().size(); i++) {
                            params.append(simbolo.getParametros().get(i));
                            if (i < simbolo.getParametros().size() - 1) {
                                params.append(", ");
                            }
                        }
                        params.append(")");
                        System.out.print(BLUE + params.toString() + RESET);
                    }
                    System.out.println(RESET);
                });
    }

    /**
     * Verifica la compatibilidad de los parámetros de una llamada a función.
     * @param nombreFuncion Nombre de la función
     * @param tiposArgumentos Tipos de los argumentos pasados en la llamada
     * @return true si los parámetros coinciden, false en caso contrario
     */
    public boolean verificarParametros(String nombreFuncion, List<String> tiposArgumentos) {
        Simbolo funcionSimbolo = buscarEnAmbitoDirecto(nombreFuncion, "global");

        if (funcionSimbolo == null || !funcionSimbolo.getCategoria().equals("funcion")) {
            return false;
        }

        List<String> tiposEsperados = funcionSimbolo.getParametros();

        if (tiposEsperados.size() != tiposArgumentos.size()) {
            return false;
        }

        for (int i = 0; i < tiposEsperados.size(); i++) {
            if (!esTipoCompatible(tiposEsperados.get(i), tiposArgumentos.get(i))) {
                return false;
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
