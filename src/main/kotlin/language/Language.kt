package org.noze.language

import org.noze.symbol.Name
import org.noze.token.Token

interface Language {
	// Lex
	fun emptyBlock(): String
	fun leadingZero(): String
	fun mismatchedGroupClose(expectedKind: Int, actualKind: Int): String
	fun noLeadingSpace(): String
	fun nonLeadingTab(): String
	fun tooMuchIndent(): String
	fun trailingSpace(): String
	fun unrecognizedCharacter(ch: Char): String

	// Parse
	fun condArgs(): String
	fun expectedBlock(): String
	fun unexpected(token: Token): String

	// Check
	fun cantBind(name: Name): String
	fun shadow(name: Name): String
}

object English : Language {
	// Lex
	override fun emptyBlock() =
		"Empty block!!#!!!!!"

	override fun leadingZero() =
		"Leading 0 must be followed by 'b', 'x', or decimal point '.'."

	override fun mismatchedGroupClose(expectedKind: Int, actualKind: Int) =
		"Trying to close $actualKind, but last opened was $expectedKind"

	override fun noLeadingSpace() =
		"NO LEAD A SPACE!"

	override fun nonLeadingTab() =
		"NON LEAD A YOU TAB"

	override fun tooMuchIndent() =
		"TOO MUCH A INDENT YOU TAB YOU"

	override fun trailingSpace() =
		"YOU SPACE A TRAILING!"

	override fun unrecognizedCharacter(ch: Char) =
		"Unrecognized character '$ch'"

	// Parse
	override fun expectedBlock() =
		"YOU GOT TO HAVE A BLOCK THERE"

	override fun unexpected(token: Token) =
		"I NOT SEE THAT ONE COMING! $token"

	// Check
	override fun cantBind(name: Name) =
		"Can't find a definition for `$name`"

	override fun condArgs() =
		"`cond` expects 3 arguments"

	override fun shadow(name: Name) =
		"Can't shadow $name"
}
