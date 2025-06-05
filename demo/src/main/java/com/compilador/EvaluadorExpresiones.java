package com.compilador;

import com.compilador.CompiladorParser.*;

public class EvaluadorExpresiones extends CompiladorBaseVisitor<Object> {

    private int profundidad = 0;
    private static final int MAX_PROFUNDIDAD = 100;

    private TablaSimbolos tabla;

    public EvaluadorExpresiones(TablaSimbolos tabla) {
        this.tabla = tabla;
    }

    @Override
    public Object visitExpEntero(ExpEnteroContext ctx) {
        return Integer.parseInt(ctx.INTEGER().getText());
    }

    @Override
    public Object visitExpDecimal(ExpDecimalContext ctx) {
        return Double.parseDouble(ctx.DECIMAL().getText());
    }

    @Override
    public Object visitExpTrue(ExpTrueContext ctx) {
        return true;
    }

    @Override
    public Object visitExpFalse(ExpFalseContext ctx) {
        return false;
    }

    @Override
    public Object visitExpCaracter(ExpCaracterContext ctx) {
        return ctx.getText().charAt(1); // suponer comilla simple: 'a'
    }

    @Override
    public Object visitExpVariable(ExpVariableContext ctx) {
        String id = ctx.ID().getText();
        if (!tabla.existe(id)) {
            System.err.println("Error: variable '" + id + "' no declarada.");
            return null;
        }

        TablaSimbolos.Simbolo simbolo = tabla.buscar(id);

        if (!simbolo.isInicializada()) {
            System.err.println("Advertencia: variable '" + id + "' utilizada sin estar inicializada.");
        }
        return tabla.obtenerValor(id); // solo si tenés soporte de valores
    }

    @Override
    public Object visitExpNegacion(ExpNegacionContext ctx) {
        Object valor = visit(ctx.expresion());
        if (valor instanceof Boolean) {
            return !(Boolean) valor;
        }
        System.err.println("Error semántico: ¡Negación de valor no booleano!");
        return null;
    }

    @Override
    public Object visitExpParentizada(ExpParentizadaContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public Object visitExpBinaria(ExpBinariaContext ctx) {
        profundidad++;
        if (profundidad > MAX_PROFUNDIDAD) {
            System.err.println("Advertencia: expresión demasiado profunda (posible bucle infinito)");
            return null;
        }

        Object izq = visit(ctx.expresion(0));
        Object der = visit(ctx.expresion(1));
        String op = ctx.operadorBinario().getText();

        Object resultado = null;

        if (izq instanceof Integer && der instanceof Integer) {
            int a = (Integer) izq;
            int b = (Integer) der;
            switch (op) {
                case "+": resultado = a + b; break;
                case "-": resultado = a - b; break;
                case "*": resultado = a * b; break;
                case "/": resultado = b != 0 ? a / b : errorDiv(); break;
                case "%": resultado = b != 0 ? a % b : errorDiv(); break;
                case "==": resultado = a == b; break;
                case "!=": resultado = a != b; break;
                case "<": resultado = a < b; break;
                case "<=": resultado = a <= b; break;
                case ">": resultado = a > b; break;
                case ">=": resultado = a >= b; break;
                default: resultado = errorOp(op);
            }
        } else if (izq instanceof Boolean && der instanceof Boolean) {
            boolean a = (Boolean) izq;
            boolean b = (Boolean) der;
            switch (op) {
                case "&&": resultado = a && b; break;
                case "||": resultado = a || b; break;
                case "==": resultado = a == b; break;
                case "!=": resultado = a != b; break;
                default: resultado = errorOp(op);
            }
        } else {
            System.err.println("Error: Tipos incompatibles para el operador '" + op + "'");
        }

        profundidad--;
        return resultado;
    }

    private Object errorDiv() {
        System.err.println("Error: división por cero");
        return null;
    }

    private Object errorOp(String op) {
        System.err.println("Operador no soportado: " + op);
        return null;
    }

}
