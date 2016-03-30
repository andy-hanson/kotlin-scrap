package org.noze.parse

import org.noze.CompileContext
import org.noze.Loc
import org.noze.ast.Declaration
import org.noze.ast.Expr
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.ast.Signature
import org.noze.ast.Statement
import org.noze.ast.TypeAst
import org.noze.symbol.Name
import org.noze.token.Kw
import org.noze.token.Token
import org.noze.token.Tkw
import org.noze.type.Type

fun parse(rootToken: Token.Group.Block, ctx: CompileContext): ModuleAst =
	Parser(ctx).parseModule(Lines(rootToken))

	/*
	For each line:

	1. Take a block off the end. (Currently, there must be a block!)

	First token must be a Name.
	Next token must be a TypeName.
	Then take args (Name, TypeName).

	If no args, this is a value. (NotImplementedException)

	Else, this is a function and should end in a Block.

	*/

class Parser(private val ctx: CompileContext) {
	fun parseModule(lines: Lines): ModuleAst {
		// TODO: https://youtrack.jetbrains.com/issue/KT-6947
		val declarations = lines.mapSlices { parseDeclaration(it) }
		return ModuleAst(lines.loc, declarations)
	}

	fun parseDeclaration(tokens: Tokens): Declaration {
		val first = tokens.head()
		val tail = tokens.tail()

		return when (first) {
			is Token.Keyword -> when (first.kind) {
				Kw.FN -> parseFn(tail)
				else -> TODO()
			}
			else -> throw unexpected(first)
		}
	}

	// e.g.:
	// [fn] add Int a Int b Int
	//	<block content>
	// ([fn] taken out by parseDeclaration)
	fun parseFn(tokens: Tokens): Declaration.Fn {
		val (sigTokens, block) = beforeAndBlock(tokens)

		val name = parseName(sigTokens.first())
		val (returnType, paramTokens) = takeType(sigTokens.tail())
		val parameters = parseParameters(paramTokens)

		//val (type, rest) = takeType(tokens)

		val sig = Signature(sigTokens.loc, returnType, parameters)
		val body = parseBlock(block)
		return Declaration.Fn(tokens.loc, name, sig, body)
	}

	fun parseBlock(lines: Lines): Expr.Block {
		val statements = lines.rtail().mapSlices { parseStatement(it) }
		val returned = parseExpr(lines.lastSlice())
		return Expr.Block(lines.loc, statements, returned)
	}

	fun parseStatement(tokens: Tokens): Statement {
		//TODO: look for '='
		return parseExpr(tokens)
	}

	fun parseExpr(tokens: Tokens): Expr {
		val head = tokens.head()
		val tail = tokens.tail()

		return if (tail.isEmpty())
			parseSingle(head)
		else {
			//TODO:RENAME
			fun parseExprPlain(): Expr {
				val args = tail.map { parseSingle(it) }
				return Expr.Call(tokens.loc, parseSingle(head), args)
			}

			when (head) {
				is Token.Keyword -> when (head.kind) {
					Kw.COND -> parseCond(tokens.loc, tail)
					else -> parseExprPlain()
				}
				else -> parseExprPlain()
			}
		}
	}

	fun parseCond(loc: Loc, tokens: Tokens): Expr.Cond {
		ctx.check(tokens.size() == 3, tokens.loc) { it.condArgs() }
		val args = tokens.map { parseSingle(it) }
		val (condition, ifTrue, ifFalse) = args
		return Expr.Cond(loc, condition, ifTrue, ifFalse)
	}

	fun parseSingle(token: Token): Expr {
		fun lit(value: Expr.Literal.Value): Expr.Literal =
			Expr.Literal(token.loc, value)

		return when (token) {
			is Token.Keyword -> when (token.kind) {
				Kw.FALSE -> lit(Expr.Literal.Value.Bool(false))
				Kw.TRUE -> lit(Expr.Literal.Value.Bool(true))
				else -> throw unexpected(token)
			}
			is Token.Literal ->
				Expr.Literal(token.loc, token.value)
			is Token.Name ->
				Expr.Access(token.loc, token.name)
			else ->
				throw unexpected(token)
		}
	}

	fun parseParameters(tokens: Tokens): List<Parameter> {
		var t = tokens
		val params = mutableListOf<Parameter>()
		while (!t.isEmpty()) {
			val name = parseName(t.head())
			val (type, rest) = takeType(t.tail())
			params += Parameter(Loc(t.loc.start, rest.loc.start), name, type)
			t = rest
		}
		return params.toList()
	}

	fun parseName(token: Token): Name =
		when (token) {
			is Token.Name -> token.name
			else -> throw unexpected(token)
		}

	fun takeType(tokens: Tokens): Pair<TypeAst, Tokens> =
		Pair(parseType(tokens.head()), tokens.tail())

	fun parseType(token: Token): TypeAst {
		return when (token) {
			is Token.TypeKeyword -> {
				val type = when (token.kind) {
					Tkw.BOOL -> Type.Builtin.Bool
					Tkw.FLOAT -> Type.Builtin.Float
					Tkw.INT -> Type.Builtin.Int
				}
				TypeAst.Builtin(token.loc, type)
			}
			is Token.TypeName -> TypeAst.Named(token.loc, token.name)
			else -> throw unexpected(token)
		}
	}

	fun unexpected(token: Token) =
		ctx.fail(token.loc) { it.unexpected(token) }

	data class BeforeAndBlock(val tokens: Tokens, val lines: Lines)
	fun beforeAndBlock(tokens: Tokens): BeforeAndBlock {
		val (before, opBlock) = beforeAndOpBlock(tokens)
		val block = opBlock ?: throw ctx.fail(tokens.loc) { it.expectedBlock() }
		return BeforeAndBlock(before, block)
	}

	data class BeforeAndOpBlock(val tokens: Tokens, val lines: Lines?)
	fun beforeAndOpBlock(tokens: Tokens): BeforeAndOpBlock =
		if (tokens.isEmpty())
			BeforeAndOpBlock(tokens, null)
		else {
			val block = tokens.last()
			when (block) {
				is Token.Group.Block -> BeforeAndOpBlock(tokens.rtail(), Lines(block))
				else -> BeforeAndOpBlock(tokens, null)
			}
		}

}