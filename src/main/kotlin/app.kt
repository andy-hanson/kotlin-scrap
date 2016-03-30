package org.noze

private val source = """
fn plus-one Int x Int
	+ 1 x

fn plus Int a Int b Int
	+ a b

fn main Int x Int
	|| Also works: plus: plus-one x, 1
	plus 1: plus-one x
"""

/*
//TODO:
private val source = """
rec Point
	x Real
	y Real
"""
*/

fun main(args: Array<String>) {
	val noze = NozeRuntime()
	val module = noze.load(source)
	module.printDebug()
	//println(module.invokeFn("main", listOf(Int::class.java), listOf(1)))
}

fun runIndy(bytes: ByteArray) {
	//val bytes = indy()
	val loader = DynamicClassLoader()
	val klass = loader.define("hello.HelloWorld", bytes)
	val method = klass.getMethod("main")
	val result = method.invoke(null)
	println(result)
}
