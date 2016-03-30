package org.noze.parse

import org.noze.CompileContext
import org.noze.Loc
import org.noze.ast.Decl
import org.noze.ast.Expr
import org.noze.ast.ModuleAst
import org.noze.ast.Parameter
import org.noze.ast.Property
import org.noze.ast.Signature
import org.noze.ast.Statement
import org.noze.ast.TypeAst
import org.noze.symbol.Name
import org.noze.symbol.TypeName
import org.noze.token.Kw
import org.noze.token.Token
import org.noze.type.Type

fun parse(rootToken: Token.Group.Block, ctx: CompileContext): ModuleAst =
	Parser(ctx).parseModule(Lines(rootToken))

class Parser(val ctx: CompileContext) {
	fun parseModule(lines: Lines): ModuleAst {
		// TODO: https://youtrack.jetbrains.com/issue/KT-6947
		val declarations = lines.mapSlices { parseDeclaration(it) }
		return ModuleAst(lines.loc, declarations)
	}

	fun parseDeclaration(tokens: Tokens): Decl {
		val first = tokens.head()
		val tail = tokens.tail()

		return when (first) {
			is Token.Keyword -> when (first.kind) {
				Kw.FN -> parseFn(tail)
				Kw.REC -> parseRec(tail)
				else -> throw unexpected(first)
			}
			else -> throw unexpected(first)
		}
	}

	fun parseRec(tokens: Tokens): Decl.Type.Rec {
		val (before, block) = beforeAndBlock(tokens)
		val name = parseTypeName(checkSolo(before))
		val properties = block.mapSlices { parseProperty(it) }
		return Decl.Type.Rec(tokens.loc, name, properties)
	}

	fun parseProperty(tokens: Tokens): Property =
		Property(tokens.loc, parseName(tokens.head()), parseType(tokens.tail()))

	// e.g.:
	// [fn] add Int a Int b Int
	//	<block content>
	// ([fn] taken out by parseDeclaration)
	fun parseFn(tokens: Tokens): Decl.Val.Fn {
		val (sigTokens, block) = beforeAndBlock(tokens)

		val name = parseName(sigTokens.first())
		val (returnType, paramTokens) = takeType(sigTokens.tail())
		val parameters = parseParameters(paramTokens)

		//val (type, rest) = takeType(tokens)

		val sig = Signature(sigTokens.loc, returnType, parameters)
		val body = parseBlock(block)
		return Decl.Val.Fn(tokens.loc, name, sig, body)
	}

	fun parseStatement(tokens: Tokens): Statement {
		//TODO: look for '='
		return parseExpr(tokens)
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

	fun parseTypeName(token: Token): TypeName =
		when (token) {
			is Token.TypeName -> token.name
			else -> throw unexpected(token)
		}

	fun takeType(tokens: Tokens): Pair<TypeAst, Tokens> =
		Pair(parseType(tokens.head()), tokens.tail())

	fun parseType(tokens: Tokens): TypeAst =
		// TODO: multi-token types
		parseType(checkSolo(tokens))

	fun parseType(token: Token): TypeAst =
		when (token) {
			is Token.TypeName -> TypeAst.Access(token.loc, token.name)
			else -> throw unexpected(token)
		}
}
