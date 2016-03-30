package org.noze.lex

import java.util.regex.Pattern

import org.noze.Loc
import org.noze.Pos
import org.noze.ast.Expr
import org.noze.token.Token

class SourceContext {
	private val source: String
	var index: Int
	var line: Short
	var column: Short

	constructor(source: String) {
		var s = source

		// Algorithm requires trailing newline to close any blocks.
		if (!s.endsWith('\n'))
			s = "$s\n"

		this.source = s + NULL_CHAR

		index = 0
		line = Pos.START.line
		column = Pos.START.column
	}

	fun pos(): Pos =
		Pos(line, column)

	fun locFrom(startPos: Pos): Loc =
		Loc(startPos, pos())

	fun canPeek(n: Int = 0): Boolean {
		val idx = index + n
		return 0 <= idx && idx < source.length
	}

	fun peek(n: Int = 0): Char =
		source[index + n]

	fun eat(): Char {
		val char = source[index]
		skip()
		return char
	}

	fun skip(n: Int = 1) {
		index += n
		column = (column + n).toShort()
	}

	// charToEat must not be Char.LineFeed
	fun tryEat(charToEat: Char): Boolean =
		tryEatIf { it == charToEat }

	fun tryEatIf(pred: (Char) -> Boolean): Boolean {
		val canEat = pred(peek())
		if (canEat)
			skip()
		return canEat
	}

	fun skipRestOfLine() {
		skipUntilRegex(Rgx.LINE_FEED)
	}

	fun takeRestOfLine(): String =
		takeUntilRegex(Rgx.LINE_FEED)

	fun skipTabs(): Int =
		skipUntilRegex(Rgx.TABS)

	fun takeIntBinary(): Int =
		java.lang.Integer.valueOf(takeUntilRegex(Rgx.BINARY), 2)

	fun takeIntHex(): Int =
		java.lang.Integer.valueOf(takeUntilRegex(Rgx.HEX), 16)

	fun takeNumDecimal(): Expr.Literal.Value {
		val startIndex = index - 1
		skipUntilRegex(Rgx.DECIMAL)
		return if (peek() == '.') {
			if (peek(1) in '0'..'9') {
				skip()
				skipUntilRegex(Rgx.DECIMAL)
			}
			Expr.Literal.Value.Float(java.lang.Double.valueOf(sliceFromIndex(startIndex)))
		} else
			Expr.Literal.Value.Int(java.lang.Integer.valueOf(sliceFromIndex(startIndex)))
	}

	fun skipNewlines(): Int {
		val startLine = line
		line++
		while (peek() == '\n') {
			index++
			line++
		}
		column = Pos.START.column
		return line - startLine
	}

	private fun skipUntilRegex(rgx: Pattern): Int {
		val startIndex = index

		val matcher = rgx.matcher(source)
		val found = matcher.find(startIndex)
		assert(found)
		index = matcher.start()
		val diff = index - startIndex
		column = (column + diff).toShort()
		return diff
	}

	private fun takeUntilRegex(rgx: Pattern): String {
		val startIndex = index
		skipUntilRegex(rgx)
		return sliceFromIndex(startIndex)
	}

	fun takeName(): String {
		val startIndex = index - 1
		skipUntilRegex(Rgx.NAME)
		return sliceFromIndex(startIndex)
	}

	private fun sliceFromIndex(startIndex: Int): String =
		source.slice(startIndex..(index - 1))
}

private object Rgx {
	val NAME = Pattern.compile("[^a-zA-Z\\+\\-\\*\\/]")
	//private val NAME_RGX = Pattern.compile("[`&\\(\\)\\[\\]\\{\\}|:'\". \\n\\t#^\\\\;,]")
	//private val SPACES_RGX = Pattern.compile("[^ ]")
	val TABS = Pattern.compile("[^\t]")

	val BINARY = Pattern.compile("[^01]")
	val DECIMAL = Pattern.compile("[^0-9]")
	val HEX = Pattern.compile("[^0-9a-f]")

	val LINE_FEED = Pattern.compile("\n")
}
