package org.noze

/*
private val source = """
fn plus-one Int x Int
	+ 1 x

fn plus Int a Int b Int
	+ a b

fn main Int x Int
	|| Also works: plus: plus-one x, 1
	plus 1: plus-one x
"""
*/

// TODO: Generate ctr, toString for records
// Then, make New(type: TypeAst, args: List<Expr>) ast type.
//	fn foo Point x Real
//		Point x x
private val source = """
rec Point
	x Real32
	y Real32
"""

fun main(args: Array<String>) {
	val noze = NozeRuntime()
	val module = noze.load(source)
	val Point = module.getType("Point")
	val p = Point.getConstructor(Float::class.java, Float::class.java).newInstance(1.0.toFloat(), 2.0.toFloat())
	println(p)
	//module.printDebug()
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
