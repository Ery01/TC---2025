package com.compilador;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generador de Código de Tres Direcciones.
 * Simplificado para demostrar los conceptos.
 */
public class GeneradorCodigo {

    private List<String> codigo;
    private int tempCounter;
    private int labelCounter;

    public GeneradorCodigo() {
        this.codigo = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
    }

    /**
     * Genera un nuevo nombre de variable temporal.
     * @return Nombre de la nueva temporal (ej. "t0", "t1").
     */
    public String newTemp() {
        return "t" + (tempCounter++);
    }

    /**
     * Genera una nueva etiqueta.
     * @return Nombre de la nueva etiqueta (ej. "L0", "L1").
     */
    public String newLabel() {
        return "L" + (labelCounter++);
    }

    /**
     * Genera código para una operación binaria: `temp = left op right`.
     * @param op Operador (ej. "+", "-", "*", "/", "==", ">").
     * @param left Operando izquierdo.
     * @param right Operando derecho.
     * @return El nombre de la temporal donde se almacena el resultado.
     */
    public String genOperacionBinaria(String op, String left, String right) {
        String temp = newTemp();
        String instruccion = temp + " = " + left + " " + op + " " + right;
        codigo.add(instruccion);
        return temp;
    }

    /**
     * Genera código para una asignación: `variable = valor`.
     * @param variable Nombre de la variable destino.
     * @param valor El valor o nombre de la temporal/variable a asignar.
     */
    public void genAsignacion(String variable, String valor) {
        String instruccion = variable + " = " + valor;
        codigo.add(instruccion);
    }

    /**
     * Genera una etiqueta en el código: `LABEL:`.
     * @param label Nombre de la etiqueta.
     */
    public void genLabel(String label) {
        String instruccion = label + ":";
        codigo.add(instruccion);
    }

    /**
     * Genera un salto condicional: `if !condicion goto label`.
     * @param condicion La variable temporal o nombre que contiene la condición.
     * @param label La etiqueta a la que saltar si la condición es falsa.
     */
    public void genIfFalse(String condicion, String label) {
        String instruccion = "if !" + condicion + " goto " + label;
        codigo.add(instruccion);
    }

    /**
     * Genera un salto incondicional: `goto label`.
     * @param label La etiqueta a la que saltar.
     */
    public void genGoto(String label) {
        String instruccion = "goto " + label;
        codigo.add(instruccion);
    }

    /**
     * Genera una instruccion generica de tres direcciones.
     * Este metodo es util para instrucciones que no tienen un metodo 'gen' especifico.
     * @param instruccion La instruccion TAC completa como String.
     */
    public void genInstruccion(String instruccion) {
        codigo.add(instruccion);
    }

    /**
     * Obtiene la lista de todas las instrucciones de código de tres direcciones generadas.
     * @return Lista de strings representando las instrucciones TAC.
     */
    public List<String> getCodigo() {
        return codigo;
    }

    /**
     * Imprime el código generado en la consola, con números de línea.
     */
    public void imprimirCodigo() {
        for (int i = 0; i < codigo.size(); i++) {
            System.out.printf("%3d: %s\n", i, codigo.get(i));
        }
        System.out.println("Total instrucciones: " + codigo.size());
    }

    /**
     * Para compatibilidad: Este método no está implementado en esta versión simple.
     * @return Siempre `null`.
     */
    public Map<String, String> getTiposVariables() { return null; }

    /**
     * Para compatibilidad: Este método no está implementado en esta versión simple.
     */
    public void imprimirTipos() { }

    /**
     * Imprime estadísticas sobre el código intermedio generado.
     */
    public void imprimirEstadisticas() {
        System.out.println("\n📊 ESTADÍSTICAS:");
        System.out.println("   - Temporales creados: " + tempCounter);
        System.out.println("   - Etiquetas creadas: " + labelCounter);
        System.out.println("   - Instrucciones totales: " + codigo.size());
    }
}
