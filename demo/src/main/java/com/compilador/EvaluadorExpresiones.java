package com.compilador;

import org.antlr.v4.runtime.tree.TerminalNode;
import java.util.List;
import java.util.Stack;

public class EvaluadorExpresiones extends CompiladorBaseVisitor<Object> {
    private TablaSimbolos tablaSimbolos;
    private Stack<String> ambitoStack;
    private List<String> errores;
    private List<String> warnings;

    public EvaluadorExpresiones(TablaSimbolos tablaSimbolos, Stack<String> ambitoStack, List<String> errores, List<String> warnings) {
        this.tablaSimbolos = tablaSimbolos;
        this.ambitoStack = ambitoStack;
        this.errores = errores;
        this.warnings = warnings;
    }

    @Override
    public Object visitExpParentizada(CompiladorParser.ExpParentizadaContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public Object visitExpEntero(CompiladorParser.ExpEnteroContext ctx) {
        TerminalNode integerNode = ctx.INTEGER();
        if (integerNode != null) {
            return Integer.parseInt(integerNode.getText());
        }
        errores.add("Error interno: No se encontró token INTEGER en expresión entera en línea " + ctx.getStart().getLine());
        return null;
    }

    @Override
    public Object visitExpDecimal(CompiladorParser.ExpDecimalContext ctx) {
        TerminalNode decimalNode = ctx.DECIMAL();
        if (decimalNode != null) {
            return Double.parseDouble(decimalNode.getText());
        }
        errores.add("Error interno: No se encontró token DECIMAL en expresión decimal en línea " + ctx.getStart().getLine());
        return null;
    }

    @Override
    public Object visitExpCaracter(CompiladorParser.ExpCaracterContext ctx) {
        TerminalNode characterNode = ctx.CHARACTER();
        if (characterNode != null) {
            String text = characterNode.getText();
            return text.substring(1, text.length() - 1).charAt(0);
        }
        errores.add("Error interno: No se encontró token CHARACTER en expresión de caracter en línea " + ctx.getStart().getLine());
        return null;
    }

    @Override
    public Object visitExpTrue(CompiladorParser.ExpTrueContext ctx) {
        return true;
    }

    @Override
    public Object visitExpFalse(CompiladorParser.ExpFalseContext ctx) {
        return false;
    }

    @Override
    public Object visitExpVariable(CompiladorParser.ExpVariableContext ctx) {
        String nombre = ctx.ID().getText();
        TablaSimbolos.Simbolo simbolo = tablaSimbolos.buscar(nombre, ambitoStack);
        if (simbolo != null && simbolo.getValor() != null) {
            return simbolo.getValor();
        }
        // errores.add("Error en línea " + ctx.getStart().getLine() + ": Variable '" + nombre + "' no evaluable como constante.");
        return null;
    }

    @Override
    public Object visitExpNegacion(CompiladorParser.ExpNegacionContext ctx) {
        Object valor = visit(ctx.expresion());
        if (valor instanceof Boolean) {
            return !((Boolean) valor);
        } else if (valor instanceof Integer) {
            return ((Integer) valor) == 0 ? 1 : 0;
        }
        errores.add("Error en línea " + ctx.getStart().getLine() + ": Operador '!' aplicado a tipo no booleano/entero.");
        return null;
    }

    @Override
    public Object visitExpBinaria(CompiladorParser.ExpBinariaContext ctx) {
        Object left = visit(ctx.expresion(0));
        Object right = visit(ctx.expresion(1));

        if (left == null || right == null) {
            return null;
        }

        String op = ctx.operadorBinario().getText();
        int linea = ctx.getStart().getLine();

        try {
            switch (op) {
                case "+":
                    if (left instanceof Integer && right instanceof Integer) return (Integer) left + (Integer) right;
                    if (left instanceof Double || right instanceof Double) return ((Number) left).doubleValue() + ((Number) right).doubleValue();
                    break;
                case "-":
                    if (left instanceof Integer && right instanceof Integer) return (Integer) left - (Integer) right;
                    if (left instanceof Double || right instanceof Double) return ((Number) left).doubleValue() - ((Number) right).doubleValue();
                    break;
                case "*":
                    if (left instanceof Integer && right instanceof Integer) return (Integer) left * (Integer) right;
                    if (left instanceof Double || right instanceof Double) return ((Number) left).doubleValue() * ((Number) right).doubleValue();
                    break;
                case "/":
                    if (left instanceof Integer && right instanceof Integer) {
                        if ((Integer) right == 0) {
                            errores.add("Error en línea " + linea + ": División por cero.");
                            return null;
                        }
                        return (Integer) left / (Integer) right;
                    }
                    if (left instanceof Double || right instanceof Double) {
                        if (((Number) right).doubleValue() == 0.0) {
                            errores.add("Error en línea " + linea + ": División por cero flotante.");
                            return null;
                        }
                        return ((Number) left).doubleValue() / ((Number) right).doubleValue();
                    }
                    break;
                case "%":
                    if (left instanceof Integer && right instanceof Integer) {
                        if ((Integer) right == 0) {
                            errores.add("Error en línea " + linea + ": Módulo por cero.");
                            return null;
                        }
                        return (Integer) left % (Integer) right;
                    }
                    break;
                case "==":
                    return left.equals(right);
                case "!=":
                    return !left.equals(right);
                case ">":
                    if (left instanceof Number && right instanceof Number) return ((Number) left).doubleValue() > ((Number) right).doubleValue();
                    break;
                case "<":
                    if (left instanceof Number && right instanceof Number) return ((Number) left).doubleValue() < ((Number) right).doubleValue();
                    break;
                case ">=":
                    if (left instanceof Number && right instanceof Number) return ((Number) left).doubleValue() >= ((Number) right).doubleValue();
                    break;
                case "<=":
                    if (left instanceof Number && right instanceof Number) return ((Number) left).doubleValue() <= ((Number) right).doubleValue();
                    break;
                case "&&":
                    if (left instanceof Boolean && right instanceof Boolean) return (Boolean) left && (Boolean) right;
                    if (left instanceof Integer && right instanceof Integer) return (((Integer) left) != 0) && (((Integer) right) != 0);
                    break;
                case "||":
                    if (left instanceof Boolean && right instanceof Boolean) return (Boolean) left || (Boolean) right;
                    if (left instanceof Integer && right instanceof Integer) return (((Integer) left) != 0) || (((Integer) right) != 0);
                    break;
            }
        } catch (ClassCastException e) {
            errores.add("Error en línea " + linea + ": Operación binaria inválida entre tipos incompatibles.");
            return null;
        }

        errores.add("Error en línea " + linea + ": Operación binaria no soportada o tipos incompatibles para el operador '" + op + "'.");
        return null;
    }

    @Override
    public Object visitExpFuncion(CompiladorParser.ExpFuncionContext ctx) {
        errores.add("Error en línea " + ctx.getStart().getLine() + ": Llamada a función '" + ctx.ID().getText() + "' no puede ser evaluada como una constante.");
        return null;
    }
}