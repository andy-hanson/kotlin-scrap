package org.noze

//move
data class Pos(val line: Short, val column: Short) {
	companion object {
		val START = Pos(1, 1)
	}

	fun nextColumn(): Pos =
		copy(column = (column + 1).toShort())

	fun nextLine(): Pos =
		copy(line = (line + 1).toShort())

	override fun toString(): String =
		"$line:$column"
}

data class Loc(val start: Pos, val end: Pos) {
	companion object {
		fun singleChar(pos: Pos): Loc =
			Loc(pos, pos.nextColumn())
	}

	override fun toString(): String =
		"$start"//"$start-$end"
}
