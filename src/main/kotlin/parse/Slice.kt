package org.noze.parse

import org.noze.Loc
import org.noze.token.Token

abstract class Slice<Self : Slice<Self, SubType>, SubType : Token>(
	private val tokens: Array<SubType>,
	private val start: Int,
	private val end: Int,
	val loc: Loc) : Iterable<SubType> {

	fun size(): Int =
		end - start

	fun isEmpty(): Boolean =
		start == end

	fun head(): SubType =
		tokens[start]

	fun second(): SubType =
		tokens[start + 1]

	fun last(): SubType =
		tokens[end - 1]

	fun nextToLast(): SubType =
		tokens[end - 2]

	fun tail(): Self =
		chopStart(start + 1)

	fun rtail(): Self =
		chopEnd(end - 1)

	//opSplitOnce

	inner class SplitOnceResult(val before: Self, val at: SubType, val after: Self) {
		operator fun component1() = before
		operator fun component2() = at
		operator fun component3() = after
	}
	fun trySplitOnce(splitOn: (SubType) -> Boolean): SplitOnceResult? {
		for (i in indices()) {
			val token = tokens[i]
			if (splitOn(token))
				return SplitOnceResult(chopEnd(i), token, chopStart(i + 1))
		}
		return null
	}

	inner class BeforeAndAt(val before: Self, at: SubType)
	inner class SplitManyResult(val splits: List<BeforeAndAt>, val last: Self)
	fun splitMany(splitOn: (SubType) -> Boolean): SplitManyResult {
		var iLast = start
		val out = mutableListOf<BeforeAndAt>()
		for (i in indices()) {
			val token = tokens[i]
			if (splitOn(token)) {
				out += BeforeAndAt(chop(iLast, i), token)
				iLast = i + 1
			}
		}
		return SplitManyResult(out, chopStart(iLast))
	}

	fun splitManyAndIgnoreSplitters(splitOn: (SubType) -> Boolean): List<Self> {
		val split = splitMany(splitOn)
		return split.splits.map { it.before } + listOf(split.last)
	}

	override fun iterator(): Iterator<SubType> =
		ArraySliceIterator(tokens, start, end)

	//map

	protected abstract fun copy(tokens: Array<SubType>, start: Int, end: Int, loc: Loc): Self

	private fun slice(newStart: Int, newEnd: Int, newLoc: Loc): Self =
		copy(tokens, newStart, newEnd, newLoc)

	protected fun chop(newStart: Int, newEnd: Int): Self =
		slice(newStart, newEnd, Loc(tokens[newStart].loc.start, tokens[newEnd - 1].loc.end))

	private fun chopStart(newStart: Int): Self =
		slice(newStart, end, if (newStart == end) loc else loc.copy(start = tokens[newStart].loc.start))

	private fun chopEnd(newEnd: Int): Self =
		slice(start, newEnd, if (newEnd == start) loc else loc.copy(end = tokens[newEnd - 1].loc.end))

	//TODO: indicesWithTokens
	private fun indices(): IntRange =
		start..(end - 1)
}

class Lines(tokens: Array<Token.Group.Line>, start: Int, end: Int, loc: Loc) : Slice<Lines, Token.Group.Line>(tokens, start, end, loc) {
	constructor(group: Token.Group.Block) : this(group.subTokens, 0, group.subTokens.size, group.loc)

	override fun copy(tokens: Array<Token.Group.Line>, start: Int, end: Int, loc: Loc) =
		Lines(tokens, start, end, loc)

	fun slices(): Iterable<Tokens> =
		map { Tokens(it) }

	fun headSlice(): Tokens =
		Tokens(head())

	fun lastSlice(): Tokens =
		Tokens(last())

	fun<R> mapSlices(mapper: (Tokens) -> R): List<R> =
		slices().map(mapper).toList()
}

class Tokens(tokens: Array<Token>, start: Int, end: Int, loc: Loc) : Slice<Tokens, Token>(tokens, start, end, loc) {
	constructor(group: Token.Group.Line) : this(group.subTokens, 0, group.subTokens.size, group.loc)

	override fun copy(tokens: Array<Token>, start: Int, end: Int, loc: Loc) =
		Tokens(tokens, start, end, loc)
}

private class ArraySliceIterator<A>(val array: Array<A>, val start: Int, val end: Int) : Iterator<A> {
	var idx = start

	override fun next(): A {
		val next = array[idx]
		idx = idx + 1
		return next
	}

	override fun hasNext(): Boolean =
		idx != end
}
