package com.compilador;

import org.antlr.v4.runtime.tree.TerminalNode;
import com.compilador.CompiladorParser.*;  // Importa todos los contextos del parser
import com.compilador.EvaluadorExpresiones;
import org.antlr.v4.runtime.tree.TerminalNode;
import com.compilador.TablaSimbolos;
import com.compilador.TablaSimbolos.Simbolo;

import java.util.ArrayList;
import java.util.List;

public class Caminante extends CompiladorBaseVisitor<String> {

    //private int asignaciones = 0;
    //private int declaraciones = 0;
    //private int bloques = 0;
    //private int expresiones = 0;

    private TablaSimbolos tabla = new TablaSimbolos();
    private List<String> errores = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();


    public TablaSimbolos getTablaSimbolos() {
        return tabla;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    public void agregarWarning(String warning) {
        warnings.add(warning);
    }

    public List<String> getErrores() {
        return errores;
    }

    public List<String> getWarnings() {
        return warnings;
    }

//    @Override
//    public String visitPrograma(ProgramaContext ctx) {
//        System.out.println("== INICIO DE ANÁLISIS ==");
//        String ret = super.visitPrograma(ctx);
//        System.out.println("\n== REPORTE ==");
//        System.out.println("Asignaciones: " + asignaciones);
//        System.out.println("Declaraciones: " + declaraciones);
//        System.out.println("Bloques: " + bloques);
//        System.out.println("Expresiones: " + expresiones);
//        tabla.imprimir();
//        return ret;
//    }

    @Override
    public String visitPrograma(ProgramaContext ctx) {
        String resultado = super.visitPrograma(ctx);

        // Verificar variables no usadas
        for (TablaSimbolos.Simbolo simbolo : tabla.getSimbolos()) {
            if (simbolo.getCategoria().equals("variable") && !simbolo.isUsada()) {
                agregarWarning("Advertencia: variable '" + simbolo.getNombre() + "' declarada pero no usada. Línea " + simbolo.getLinea());
            }
        }

        return resultado;
    }


    @Override
    public String visitDeclaracionVariable(DeclaracionVariableContext ctx) {
        //declaraciones++;
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        String ambitoActual = tabla.getAmbito();

        Simbolo simbolo = new Simbolo(nombre, tipo, "variable", linea, columna, ambitoActual, new ArrayList<>());
        boolean agregado = tabla.agregar(simbolo);

        if (!agregado) {
            agregarError("Error: variable '" + nombre + "' ya declarada en este ámbito en línea " + linea);
        }

        return super.visitDeclaracionVariable(ctx);
    }

    @Override
    public String visitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.ID().getText();
        int linea = ctx.ID().getSymbol().getLine();
        int columna = ctx.ID().getSymbol().getCharPositionInLine();
        List<String> parametros = obtenerParametros(ctx.parametros());

        String ambitoActual = tabla.getAmbito();
        Simbolo simbolo = new Simbolo(nombre, tipo, "funcion", linea, columna, ambitoActual, parametros);


        if (!tabla.agregar(simbolo)) {
            agregarError("Error: función '" + nombre + "' ya declarada en este ámbito en línea " + linea);
        }

        return super.visitDeclaracionFuncion(ctx);
    }



    private List<String> obtenerParametros(ParametrosContext ctx) {
        List<String> lista = new ArrayList<>();
        if (ctx == null) return lista;
        for (ParametroContext p : ctx.parametro()) {
            String tipo = p.tipo().getText();
            String nombre = p.ID().getText();
            lista.add(tipo + " " + nombre); // O solo nombre, según tu diseño
        }
        return lista;
    }


    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        //asignaciones++;
        String nombre = ctx.ID().getText();
        TablaSimbolos.Simbolo simbolo = tabla.buscar(nombre);

        if (simbolo == null) {
            agregarError("Error: variable '" + nombre + "' no declarada.");
        } else {
            simbolo.setInicializada(true);
            //System.out.println("Asignación: " + nombre + " = " + ctx.expresion().getText());

            // Evaluar la expresión de la asignación
//            EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tabla);
//            evaluador.visit(ctx.expresion());
        }

        return super.visitAsignacion(ctx);
    }

    @Override
    public String visitBloque(BloqueContext ctx) {
        //bloques++;
        return super.visitBloque(ctx);
    }

    @Override
    public String visitExpNegacion(CompiladorParser.ExpNegacionContext ctx) {
        //expresiones++;
        return super.visitExpNegacion(ctx);
    }

    @Override
    public String visitExpDecimal(CompiladorParser.ExpDecimalContext ctx) {
        //expresiones++;
        return super.visitExpDecimal(ctx);
    }

    @Override
    public String visitExpTrue(CompiladorParser.ExpTrueContext ctx) {
        //expresiones++;
        return super.visitExpTrue(ctx);
    }

    @Override
    public String visitExpBinaria(CompiladorParser.ExpBinariaContext ctx) {
        //expresiones++;
        return super.visitExpBinaria(ctx);
    }

    @Override
    public String visitExpParentizada(CompiladorParser.ExpParentizadaContext ctx) {
        //expresiones++;
        return super.visitExpParentizada(ctx);
    }

    @Override
    public String visitExpCaracter(CompiladorParser.ExpCaracterContext ctx) {
        //expresiones++;
        return super.visitExpCaracter(ctx);
    }

    @Override
    public String visitExpEntero(CompiladorParser.ExpEnteroContext ctx) {
        //expresiones++;
        return super.visitExpEntero(ctx);
    }

    @Override
    public String visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        TablaSimbolos.Simbolo simbolo = tabla.buscar(nombre);

        if (simbolo != null) {
            simbolo.setUsada(true);
        } else {
            agregarError("Error: variable '" + nombre + "' no declarada.");
        }

        return super.visitExpVariable(ctx);
    }

    @Override
    public String visitExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        //expresiones++;
        return super.visitExpFuncion(ctx);
    }

    @Override
    public String visitExpFalse(CompiladorParser.ExpFalseContext ctx) {
        //expresiones++;
        return super.visitExpFalse(ctx);
    }

    @Override
    public String visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx) {
        EvaluadorExpresiones evaluador = new EvaluadorExpresiones(tabla);
        Object resultado = evaluador.visit(ctx.expresion());

        if (resultado instanceof Boolean) {
            boolean condicion = (Boolean) resultado;
            if (!condicion) {
                agregarWarning("Condición falsa constante en bucle `while`. Línea " + ctx.getStart().getLine());
            } else {
                agregarWarning("Advertencia: Bucle infinito detectado. Línea " + ctx.getStart().getLine());
            }
            return null;
        }

        return visit(ctx.bloque()); // continuar normalmente
    }


    @Override
    public String visitTerminal(TerminalNode node) {
        return super.visitTerminal(node);
    }

}
