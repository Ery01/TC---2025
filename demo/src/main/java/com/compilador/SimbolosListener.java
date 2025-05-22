package com.compilador;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener mejorado para construir la tabla de símbolos y realizar verificación de tipos
 */
public class SimbolosListener extends CompiladorBaseListener {

    private TablaSimbolos tablaSimbolos;
    private List<String> errores;
    private String tipoRetornoActual; // Para verificar return

    public SimbolosListener() {
        this.tablaSimbolos = new TablaSimbolos();
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
            errores.add("Error semántico en línea " + linea +
                    ": Variable '" + nombre + "' no declarada");
            return;
        }

        // Verificación de categoría (solo variables pueden ser asignadas, no funciones)
        if (!simbolo.getCategoria().equals("variable") && !simbolo.getCategoria().equals("parametro")) {
            errores.add("Error semántico en línea " + linea +
                    ": No se puede asignar valor a '" + nombre + "' porque no es una variable");
            return;
        }

        // La verificación de tipos de la expresión se hará cuando implementemos type checking
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
            return simbolo != null ? simbolo.getTipo() : "desconocido";
        }

        // Para otros tipos de expresiones, necesitarías reglas más complejas
        return "desconocido";
    }
}