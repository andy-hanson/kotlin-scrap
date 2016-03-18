package org.noze.token

import org.noze.Loc
import org.noze.ast.Expr
import org.noze.symbol.Name
import org.noze.symbol.TypeName
import org.noze.util.shortClassName

sealed class Token(val loc: Loc) {
	class Keyword(loc: Loc, val kind: Kw) : Token(loc) {
		override fun toString(): String =
			kind.text//"${kind.text}@$loc"
	}

	class TypeKeyword(loc: Loc, val kind: Tkw) : Token(loc) {
		override fun toString(): String =
			kind.text
	}

	class Name(loc: Loc, val name: org.noze.symbol.Name) : Token(loc) {
		override fun toString() =
			name.string
	}

	class TypeName(loc: Loc, val name: org.noze.symbol.TypeName) : Token(loc) {
		override fun toString() =
			name.string
	}

	sealed class Group<Self : Group<Self, SubType>, SubType : Token>(loc: Loc) : Token(loc) {
		abstract val subTokens: Array<SubType>

		fun isEmpty(): Boolean =
			subTokens.isEmpty()

		override fun toString(): String =
			"${shortClassName(this)}(${subTokens.joinToString(", ")})"

		class Block(loc: Loc, override val subTokens: Array<Line>) : Group<Block, Line>(loc)

		class Line(loc: Loc, override val subTokens: Array<Token>) : Group<Line, Token>(loc)
	}

	class Literal(loc: Loc, val value: Expr.Literal.Value) : Token(loc)
}


enum class Kw(val text: String) {
	//CASE("case"),
	COND("cond"),
	DATA("data"),
	FALSE("false"),
	//INTERFACE("interface"),
	FN("fn"),
	OBJECT("object"),
	//THROW("throw"),
	TRUE("true")
}

enum class Tkw(val text: String) {
	BOOL("Bool"),
	INT("Int")
}

private val kwFromNameMap: Map<String, Kw> = Kw.values().associateBy({it.text}, {it})
fun kwFromName(name: Name): Kw?  =
	kwFromNameMap[name.string]

private val tkwFromNameMap: Map<String, Tkw> = Tkw.values().associateBy({it.text}, {it})
fun tkwFromName(name: TypeName): Tkw? =
	tkwFromNameMap[name.string]
