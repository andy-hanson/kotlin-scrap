package org.noze.language

import org.noze.lex.GroupBuilder
import org.noze.symbol.Name
import org.noze.symbol.Symbol
import org.noze.token.Token
import org.noze.type.Type

interface Language {
	// Lex
	fun commentNeedsSpace(): String
	fun emptyBlock(): String
	fun leadingZero(): String
	fun mismatchedGroupClose(expectedKind: GroupBuilder.Kind, actualKind: GroupBuilder.Kind): String
	fun noLeadingSpace(): String
	fun nonLeadingTab(): String
	fun tooMuchIndent(): String
	fun trailingDocComment(): String
	fun trailingSpace(): String
	fun unrecognizedCharacter(ch: Char): String

	// Parse
	fun condArgs(): String
	fun expectedBlock(): String
	fun expectedNothing(): String
	fun expectedSomething(): String
	fun unexpected(token: Token): String

	// Check
	fun cantBind(name: Symbol<*>): String
	fun cantCombineTypes(a: Type, b: Type): String
	fun expectedFnType(actual: Type): String
	fun nameAlreadyAssigned(name: Symbol<*>): String
	fun numArgs(expected: Int, actual: Int): String
	fun shadow(name: Name): String
	//TODO: args
	fun typeNotAssignable(expected: Type, actual: Type): String
}

object English : Language {
	// Lex
	override fun commentNeedsSpace() =
		"COMMENT NEEDS A SPACE"

	override fun emptyBlock() =
		"Empty block!!#!!!!!"

	override fun leadingZero() =
		"Leading 0 must be followed by 'b', 'x', or decimal point '.'."

	override fun mismatchedGroupClose(expectedKind: GroupBuilder.Kind, actualKind: GroupBuilder.Kind) =
		"Trying to close $actualKind, but last opened was $expectedKind"

	override fun nameAlreadyAssigned(name: Symbol<*>) =
		"ALREADY ASSIGNED $name"

	override fun noLeadingSpace() =
		"NO LEAD A SPACE!"

	override fun nonLeadingTab() =
		"NON LEAD A YOU TAB"

	override fun tooMuchIndent() =
		"TOO MUCH A INDENT YOU TAB YOU"

	override fun trailingDocComment() =
		"TRAILING DOC COMMENT"

	override fun trailingSpace() =
		"YOU SPACE A TRAILING!"

	override fun unrecognizedCharacter(ch: Char) =
		"Unrecognized character '$ch'"

	// Parse
	override fun expectedBlock() =
		"YOU GOT TO HAVE A BLOCK THERE"

	override fun expectedNothing() =
		"Expected nothing here"

	override fun expectedSomething() =
		"Expected something here"

	override fun unexpected(token: Token) =
		"I NOT SEE THAT ONE COMING! $token"

	// Check
	override fun cantBind(name: Symbol<*>) =
		"Can't find a definition for `$name`"

	override fun cantCombineTypes(a: Type, b: Type) =
		"Can't combine types $a and $b"

	override fun condArgs() =
		"`cond` expects 3 arguments"

	override fun expectedFnType(actual: Type) =
		"Expected a function here, got $actual"

	override fun numArgs(expected: Int, actual: Int) =
		"Expected $expected arguments, got $actual"

	override fun shadow(name: Name) =
		"Can't shadow $name"

	override fun typeNotAssignable(expected: Type, actual: Type) =
		"Expected $expected, got $actual"
}
