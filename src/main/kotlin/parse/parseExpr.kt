package org.noze.parse

import org.noze.Loc
import org.noze.ast.Expr
import org.noze.parse.Tokens
import org.noze.token.Kw
import org.noze.token.Token

/*
a b c ==> a(b, c)
a: b c ==> a(b(c))
*/
fun Parser.parseExpr(tokens: Tokens): Expr {
	val head = tokens.head()
	val tail = tokens.tail()

	if (tail.isEmpty())
		return parseSingle(head)

	val split = tail.trySplitOnce { Token.Keyword.match(it, Kw.COLON) }

	val args = if (split == null) {
		tail.map { parseSingle(it) }
	} else {
		val (before, at, after) = split
		val beforeArgs = before.map { parseSingle(it) }
		val afterParts = after.splitManyAndIgnoreSplitters { Token.Keyword.match(it, Kw.COMMA) }
		val afterArgs = afterParts.map { parseExpr(it) }
		beforeArgs + afterArgs
	}

	return if (Token.Keyword.match(head, Kw.COND))
		parseCond(tokens.loc, args)
	else
		Expr.Call(tokens.loc, parseSingle(head), args)
}

fun Parser.parseBlock(lines: Lines): Expr.Block {
	val statements = lines.rtail().mapSlices { parseStatement(it) }
	val returned = parseExpr(lines.lastSlice())
	return Expr.Block(lines.loc, statements, returned)
}

fun Parser.parseCond(loc: Loc, args: List<Expr>): Expr.Cond {
	ctx.check(args.size == 3, loc) { it.condArgs() }
	val (condition, ifTrue, ifFalse) = args
	return Expr.Cond(loc, condition, ifTrue, ifFalse)
}

fun Parser.parseSingle(token: Token): Expr {
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