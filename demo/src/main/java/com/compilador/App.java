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

    // --- Constantes de Color ANSI ---
    public static final String RESET = "\u001B[0m";

    // Colores de Texto
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Estilos
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(RED + "Uso: java -jar compilador.jar <archivo.txt>" + RESET);
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
                System.err.println(RED + "❌ Error: El archivo '" + inputFilePath + "' no existe." + RESET);
                System.exit(1);
            }

            System.out.println(" ");
            System.out.println(" ");

            System.out.println(BLUE + "----------------------------------------------------" + RESET);
            System.out.println(BLUE + "| Final de Técnicas de Compilación - Jonathan Mora |" + RESET);
            System.out.println(BLUE + "-------------------------\uD83C\uDF93-------------------------" + RESET);

            System.out.println(" ");
            System.out.println(" ");


            System.out.println(BLUE + "🚀 Iniciando compilación de: " + inputFilePath + RESET);
            System.out.println(BLUE + "=".repeat(60) + RESET);

            // 1. ANÁLISIS LÉXICO
            System.out.println(CYAN + BOLD + "\n=== ANÁLISIS LÉXICO ===" + RESET);
            CharStream input = CharStreams.fromFileName(inputFilePath);

            List<String> erroresLexicos = new ArrayList<>();
            CompiladorLexer lexer = new CompiladorLexer(input); // Usando tu CompiladorLexer
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresLexicos.add(RED + "ERROR LÉXICO en línea " + line + ":" + charPositionInLine + " - " + msg + RESET);
                    throw new ParseCancellationException(msg);
                }
            });

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();

            if (erroresLexicos.isEmpty()) {
                System.out.printf(BOLD + CYAN + "%-20s %-30s %-10s %-10s\n" + RESET, "TIPO", "LEXEMA", "LÍNEA", "COLUMNA");
                System.out.println(BOLD + CYAN + "-------------------------------------------------------------------" + RESET);
                for (Token token : tokens.getTokens()) {
                    if (token.getType() != Token.EOF) {
                        String tokenType = CompiladorLexer.VOCABULARY.getSymbolicName(token.getType());
                        String tokenColor = WHITE; // Default color for tokens

                        // Apply colors based on token type
                        switch (tokenType) {
                            case "INT":
                            case "CHAR":
                            case "DOUBLE":
                            case "VOID":
                            case "BOOL":
                                tokenColor = GREEN; // Type keywords
                                break;
                            case "IF":
                            case "ELSE":
                            case "WHILE":
                            case "FOR":
                            case "RETURN":
                            case "BREAK":
                            case "CONTINUE":
                                tokenColor = BLUE; // Control flow keywords
                                break;
                            case "ID":
                                tokenColor = YELLOW; // Identifiers
                                break;
                            case "INTEGER":
                            case "DECIMAL":
                            case "CHARACTER":
                            case "STRING_LITERAL":
                            case "TRUE":
                            case "FALSE":
                                tokenColor = MAGENTA; // Literals
                                break;
                            case "SUM":
                            case "RES":
                            case "MUL":
                            case "DIV":
                            case "MOD":
                            case "MAYOR":
                            case "MAYOR_IGUAL":
                            case "MENOR":
                            case "MENOR_IGUAL":
                            case "EQL":
                            case "DISTINTO":
                            case "AND":
                            case "OR":
                            case "NOT":
                            case "IGUAL":
                                tokenColor = RED; // Operators
                                break;
                            case "PA":
                            case "PC":
                            case "LA":
                            case "LC":
                            case "PYC":
                            case "COMA":
                                tokenColor = CYAN; // Punctuation
                                break;
                            default:
                                tokenColor = WHITE; // Other tokens
                                break;
                        }

                        System.out.printf(tokenColor + "%-20s %-30s %-10d %-10d\n" + RESET,
                                tokenType, token.getText(), token.getLine(), token.getCharPositionInLine());
                    }
                }
                System.out.println(GREEN + "\n✅ Análisis léxico completado sin errores." + RESET);
            } else {
                erroresLexicos.forEach(System.out::println);
                return;
            }

            // 2. ANÁLISIS SINTÁCTICO
            System.out.println(CYAN + BOLD + "\n=== ANÁLISIS SINTÁCTICO ===" + RESET);
            CompiladorParser parser = new CompiladorParser(tokens); // Usando tu CompiladorParser
            List<String> erroresSintacticos = new ArrayList<>();
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    erroresSintacticos.add(RED + "ERROR SINTÁCTICO en línea " + line + ":" + charPositionInLine + " - " + msg + RESET);
                }
            });

            ParseTree tree = parser.programa();
            if (!erroresSintacticos.isEmpty()) {
                erroresSintacticos.forEach(System.out::println); // Ya tienen color aplicado
                return;
            } else {
                System.out.println(GREEN + "✅ Análisis sintáctico completado sin errores." + RESET);
                System.out.println("Representación textual del árbol sintáctico:");
                System.out.println(tree.toStringTree(parser));
            }

            // 3. VISUALIZACIÓN DEL ÁRBOL SINTÁCTICO (Opcional, puede comentar esta línea si no lo necesita)
            // generarImagenArbolSintactico(tree, parser);
            System.out.println(BLUE + "   📊 Ventana del árbol sintáctico abierta" + RESET);

            // Listas para errores y advertencias semánticas y de código intermedio
            List<String> erroresSemanticos = new ArrayList<>();
            List<String> warningsGenerales = new ArrayList<>();

            // 4. ANÁLISIS SEMÁNTICO (Fase 1: Listener)
            System.out.println(CYAN + BOLD + "\n=== ANÁLISIS SEMÁNTICO (Fase 1: Listener) ===" + RESET);
            SimbolosListener listener = new SimbolosListener();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(listener, tree);

            TablaSimbolos tabla = listener.getTablaSimbolos();
            erroresSemanticos.addAll(listener.getErrores());
            warningsGenerales.addAll(listener.getWarnings());

            // Mostrar tabla de símbolos
            System.out.println(CYAN + BOLD + "\n=== TABLA DE SÍMBOLOS ===" + RESET);
            tabla.imprimir(); // Asumiendo que tabla.imprimir() no usa colores internos

            // 5. ANÁLISIS SEMÁNTICO (Fase 2: Visitor - Caminante)
            System.out.println(CYAN + BOLD + "\n=== ANÁLISIS SEMÁNTICO (Fase 2: Visitor - Caminante) ===" + RESET);
            Caminante caminanteVisitor = new Caminante(tabla, erroresSemanticos, warningsGenerales);
            caminanteVisitor.visit(tree);

            // 6. Reportar Errores y Advertencias Semánticas
            if (!erroresSemanticos.isEmpty()) {
                System.out.println(RED + BOLD + "\n❌ ERRORES SEMÁNTICOS:" + RESET);
                erroresSemanticos.forEach(System.out::println); // Ya tienen color aplicado
                return;
            } else {
                System.out.println(GREEN + "\n✅ Análisis semántico completado sin errores." + RESET);
            }

            // 7. Verificación final de variables/funciones no usadas (después de ambos pases semánticos)
            System.out.println(YELLOW + BOLD + "\n=== ADVERTENCIAS FINALES (Uso de Símbolos) ===" + RESET);
            for (TablaSimbolos.Simbolo simbolo : tabla.getTodosSimbolos()) {
                if (simbolo.getCategoria().equals("variable") && !simbolo.isUsada()) {
                    warningsGenerales.add(YELLOW + "Advertencia en línea " + simbolo.getLinea() +
                            ": Variable '" + simbolo.getNombre() + "' declarada pero no usada." + RESET);
                }
                if (simbolo.getCategoria().equals("funcion") && !simbolo.isUsada() && !simbolo.getNombre().equals("main")) {
                    warningsGenerales.add(YELLOW + "Advertencia en línea " + simbolo.getLinea() +
                            ": Función '" + simbolo.getNombre() + "' declarada pero no llamada." + RESET);
                }
            }

            if (!warningsGenerales.isEmpty()) {
                System.out.println(YELLOW + BOLD + "\n⚠️ ADVERTENCIAS DETECTADAS:" + RESET);
                warningsGenerales.forEach(System.out::println); // Ya tienen color aplicado
            } else {
                System.out.println(GREEN + "\n✅ No se detectaron advertencias." + RESET);
            }

            // 8. GENERACIÓN DE CÓDIGO INTERMEDIO (TAC sin optimizar)
            System.out.println(CYAN + BOLD + "\n=== GENERACIÓN DE CÓDIGO INTERMEDIO (Original) ===" + RESET);
            CodigoVisitor visitor = new CodigoVisitor(tabla, erroresSemanticos, warningsGenerales);
            visitor.visit(tree);

            GeneradorCodigo generador = visitor.getGenerador();

            if (!erroresSemanticos.isEmpty()) {
                System.out.println(RED + BOLD + "\n❌ ERRORES DURANTE LA GENERACIÓN DE CÓDIGO INTERMEDIO:" + RESET);
                erroresSemanticos.forEach(System.out::println); // Ya tienen color aplicado
                return;
            } else {
                System.out.println(GREEN + "✅ Generación de Código Intermedio completada sin errores." + RESET);
            }

            // Mostrar y guardar el código intermedio ORIGINAL
            System.out.println(MAGENTA + BOLD + "\n📝 === CÓDIGO DE TRES DIRECCIONES (Original) ===" + RESET);
            // La impresion del codigo y estadisticas ya pueden ser personalizadas internamente por GeneradorCodigo
            generador.imprimirCodigo();
            generador.imprimirEstadisticas();

            String codigoIntermedioOriginalPath = baseName + "_codigo_intermedio_original.txt";
            guardarCodigoEnArchivo(generador.getCodigo(), codigoIntermedioOriginalPath);
            System.out.println(BLUE + "\n💾 Código intermedio original guardado en: " + codigoIntermedioOriginalPath + RESET);

            // 9. OPTIMIZACIÓN DE CÓDIGO INTERMEDIO
            System.out.println(CYAN + BOLD + "\n=== FASE DE OPTIMIZACIÓN DE CÓDIGO INTERMEDIO ===" + RESET);
            OptimizadorCodigo optimizador = new OptimizadorCodigo();
            List<String> tacOriginal = new ArrayList<>(generador.getCodigo()); // Obtener una copia del codigo original
            List<String> tacOptimizado = optimizador.optimizar(tacOriginal);

            System.out.println(MAGENTA + BOLD + "\n📝 === CÓDIGO DE TRES DIRECCIONES (Optimizado) ===" + RESET);
            for (int i = 0; i < tacOptimizado.size(); i++) {
                String instruccion = tacOptimizado.get(i);
                String color = RESET; // Default color

                if (instruccion.contains("func_") && instruccion.endsWith(":")) {
                    color = CYAN + BOLD; // Function entry points
                } else if (instruccion.startsWith("end_func_")) {
                    color = CYAN + BOLD; // Function end points
                } else if (instruccion.contains(" = ")) {
                    color = WHITE; // Assignments
                } else if (instruccion.startsWith("if !") || instruccion.startsWith("goto ")) { // Ajustado para 'if !'
                    color = BLUE; // Control flow
                } else if (instruccion.endsWith(":")) {
                    color = MAGENTA; // Labels
                } else if (instruccion.startsWith("return ")) {
                    color = GREEN; // Return statements
                } else if (instruccion.startsWith("push ")) {
                    color = YELLOW; // Push parameters
                } else if (instruccion.contains(" = call ") || instruccion.contains(" = pop ")) { // Agregado pop
                    color = GREEN; // Function calls with return / Pop
                } else if (instruccion.contains("call ")) {
                    color = GREEN; // Function calls without return
                } else if (instruccion.contains(" = !")) { // Para operaciones unarias como !
                    color = WHITE;
                }

                System.out.printf(color + "%3d: %s\n" + RESET, i, instruccion);
            }
            System.out.println(GREEN + "Total instrucciones optimizadas: " + RESET + tacOptimizado.size());

            String codigoIntermedioOptimizadoPath = baseName + "_codigo_intermedio_optimizado.txt";
            guardarCodigoEnArchivo(tacOptimizado, codigoIntermedioOptimizadoPath);
            System.out.println(BLUE + "\n💾 Código intermedio optimizado guardado en: " + codigoIntermedioOptimizadoPath + RESET);

            // 10. RESUMEN FINAL
            System.out.println(CYAN + BOLD + "\n=== RESUMEN DE COMPILACIÓN ===" + RESET);
            System.out.println(WHITE + "\uD83D\uDCC1 Archivo procesado: " + RESET + inputFilePath);
            System.out.println(WHITE + "\uD83D\uDD24 Tokens analizados: " + RESET + (tokens.size() - 1));
            System.out.println(WHITE + "\uD83D\uDCCA Símbolos en tabla: " + RESET + tabla.getTodosSimbolos().size());
            System.out.println(WHITE + "\uD83D\uDCDD Instrucciones originales: " + RESET + generador.getCodigo().size());
            System.out.println(WHITE + "\uD83D\uDCDD Instrucciones optimizadas: " + RESET + tacOptimizado.size());
            System.out.println(WHITE + "\uD83D\uDCC8 Reducción de instrucciones: " + RESET + (generador.getCodigo().size() - tacOptimizado.size()));
            System.out.println(WHITE + "\uD83D\uDCC4 Archivo de salida (original): " + RESET + codigoIntermedioOriginalPath);
            System.out.println(WHITE + "\uD83D\uDCC4 Archivo de salida (optimizado): " + RESET + codigoIntermedioOptimizadoPath);

            if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty() && erroresSemanticos.isEmpty()) {
                System.out.println(GREEN + BOLD + "\n🎉 ¡COMPILACIÓN EXITOSA! 🎉" + RESET);
            } else {
                System.out.println(RED + BOLD + "\n❌ COMPILACIÓN FALLIDA debido a errores." + RESET);
            }

        } catch (IOException e) {
            System.err.println(RED + "❌ Error al leer o escribir archivos: " + e.getMessage() + RESET);
        } catch (ParseCancellationException e) {
            System.err.println(RED + "❌ Error de análisis (Léxico/Sintáctico): " + e.getMessage() + RESET);
        } catch (Exception e) {
            System.err.println(RED + "❌ Error inesperado:" + RESET);
            e.printStackTrace();
        }
    }

    /**
     * Guarda una lista de líneas de código en un archivo de texto
     */
    private static void guardarCodigoEnArchivo(List<String> codigo, String rutaArchivo) throws IOException {
        Path filePath = Paths.get(rutaArchivo);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("// Codigo de tres direcciones generado automaticamente");
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
     * Genera y muestra el arbol sintactico visualmente (Opcional, se puede comentar)
     */
    private static void generarImagenArbolSintactico(ParseTree tree, Parser parser) {
        try {
            JFrame frame = new JFrame("Arbol Sintactico - Compilador");
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

            // frame.setVisible(true); // Comentado para evitar que se abra por defecto

            viewer.open();

        } catch (Exception e) {
            System.err.println(RED + "❌ Error al mostrar arbol sintactico: " + e.getMessage() + RESET);
            System.err.println(YELLOW + "   ⚠️ La visualizacion del AST fallo, pero la compilacion continua..." + RESET);
        }
    }
}