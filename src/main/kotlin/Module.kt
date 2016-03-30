package org.noze

import org.noze.ast.ModuleAst
import org.noze.check.CheckResult
import org.noze.check.getBindings
import org.noze.check.typeCheck
import org.noze.codeGen.moduleToBytecode
import org.noze.lex.lex
import org.noze.parse.parse
import org.noze.symbol.TypeName
import org.noze.util.inputStreamToByteArray
import org.noze.util.mapToArray
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import java.io.PrintWriter
import java.lang.reflect.Method

class NozeRuntime {
	val classLoader = DynamicClassLoader().apply {
		//registerAsParallelCapable()
	}

	fun load(source: String): Module {
		val ctx = CompileContext()

		val token = lex(source, ctx)

		val ast = parse(token, ctx)

		val bindings = getBindings(ast, ctx)

		val typeBindings = typeCheck(ast, bindings, ctx)

		val checks = CheckResult(bindings, typeBindings)

		val (functions, types) = moduleToBytecode(ast, checks)

		return Module(this, ast, functions, types)
	}

	// TODO: get module from path
}

class Module {
	constructor(runtime: NozeRuntime, ast: ModuleAst, functions: ByteArray, types: Map<TypeName, ByteArray>) {
		this.runtime = runtime
		this.ast = ast
		this.functionsBytes = functions
		this.functionsClass = runtime.classLoader.define("hello.HelloWorld", functions)
		this.typesBytes = types
		this.typesClasses = types.mapValues { entry ->
			// TODO: syntax
			val (name, bytes) = entry
			runtime.classLoader.define(name.string, bytes)
		}
	}

	private val runtime: NozeRuntime
	private val ast: ModuleAst
	private val functionsBytes: ByteArray
	private val functionsClass: Class<*>
	private val typesBytes: Map<TypeName, ByteArray>
	private val typesClasses: Map<TypeName, Class<*>>

	fun getType(name: String): Class<*> =
		typesClasses[TypeName(name)]!!

	fun getFn(name: String, vararg types: Class<*>): Method =
		functionsClass.getMethod(name, *types)

	fun invokeFn(name: String, types: List<Class<*>>, args: List<Any>) =
		getFn(name, *types.toTypedArray()).invoke(null, *args.toTypedArray())

	/*
	fun invokeFn(name: String, vararg args: Any): Any {
		// Getting types from args is dangerous because int args become Integer objects when passed in.
		val types = args.mapToArray<Any, Class<*>>(Class::class.java) { a: Any -> a.javaClass }
		val method = functionsClass.getMethod(name, *types)
		return method.invoke(null, *args)
	}
	*/

	fun printDebug() {
		val stream = runtime.classLoader.getResourceAsStream("hello.HelloWorld")
		//val x = inputStreamToByteArray(stream)
		println(runtime.classLoader.findLoadedClass("hello.HelloWorld"))
		//println(x.size)
		println(functionsBytes.size)
		println("!")
		val reader = ClassReader(stream)
		val visitor = TraceClassVisitor(PrintWriter(System.out))
		reader.accept(visitor, ClassReader.SKIP_DEBUG)
	}

	/*
	fun printDebug() {
		val reader = ClassReader(functionsBytes)
		val visitor = TraceClassVisitor(PrintWriter(System.out))
		reader.accept(visitor, ClassReader.SKIP_DEBUG)
	}
	*/
}


class DynamicClassLoader : ClassLoader {
	constructor() : super()
	constructor(parent: ClassLoader) : super(parent)

	private val defined = mutableMapOf<String, Class<*>>()

	override fun findClass(name: String): Class<*> =
		defined[name]!!

	fun define(className: String, bytecode: ByteArray): Class<*> =
		super.defineClass(className, bytecode, 0, bytecode.size).apply {
			defined[className] = this
		}
}
