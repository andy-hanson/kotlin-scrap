package org.noze.lex

import java.util.ArrayList
import java.util.Stack

import org.noze.CompileContext
import org.noze.Loc
import org.noze.Pos
import org.noze.token.Token
import org.noze.util.castToTypedArray
import org.noze.util.noElse

class GroupContext(private val ctx: CompileContext) {
	var cur = GroupBuilder(GroupBuilder.Kind.BLOCK, Pos.START)
	private var stack = Stack<GroupBuilder>()

	init {
		openLine(Pos.START)
	}

	fun finish(endPos: Pos): Token.Group.Block {
		closeLine(endPos)
		assert(stack.isEmpty())
		return cur.finish(endPos) as Token.Group.Block
	}

	operator fun plusAssign(token: Token) {
		cur += token
	}

	private fun dropGroup() {
		cur = stack.pop()
	}

	private fun openGroup(kind: GroupBuilder.Kind, openPos: Pos) {
		stack.push(cur)
		cur = GroupBuilder(kind, openPos)
	}

	fun maybeCloseGroup(kind: GroupBuilder.Kind, closePos: Pos) {
		if (cur.kind == kind)
			closeGroupNoCheck(kind, closePos)
	}

	private fun closeGroup(kind: GroupBuilder.Kind, closePos: Pos) {
		ctx.check(cur.kind == kind, closePos) { it.mismatchedGroupClose(cur.kind, kind) }
		closeGroupNoCheck(kind, closePos)
	}

	private fun closeGroupNoCheck(kind: GroupBuilder.Kind, closePos: Pos) {
		val justClosed = cur.finish(closePos)
		dropGroup()
		noElse(when (kind) {
			GroupBuilder.Kind.LINE -> {
				if (!justClosed.isEmpty())
					this += justClosed
				else {}
			}
			GroupBuilder.Kind.BLOCK -> {
				ctx.check(!justClosed.isEmpty(), closePos) { it.emptyBlock() }
				this += justClosed
			}
		})
	}

	fun closeGroupsForDedent(pos: Pos) {
		closeLine(pos)
		closeGroup(GroupBuilder.Kind.BLOCK, pos)
		//TODO: close parenthesis
	}

	fun openBlock(pos: Pos) {
		openGroup(GroupBuilder.Kind.BLOCK, pos)
	}

	fun openLine(pos: Pos) {
		openGroup(GroupBuilder.Kind.LINE, pos)
	}

	fun closeLine(pos: Pos) {
		closeGroup(GroupBuilder.Kind.LINE, pos)
	}
}

class GroupBuilder(val kind: Kind, val openPos: Pos) {
	enum class Kind {
		BLOCK,
		LINE
	}

	private val subTokens = ArrayList<Token>()

	fun isEmpty(): Boolean =
		subTokens.isEmpty()

	operator fun plusAssign(token: Token) {
		subTokens += token
	}

	@Suppress("UNCHECKED_CAST")
	fun finish(endPos: Pos): Token.Group<*,*> {
		val loc = Loc(openPos, endPos)
		return when (kind) {
			Kind.BLOCK ->
				Token.Group.Block(loc, subTokens.castToTypedArray<Token, Token.Group.Line>(Token.Group.Line::class.java))
			Kind.LINE ->
				Token.Group.Line(loc, subTokens.castToTypedArray<Token, Token>(Token::class.java))
		}
	}
}
