package com.compilador;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.TreeViewer;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class App {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -jar compilador.jar <archivo.txt>");
            System.exit(1);
        }

        try {
            // Obtener el nombre del archivo de entrada para generar nombres de salida
            String inputFilePath = args[0];
            String inputFileName = new File(inputFilePath).getName();
            String baseName = inputFileName.substring(0, inputFileName.lastIndexOf('.'));

            // Verificar que el archivo existe
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                System.err.println("❌ Error: El archivo '" + inputFilePath + "' no existe.");
                System.exit(1);
            }

            System.out.println("🚀 Iniciando compilación de: " + inputFilePath);
            System.out.println("=".repeat(60));

            // 1. ANÁLISIS LÉXICO
            System.out.println("\n=== ANÁLISIS LÉXICO ===");
            CharStream input = CharStreams.fromFileName(inputFilePath);

            List<String> erroresLexicos = new ArrayList<>();
            CompiladorLexer lexer = new CompiladorLexer(input); // Usando tu CompiladorLexer
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add("ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                    throw new ParseCancellationException(msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

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
                return;
            }

            // 2. ANÁLISIS SINTÁCTICO
            System.out.println("\n=== ANÁLISIS SINTÁCTICO ===");
            CompiladorParser parser = new CompiladorParser(tokens); // Usando tu CompiladorParser
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add("ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg);
                }
            });

            ParseTree tree = parser.programa();
            if (!erroresSintacticos.isEmpty()) {
                erroresSintacticos.forEach(System.out::println);
                return;
            } else {
                System.out.println("✅ Análisis sintáctico completado sin errores.");
                System.out.println("Representación textual del árbol sintáctico:");
                System.out.println(tree.toStringTree(parser));
            }

            // 3. VISUALIZACIÓN DEL ÁRBOL SINTÁCTICO (Opcional, puede comentar esta línea si no lo necesita)
            // generarImagenArbolSintactico(tree, parser);
            // System.out.println("   📊 Ventana del árbol sintáctico abierta");

            // Listas para errores y advertencias semánticas y de código intermedio
            List<String> erroresSemanticos = new ArrayList<>();
            List<String> warningsGenerales = new ArrayList<>();

            // 4. ANÁLISIS SEMÁNTICO (Fase 1: Listener)
            System.out.println("\n=== ANÁLISIS SEMÁNTICO (Fase 1: Listener) ===");
            SimbolosListener listener = new SimbolosListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);

            TablaSimbolos tabla = listener.getTablaSimbolos();
            erroresSemanticos.addAll(listener.getErrores());
            warningsGenerales.addAll(listener.getWarnings());

            // Mostrar tabla de símbolos
            System.out.println("\n=== TABLA DE SÍMBOLOS ===");
            tabla.imprimir();

            // 5. ANÁLISIS SEMÁNTICO (Fase 2: Visitor - Caminante)
            System.out.println("\n=== ANÁLISIS SEMÁNTICO (Fase 2: Visitor - Caminante) ===");
            Caminante caminanteVisitor = new Caminante(tabla, erroresSemanticos, warningsGenerales);
            caminanteVisitor.visit(tree);

            // 6. Reportar Errores y Advertencias Semánticas
            if (!erroresSemanticos.isEmpty()) {
                System.out.println("\n❌ ERRORES SEMÁNTICOS:");
                erroresSemanticos.forEach(System.out::println);
                return;
            } else {
                System.out.println("\n✅ Análisis semántico completado sin errores.");
            }

            // 7. Verificación final de variables/funciones no usadas (después de ambos pases semánticos)
            System.out.println("\n=== ADVERTENCIAS FINALES (Uso de Símbolos) ===");
            for (TablaSimbolos.Simbolo simbolo : tabla.getTodosSimbolos()) {
                if (simbolo.getCategoria().equals("variable") && !simbolo.isUsada()) {
                    warningsGenerales.add("Advertencia en línea " + simbolo.getLinea() +
                            ": Variable '" + simbolo.getNombre() + "' declarada pero no usada.");
                }
                if (simbolo.getCategoria().equals("funcion") && !simbolo.isUsada() && !simbolo.getNombre().equals("main")) {
                    warningsGenerales.add("Advertencia en línea " + simbolo.getLinea() +
                            ": Función '" + simbolo.getNombre() + "' declarada pero no llamada.");
                }
            }

            if (!warningsGenerales.isEmpty()) {
                System.out.println("\n⚠️ ADVERTENCIAS DETECTADAS:");
                warningsGenerales.forEach(System.out::println);
            } else {
                System.out.println("\n✅ No se detectaron advertencias.");
            }

            // 8. GENERACIÓN DE CÓDIGO INTERMEDIO (TAC sin optimizar)
            System.out.println("\n=== GENERACIÓN DE CÓDIGO INTERMEDIO (Original) ===");
            CodigoVisitor visitor = new CodigoVisitor(tabla, erroresSemanticos, warningsGenerales);
            visitor.visit(tree);

            GeneradorCodigo generador = visitor.getGenerador();

            if (!erroresSemanticos.isEmpty()) {
                System.out.println("\n❌ ERRORES DURANTE LA GENERACIÓN DE CÓDIGO INTERMEDIO:");
                erroresSemanticos.forEach(System.out::println);
                return;
            } else {
                System.out.println("✅ Generación de Código Intermedio completada sin errores.");
            }

            // Mostrar y guardar el código intermedio ORIGINAL
            System.out.println("\n📝 === CÓDIGO DE TRES DIRECCIONES (Original) ===");
            generador.imprimirCodigo();
            generador.imprimirEstadisticas();

            String codigoIntermedioOriginalPath = baseName + "_codigo_intermedio_original.txt";
            guardarCodigoEnArchivo(generador.getCodigo(), codigoIntermedioOriginalPath);
            System.out.println("\n💾 Código intermedio original guardado en: " + codigoIntermedioOriginalPath);

            // 9. OPTIMIZACIÓN DE CÓDIGO INTERMEDIO
            System.out.println("\n=== FASE DE OPTIMIZACIÓN DE CÓDIGO INTERMEDIO ===");
            OptimizadorCodigo optimizador = new OptimizadorCodigo();
            List<String> tacOriginal = new ArrayList<>(generador.getCodigo()); // Obtener una copia del código original
            List<String> tacOptimizado = optimizador.optimizar(tacOriginal);

            // Mostrar y guardar el código intermedio OPTIMIZADO
            System.out.println("\n📝 === CÓDIGO DE TRES DIRECCIONES (Optimizado) ===");
            for (int i = 0; i < tacOptimizado.size(); i++) {
                System.out.printf("%3d: %s\n", i, tacOptimizado.get(i));
            }
            System.out.println("Total instrucciones optimizadas: " + tacOptimizado.size());

            String codigoIntermedioOptimizadoPath = baseName + "_codigo_intermedio_optimizado.txt";
            guardarCodigoEnArchivo(tacOptimizado, codigoIntermedioOptimizadoPath);
            System.out.println("\n💾 Código intermedio optimizado guardado en: " + codigoIntermedioOptimizadoPath);

            // 10. RESUMEN FINAL
            System.out.println("\n=== RESUMEN DE COMPILACIÓN ===");
            System.out.println("Archivo procesado: " + inputFilePath);
            System.out.println("Tokens analizados: " + (tokens.size() - 1));
            System.out.println("Símbolos en tabla: " + tabla.getTodosSimbolos().size());
            System.out.println("Instrucciones originales: " + generador.getCodigo().size());
            System.out.println("Instrucciones optimizadas: " + tacOptimizado.size());
            System.out.println("Reducción de instrucciones: " + (generador.getCodigo().size() - tacOptimizado.size()));
            System.out.println("Archivo de salida (original): " + codigoIntermedioOriginalPath);
            System.out.println("Archivo de salida (optimizado): " + codigoIntermedioOptimizadoPath);

            if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty() && erroresSemanticos.isEmpty()) {
                System.out.println("\n🎉 ¡COMPILACIÓN EXITOSA! 🎉");
            } else {
                System.out.println("\n❌ COMPILACIÓN FALLIDA debido a errores.");
            }

        } catch (IOException e) {
            System.err.println("❌ Error al leer o escribir archivos: " + e.getMessage());
        } catch (ParseCancellationException e) {
            System.err.println("❌ Error de análisis (Léxico/Sintáctico): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado:");
            e.printStackTrace();
        }
    }

    /**
     * Guarda una lista de líneas de código en un archivo de texto
     */
    private static void guardarCodigoEnArchivo(List<String> codigo, String rutaArchivo) throws IOException {
        Path filePath = Paths.get(rutaArchivo);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("// Código de tres direcciones generado automáticamente");
            writer.newLine();
            writer.write("// Archivo: " + rutaArchivo);
            writer.newLine();
            writer.write("// Total de instrucciones: " + codigo.size());
            writer.newLine();
            writer.newLine();

            for (int i = 0; i < codigo.size(); i++) {
                writer.write(String.format("%3d: %s", i, codigo.get(i)));
                writer.newLine();
            }
        }
    }

    /**
     * Genera y muestra el árbol sintáctico visualmente (Opcional, se puede comentar)
     */
    private static void generarImagenArbolSintactico(ParseTree tree, Parser parser) {
        try {
            JFrame frame = new JFrame("Árbol Sintáctico - Compilador");
            JPanel panel = new JPanel();

            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), tree);
            viewer.setScale(1.5); // Zoom

            panel.add(viewer);

            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            frame.add(scrollPane);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null); // Centrar ventana

            // frame.setVisible(true);

            viewer.open();

        } catch (Exception e) {
            System.err.println("❌ Error al mostrar árbol sintáctico: " + e.getMessage());
            System.err.println("   ⚠️ La visualización del AST falló, pero la compilación continúa...");
        }
    }
}
