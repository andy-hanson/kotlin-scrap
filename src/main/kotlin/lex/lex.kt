package org.noze.lex

import java.util.Stack

import org.noze.CompileContext
import org.noze.Loc
import org.noze.Pos
import org.noze.ast.Expr
import org.noze.symbol.Name
import org.noze.symbol.TypeName
import org.noze.token.Kw
import org.noze.token.kwFromName
import org.noze.token.Token

const val NULL_CHAR: Char = 0.toChar()

fun lex(source: String, context: CompileContext): Token.Group.Block =
	Lexer(source, context).lex()

private class Lexer(sourceStr: String, private val ctx: CompileContext) {
	val source = SourceContext(sourceStr)
	val groups = GroupContext(ctx)

	fun lex(): Token.Group.Block {
		lexPlain()
		val endPos = source.pos()
		return groups.finish(endPos)
	}

	private fun lexPlain() {
		var indent = 0

		while (true) {
			val startColumn = source.column
			val characterEaten = source.eat()

			fun startPos(): Pos =
				Pos(source.line, startColumn)
			fun loc(): Loc =
				source.locFrom(startPos())

			// Generally, the type of a token is determined by the first character.
			when (characterEaten) {
				NULL_CHAR ->
					return

				' ' ->
					pass()

				'\n' -> {
					if (source.canPeek(-2) && source.peek(-2) == ' ')
						ctx.warn(source.pos()) { it.trailingSpace() }

					source.skipNewlines()
					val oldIndent = indent
					indent = lexIndent()
					if (indent > oldIndent) {
						ctx.check(indent == oldIndent + 1, source.pos()) { it.tooMuchIndent() }
						groups.openBlock(startPos())
						groups.openLine(source.pos())
					} else {
						val p = startPos()
						for (i in indent..(oldIndent - 1))
							groups.closeGroupsForDedent(p)
						groups.closeLine(p)
						groups.openLine(source.pos())
					}
				}

				'|' -> {
					val isDocComment = !source.tryEat('|')
					if (!(source.tryEat(' ') || source.tryEat('\t') || source.peek() == '\n'))
						ctx.warn(source.pos()) { it.commentNeedsSpace() }
					if (isDocComment) {
						val text = source.takeRestOfLine()
						ctx.check(groups.cur.kind == GroupBuilder.Kind.LINE && groups.cur.isEmpty(), loc()) { it.trailingDocComment() }
						groups += Token.DocComment(loc(), text)
					} else
						source.skipRestOfLine()
				}

				':' ->
					groups += Token.Keyword(loc(), Kw.COLON)
				',' ->
					groups += Token.Keyword(loc(), Kw.COMMA)

				in 'A'..'Z' ->
					lexTypeName(startPos())

				in 'a'..'z', '+', '*', '/' ->
					lexName(startPos())

				'\t' ->
					throw ctx.fail(source.pos()) { it.nonLeadingTab() }

				'-' ->
					if (source.peek() in '0'..'9')
						lexNumber(startPos())
					else
						lexName(startPos())

				in '0'..'9' ->
					lexNumber(startPos())

				else ->
					throw ctx.fail(source.pos()) { it.unrecognizedCharacter(characterEaten) }
			}
		}
	}

	private fun lexIndent(): Int {
		val indent = source.skipTabs()
		ctx.check(source.peek() != ' ', source.pos()) { it.noLeadingSpace() }
		return indent
	}

	private fun lexName(startPos: Pos) {
		val name = Name(source.takeName())
		val loc = source.locFrom(startPos)
		val keyword = kwFromName(name)
		groups += if (keyword != null) Token.Keyword(loc, keyword) else Token.Name(loc, name)
	}

	private fun lexTypeName(startPos: Pos) {
		val name = TypeName(source.takeName())
		val loc = source.locFrom(startPos)
		//val keyword = tkwFromName(name)
		//groups += if (keyword != null) Token.TypeKeyword(loc, keyword) else Token.TypeName(loc, name)
		groups += Token.TypeName(loc, name)
	}

	private fun lexNumber(startPos: Pos) {
		val value = if (source.peek(-1) == '0') {
			when (source.peek()) {
				'b' -> {
					source.skip()
					Expr.Literal.Value.Int(source.takeIntBinary())
				}
				'x' -> {
					source.skip()
					Expr.Literal.Value.Int(source.takeIntHex())
				}
				else ->
					source.takeNumDecimal()
			}
		} else
			source.takeNumDecimal()
		val loc = source.locFrom(startPos)
		groups += Token.Literal(loc, value)
	}

}

//TODO: move to util
private fun pass() {}
