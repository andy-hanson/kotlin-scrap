package org.noze.parse

import org.noze.token.Token

fun Parser.unexpected(token: Token) =
	ctx.fail(token.loc) { it.unexpected(token) }

data class BeforeAndBlock(val tokens: Tokens, val lines: Lines)
fun Parser.beforeAndBlock(tokens: Tokens): BeforeAndBlock {
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

fun Parser.checkSolo(tokens: Tokens): Token {
	checkNonEmpty(tokens)
	checkEmpty(tokens.tail())
	return tokens.head()
}

fun Parser.checkNonEmpty(tokens: Tokens) {
	ctx.check(!tokens.isEmpty(), tokens.loc) { it.expectedSomething() }
}

fun Parser.checkEmpty(tokens: Tokens) {
	ctx.check(tokens.isEmpty(), tokens.loc) { it.expectedNothing() }
}
