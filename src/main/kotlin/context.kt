package org.noze

import org.noze.language.English
import org.noze.language.Language

class CompileContext {
	private val language = English
	private val warnings = mutableListOf<ErrorMessage>()

	fun check(condition: Boolean, loc: Loc, message: (Language) -> String) {
		if (!condition)
			throw fail(loc, message)
	}

	fun check(condition: Boolean, pos: Pos, message: (Language) -> String) {
		if (!condition)
			throw fail(pos, message)
	}

	fun fail(loc: Loc, message: (Language) -> String): CompileError =
		CompileError(errorMessage(loc, message))

	fun fail(pos: Pos, message: (Language) -> String): CompileError =
		CompileError(errorMessage(Loc.Companion.singleChar(pos), message))

	fun warn(loc: Loc, message: (Language) -> String) {
		warnings += errorMessage(loc, message)
	}

	fun warn(pos: Pos, message: (Language) -> String) {
		warn(Loc.singleChar(pos), message)
	}

	private fun errorMessage(loc: Loc, message: (Language) -> String) =
		ErrorMessage(loc, message(language))
}

class CompileError(message: ErrorMessage) : Exception(message.message)

data class ErrorMessage(val loc: Loc, val message: String)

