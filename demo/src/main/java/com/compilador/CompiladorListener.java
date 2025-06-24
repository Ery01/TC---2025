// Generated from com/compilador/Compilador.g4 by ANTLR 4.9.3
package com.compilador;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CompiladorParser}.
 */
public interface CompiladorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#programa}.
	 * @param ctx the parse tree
	 */
	void enterPrograma(CompiladorParser.ProgramaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#programa}.
	 * @param ctx the parse tree
	 */
	void exitPrograma(CompiladorParser.ProgramaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentencia}.
	 * @param ctx the parse tree
	 */
	void enterSentencia(CompiladorParser.SentenciaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentencia}.
	 * @param ctx the parse tree
	 */
	void exitSentencia(CompiladorParser.SentenciaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaAnidadas}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaAnidadas(CompiladorParser.SentenciaAnidadasContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaAnidadas}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaAnidadas(CompiladorParser.SentenciaAnidadasContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaIf}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaIf(CompiladorParser.SentenciaIfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaIf}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaIf(CompiladorParser.SentenciaIfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaWhile}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaWhile}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaWhile(CompiladorParser.SentenciaWhileContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaFor}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaFor(CompiladorParser.SentenciaForContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaFor}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaFor(CompiladorParser.SentenciaForContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#forInitPart}.
	 * @param ctx the parse tree
	 */
	void enterForInitPart(CompiladorParser.ForInitPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#forInitPart}.
	 * @param ctx the parse tree
	 */
	void exitForInitPart(CompiladorParser.ForInitPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#forUpdatePart}.
	 * @param ctx the parse tree
	 */
	void enterForUpdatePart(CompiladorParser.ForUpdatePartContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#forUpdatePart}.
	 * @param ctx the parse tree
	 */
	void exitForUpdatePart(CompiladorParser.ForUpdatePartContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#declaracionVariableInternaFor}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracionVariableInternaFor(CompiladorParser.DeclaracionVariableInternaForContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#declaracionVariableInternaFor}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracionVariableInternaFor(CompiladorParser.DeclaracionVariableInternaForContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#expresionNoPuntoComa}.
	 * @param ctx the parse tree
	 */
	void enterExpresionNoPuntoComa(CompiladorParser.ExpresionNoPuntoComaContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#expresionNoPuntoComa}.
	 * @param ctx the parse tree
	 */
	void exitExpresionNoPuntoComa(CompiladorParser.ExpresionNoPuntoComaContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaBreak}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaBreak(CompiladorParser.SentenciaBreakContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaBreak}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaBreak(CompiladorParser.SentenciaBreakContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaContinue}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaContinue(CompiladorParser.SentenciaContinueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaContinue}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaContinue(CompiladorParser.SentenciaContinueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#sentenciaLlamadaFuncion}.
	 * @param ctx the parse tree
	 */
	void enterSentenciaLlamadaFuncion(CompiladorParser.SentenciaLlamadaFuncionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#sentenciaLlamadaFuncion}.
	 * @param ctx the parse tree
	 */
	void exitSentenciaLlamadaFuncion(CompiladorParser.SentenciaLlamadaFuncionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#bloque}.
	 * @param ctx the parse tree
	 */
	void enterBloque(CompiladorParser.BloqueContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#bloque}.
	 * @param ctx the parse tree
	 */
	void exitBloque(CompiladorParser.BloqueContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#declaracionFuncion}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#declaracionFuncion}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracionFuncion(CompiladorParser.DeclaracionFuncionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#parametros}.
	 * @param ctx the parse tree
	 */
	void enterParametros(CompiladorParser.ParametrosContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#parametros}.
	 * @param ctx the parse tree
	 */
	void exitParametros(CompiladorParser.ParametrosContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#parametro}.
	 * @param ctx the parse tree
	 */
	void enterParametro(CompiladorParser.ParametroContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#parametro}.
	 * @param ctx the parse tree
	 */
	void exitParametro(CompiladorParser.ParametroContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#declaracionVariable}.
	 * @param ctx the parse tree
	 */
	void enterDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#declaracionVariable}.
	 * @param ctx the parse tree
	 */
	void exitDeclaracionVariable(CompiladorParser.DeclaracionVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#asignacion}.
	 * @param ctx the parse tree
	 */
	void enterAsignacion(CompiladorParser.AsignacionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#asignacion}.
	 * @param ctx the parse tree
	 */
	void exitAsignacion(CompiladorParser.AsignacionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#asignacionNoPyC}.
	 * @param ctx the parse tree
	 */
	void enterAsignacionNoPyC(CompiladorParser.AsignacionNoPyCContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#asignacionNoPyC}.
	 * @param ctx the parse tree
	 */
	void exitAsignacionNoPyC(CompiladorParser.AsignacionNoPyCContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#retorno}.
	 * @param ctx the parse tree
	 */
	void enterRetorno(CompiladorParser.RetornoContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#retorno}.
	 * @param ctx the parse tree
	 */
	void exitRetorno(CompiladorParser.RetornoContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#tipo}.
	 * @param ctx the parse tree
	 */
	void enterTipo(CompiladorParser.TipoContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#tipo}.
	 * @param ctx the parse tree
	 */
	void exitTipo(CompiladorParser.TipoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expNegacion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpNegacion(CompiladorParser.ExpNegacionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expNegacion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpNegacion(CompiladorParser.ExpNegacionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expDecimal}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpDecimal(CompiladorParser.ExpDecimalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expDecimal}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpDecimal(CompiladorParser.ExpDecimalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expTrue}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpTrue(CompiladorParser.ExpTrueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expTrue}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpTrue(CompiladorParser.ExpTrueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expBinaria}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpBinaria(CompiladorParser.ExpBinariaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expBinaria}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpBinaria(CompiladorParser.ExpBinariaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expParentizada}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpParentizada(CompiladorParser.ExpParentizadaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expParentizada}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpParentizada(CompiladorParser.ExpParentizadaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expCaracter}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpCaracter(CompiladorParser.ExpCaracterContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expCaracter}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpCaracter(CompiladorParser.ExpCaracterContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expEntero}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpEntero(CompiladorParser.ExpEnteroContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expEntero}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpEntero(CompiladorParser.ExpEnteroContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expVariable}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpVariable(CompiladorParser.ExpVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expVariable}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpVariable(CompiladorParser.ExpVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expCadena}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpCadena(CompiladorParser.ExpCadenaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expCadena}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpCadena(CompiladorParser.ExpCadenaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expFuncion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpFuncion(CompiladorParser.ExpFuncionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expFuncion}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpFuncion(CompiladorParser.ExpFuncionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expFalse}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void enterExpFalse(CompiladorParser.ExpFalseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expFalse}
	 * labeled alternative in {@link CompiladorParser#expresion}.
	 * @param ctx the parse tree
	 */
	void exitExpFalse(CompiladorParser.ExpFalseContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#operadorBinario}.
	 * @param ctx the parse tree
	 */
	void enterOperadorBinario(CompiladorParser.OperadorBinarioContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#operadorBinario}.
	 * @param ctx the parse tree
	 */
	void exitOperadorBinario(CompiladorParser.OperadorBinarioContext ctx);
	/**
	 * Enter a parse tree produced by {@link CompiladorParser#argumentos}.
	 * @param ctx the parse tree
	 */
	void enterArgumentos(CompiladorParser.ArgumentosContext ctx);
	/**
	 * Exit a parse tree produced by {@link CompiladorParser#argumentos}.
	 * @param ctx the parse tree
	 */
	void exitArgumentos(CompiladorParser.ArgumentosContext ctx);
}