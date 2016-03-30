package org.noze.util

import java.util.ArrayList

@Suppress("UNCHECKED_CAST")
fun<A, B : A> ArrayList<A>.castToTypedArray(klass: Class<B>): Array<B> =
	toArray(java.lang.reflect.Array.newInstance(klass, size) as Array<B>)

@Suppress("UNCHECKED_CAST")
fun<A, B> Array<out A>.mapToArray(klass: Class<B>, map: (A) -> B): Array<B> {
	val bs = java.lang.reflect.Array.newInstance(klass, size) as Array<B>
	for ((idx, value) in this.withIndex())
		bs[idx] = map(value)
	return bs
}
