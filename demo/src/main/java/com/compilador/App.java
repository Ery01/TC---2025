package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class App {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar compilador.jar <archivo.txt>");
            System.exit(1);
        }

        try {
            // 1. ANÁLISIS LÉXICO
            System.out.println("Analizando archivo: " + args[0]);
            CharStream input = CharStreams.fromFileName(args[0]);

            List<String> erroresLexicos = new ArrayList<>();
            CompiladorLexer lexer = new CompiladorLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add("ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                    throw new ParseCancellationException(msg); // Detener el análisis léxico ante el primer error
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            System.out.println("\n=== ANÁLISIS LÉXICO ===");
            if (erroresLexicos.isEmpty()) {
                System.out.printf("%-20s %-30s %-10s %-10s\n", "TIPO", "LEXEMA", "LÍNEA", "COLUMNA");
                System.out.println("-------------------------------------------------------------------");
                for (Token token : tokens.getTokens()) {
                    if (token.getType() != Token.EOF) {
                        String tokenName = CompiladorLexer.VOCABULARY.getSymbolicName(token.getType());
                        System.out.printf("%-20s %-30s %-10d %-10d\n",
                                tokenName, token.getText(), token.getLine(), token.getCharPositionInLine());
                    }
                }
                System.out.println("\n✅ Análisis léxico completado sin errores.");
            } else {
                erroresLexicos.forEach(System.out::println);
                return; // Salir si hay errores léxicos
            }

            // 2. ANÁLISIS SINTÁCTICO
            CompiladorParser parser = new CompiladorParser(tokens);
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add("ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            System.out.println("\n=== ANÁLISIS SINTÁCTICO ===");
            ParseTree tree = parser.programa();
            if (!erroresSintacticos.isEmpty()) {
                erroresSintacticos.forEach(System.out::println);
                return; // Salir si hay errores sintácticos
            } else {
                System.out.println("✅ Análisis sintáctico completado sin errores.");
                System.out.println("Representación textual del árbol sintáctico:");
                System.out.println(tree.toStringTree(parser));
            }

            // 3. VISUALIZACIÓN DEL ÁRBOL SINTÁCTICO (Opcional, puede comentar esta línea si no lo necesita)
            generarImagenArbolSintactico(tree, parser);

            // 4. ANÁLISIS SEMÁNTICO
            // FASE 1: Construcción de Tabla de Símbolos y Verificación de Declaraciones/Tipos (Listener)
            System.out.println("\n=== ANÁLISIS SEMÁNTICO (Fase 1: Listener) ===");
            SimbolosListener listener = new SimbolosListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree); // Recorre el árbol con el Listener

            // Obtener la tabla de símbolos y las listas de errores/advertencias del Listener
            TablaSimbolos tablaSimbolos = listener.getTablaSimbolos();
            List<String> erroresSemanticos = listener.getErrores();
            List<String> advertencias = listener.getWarnings();

            // 5. ANÁLISIS SEMÁNTICO
            // FASE 2: Verificaciones de Flujo de Control y Evaluación de Expresiones (Visitor)
            // Se le pasa la misma tabla de símbolos y listas de errores/advertencias
            System.out.println("\n=== ANÁLISIS SEMÁNTICO (Fase 2: Visitor) ===");
            Caminante visitor = new Caminante(tablaSimbolos, erroresSemanticos, advertencias);
            visitor.visit(tree); // Recorre el árbol con el Visitor para verificaciones adicionales

            // 6. Reportar Errores y Advertencias
            if (!erroresSemanticos.isEmpty()) {
                System.out.println("\n=== ERRORES SEMÁNTICOS ===");
                erroresSemanticos.forEach(System.out::println);
            } else {
                System.out.println("\n✅ Análisis semántico completado sin errores detectados.");
            }

            // 7. Verificación final de variables/funciones no usadas
            // Esto se hace después de que ambos pases (Listener y Visitor) hayan marcado los símbolos.
            System.out.println("\n=== ADVERTENCIAS FINALES ===");
            boolean tieneAdvertenciasFinales = false;
            for (TablaSimbolos.Simbolo simbolo : tablaSimbolos.getTodosSimbolos()) {
                if (simbolo.getCategoria().equals("variable") && !simbolo.isUsada()) {
                    advertencias.add("Advertencia en línea " + simbolo.getLinea() +
                            ": Variable '" + simbolo.getNombre() + "' declarada pero no usada.");
                    tieneAdvertenciasFinales = true;
                }
                if (simbolo.getCategoria().equals("funcion") && !simbolo.isUsada() && !simbolo.getNombre().equals("main")) {
                    advertencias.add("Advertencia en línea " + simbolo.getLinea() +
                            ": Función '" + simbolo.getNombre() + "' declarada pero no llamada.");
                    tieneAdvertenciasFinales = true;
                }
            }

            if (!advertencias.isEmpty()) { // Imprimir todas las advertencias recolectadas
                advertencias.forEach(System.out::println);
            } else {
                System.out.println("✅ No se detectaron advertencias.");
            }

            // 8. Imprimir la Tabla de Símbolos final
            tablaSimbolos.imprimir();

        } catch (IOException e) {
            System.err.println("❌ Error al leer el archivo: " + e.getMessage());
        } catch (ParseCancellationException e) {
            System.err.println("❌ Error de análisis (Léxico/Sintáctico): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado durante la compilación:");
            e.printStackTrace();
        }
    }

    private static void generarImagenArbolSintactico(ParseTree tree, Parser parser) {
        try {
            JFrame frame = new JFrame("Árbol Sintáctico");
            JPanel panel = new JPanel();

            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(1.5); // Zoom

            panel.add(viewer);

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            viewer.open();  // Esto lanza una ventana gráfica con el árbol de análisis

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar árbol sintáctico: " + e.getMessage());
        }
    }
}