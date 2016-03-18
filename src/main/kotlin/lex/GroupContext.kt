package org.noze.lex

import java.util.ArrayList
import java.util.Stack

import org.noze.CompileContext
import org.noze.Loc
import org.noze.Pos
import org.noze.token.Token

class GroupContext(private val ctx: CompileContext) {
	private var cur = GroupBuilder(GroupBuilder.BLOCK, Pos.START)
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

	private fun openGroup(kind: Int, openPos: Pos) {
		stack.push(cur)
		cur = GroupBuilder(kind, openPos)
	}

	fun maybeCloseGroup(kind: Int, closePos: Pos) {
		if (cur.kind == kind)
			closeGroupNoCheck(kind, closePos)
	}

	private fun closeGroup(kind: Int, closePos: Pos) {
		ctx.check(cur.kind == kind, closePos) { it.mismatchedGroupClose(cur.kind, kind) }
		closeGroupNoCheck(kind, closePos)
	}

	private fun closeGroupNoCheck(kind: Int, closePos: Pos) {
		val justClosed = cur.finish(closePos)
		dropGroup()
		when (kind) {
			GroupBuilder.LINE ->
				if (!justClosed.isEmpty())
					this += justClosed
			GroupBuilder.BLOCK -> {
				ctx.check(!justClosed.isEmpty(), closePos) { it.emptyBlock() }
				this += justClosed
			}
			else ->
				throw Exception()
		}
	}

	fun closeGroupsForDedent(pos: Pos) {
		closeLine(pos)
		closeGroup(GroupBuilder.BLOCK, pos)
		//TODO: close parenthesis
	}

	fun openBlock(pos: Pos) {
		openGroup(GroupBuilder.BLOCK, pos)
	}

	fun openLine(pos: Pos) {
		openGroup(GroupBuilder.LINE, pos)
	}

	fun closeLine(pos: Pos) {
		closeGroup(GroupBuilder.LINE, pos)
	}
}

class GroupBuilder(val kind: Int, val openPos: Pos) {
	companion object {
		const val BLOCK = 0
		const val LINE = 1
	}

	private val subTokens = ArrayList<Token>()

	operator fun plusAssign(token: Token) {
		subTokens += token
	}

	@Suppress("UNCHECKED_CAST")
	fun finish(endPos: Pos): Token.Group<*,*> {
		val loc = Loc(openPos, endPos)
		return when (kind) {
			BLOCK -> Token.Group.Block(loc, subTokens.toTypedArray<Token, Token.Group.Line>(Token.Group.Line::class.java))
			LINE -> Token.Group.Line(loc, subTokens.toTypedArray<Token, Token>(Token::class.java))
			else -> throw Exception()
		}
	}
}

@Suppress("UNCHECKED_CAST")
fun<A, B : A> ArrayList<A>.toTypedArray(klass : Class<B>): Array<B> =
	toArray(java.lang.reflect.Array.newInstance(klass, size) as Array<B>)//Array<B>(size))

