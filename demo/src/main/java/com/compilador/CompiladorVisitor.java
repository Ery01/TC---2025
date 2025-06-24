// Generated from com/compilador/Compilador.g4 by ANTLR 4.9.3
package com.compilador;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CompiladorParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CompiladorVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#programa}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrograma(CompiladorParser.ProgramaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentencia}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentencia(CompiladorParser.SentenciaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaAnidadas}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaAnidadas(CompiladorParser.SentenciaAnidadasContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaIf}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaIf(CompiladorParser.SentenciaIfContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaWhile}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaFor(CompiladorParser.SentenciaForContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#forInitPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInitPart(CompiladorParser.ForInitPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#forUpdatePart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdatePart(CompiladorParser.ForUpdatePartContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#declaracionVariableInternaFor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracionVariableInternaFor(CompiladorParser.DeclaracionVariableInternaForContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#expresionNoPuntoComa}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpresionNoPuntoComa(CompiladorParser.ExpresionNoPuntoComaContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaBreak}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaBreak(CompiladorParser.SentenciaBreakContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaContinue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaContinue(CompiladorParser.SentenciaContinueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#sentenciaLlamadaFuncion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSentenciaLlamadaFuncion(CompiladorParser.SentenciaLlamadaFuncionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#bloque}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBloque(CompiladorParser.BloqueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#declaracionFuncion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#parametros}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametros(CompiladorParser.ParametrosContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#parametro}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParametro(CompiladorParser.ParametroContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#declaracionVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#asignacion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsignacion(CompiladorParser.AsignacionContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#asignacionNoPyC}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAsignacionNoPyC(CompiladorParser.AsignacionNoPyCContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#retorno}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRetorno(CompiladorParser.RetornoContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#tipo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTipo(CompiladorParser.TipoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expNegacion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpNegacion(CompiladorParser.ExpNegacionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expDecimal}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpDecimal(CompiladorParser.ExpDecimalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expTrue}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpTrue(CompiladorParser.ExpTrueContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expBinaria}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpBinaria(CompiladorParser.ExpBinariaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expParentizada}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpParentizada(CompiladorParser.ExpParentizadaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expCaracter}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpCaracter(CompiladorParser.ExpCaracterContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expEntero}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpEntero(CompiladorParser.ExpEnteroContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expVariable}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpVariable(CompiladorParser.ExpVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expCadena}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpCadena(CompiladorParser.ExpCadenaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expFuncion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpFuncion(CompiladorParser.ExpFuncionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expFalse}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpFalse(CompiladorParser.ExpFalseContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#operadorBinario}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperadorBinario(CompiladorParser.OperadorBinarioContext ctx);
	/**
	 * Visit a parse tree produced by {@link CompiladorParser#argumentos}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentos(CompiladorParser.ArgumentosContext ctx);
}