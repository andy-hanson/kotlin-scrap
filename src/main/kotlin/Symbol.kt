package org.noze.symbol

abstract class Symbol<Self : Symbol<Self>>(str: String) {
	val string: String = str.intern()

	@Suppress("UNCHECKED_CAST")
	override fun equals(other: Any?): Boolean =
		other != null && javaClass == other.javaClass && equals(other as Self)

	fun equals(other: Self): Boolean =
		string === other.string

	override fun hashCode(): Int =
		string.hashCode()

	override fun toString(): String {
		val className = javaClass.name
		val shortClassName = className.split('.').last()
		return "$shortClassName($string)"
	}
}

class Name(string: String) : Symbol<Name>(string)

class TypeName(string: String) : Symbol<TypeName>(string)
