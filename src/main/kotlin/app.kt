package org.noze

import java.lang.ClassLoader

import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter

// TODO:
// * Parse number literals
// 		Add token type
//		Add Literal.Value.Float
// * Add ability to call functions!

/*
val source = """
fn add Int a Int b Int
	cond true a b
"""
*/

private val source = """
fn one Int
	0b101
"""

fun main(args: Array<String>) {
	val bytes = compile(source)
	//printClass(bytes)
	runMain(bytes)
}

fun runMain(bytes: ByteArray) {
	val loader = DynamicClassLoader()
	val klass = loader.define("hello.HelloWorld", bytes)
	//val method = klass.getMethod("add", Int::class.java, Int::class.java)
	//val result = method.invoke(null, 1, 2)
	val method = klass.getMethod("one")
	val result = method.invoke(null)
	println(result)

}

fun printClass(bytes: ByteArray) {
	val reader = ClassReader(bytes)
	val visitor = TraceClassVisitor(PrintWriter(System.out))
	reader.accept(visitor, ClassReader.SKIP_DEBUG)
}

class DynamicClassLoader : ClassLoader {
	constructor() : super()
	constructor(parent: ClassLoader) : super(parent)

	fun define(className: String, bytecode: ByteArray): Class<out Any> =
		super.defineClass(className, bytecode, 0, bytecode.size)
}
