import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import org.noze.codeGen.loc

fun point(): ByteArray {
	val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

	cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "hello/Point", null, "java/lang/Object", null)
	cw.visitSource("Point.java", null)

	// Fields
	cw.visitField(Opcodes.ACC_PUBLIC, "x", "F", null, null)
	cw.visitField(Opcodes.ACC_PUBLIC, "y", "F", null, null)

	fun addCtr() {
		var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(FF)V", null, null)
		mv.visitCode()

		val l0 = Label()
		mv.loc(l0, 7)

		// Initialize
		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

		// Set X
		mv.loc(8)
		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitVarInsn(Opcodes.FLOAD, 1)
		mv.visitFieldInsn(Opcodes.PUTFIELD, "hello/Point", "x", "F")

		// Set Y
		mv.loc(9)
		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitVarInsn(Opcodes.FLOAD, 2)
		mv.visitFieldInsn(Opcodes.PUTFIELD, "hello/Point", "y", "F")

		// Return
		mv.loc(10)
		mv.visitInsn(Opcodes.RETURN)

		val l1 = Label()
		mv.visitLabel(l1)
		mv.visitLocalVariable("this", "Lhello/Point;", null, l0, l1, 0)
		mv.visitLocalVariable("x", "F", null, l0, l1, 1)
		mv.visitLocalVariable("y", "F", null, l0, l1, 2)
		mv.visitMaxs(0, 0)
		mv.visitEnd()
	}
	addCtr()

	fun addToStr() {
		val mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null)
		mv.visitCode()

		val l0 = Label()
		mv.loc(l0, 13)

		val sb = Type.getInternalName(StringBuilder::class.java)
		val sbInit = Type.getConstructorDescriptor(StringBuilder::class.java.getConstructor(String::class.java))
		val sbToString = Type.getMethodDescriptor(StringBuilder::class.java.getMethod("toString"))

		val sbAppendStr = Type.getMethodDescriptor(StringBuilder::class.java.getMethod("append", String::class.java))
		val sbAppendFloat = Type.getMethodDescriptor(StringBuilder::class.java.getMethod("append", java.lang.Float.TYPE))
		fun appendStr() {
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", sbAppendStr, false)
		}
		fun appendFloat() {
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "append", sbAppendFloat, false)
		}

		mv.visitTypeInsn(Opcodes.NEW, sb)
		mv.visitInsn(Opcodes.DUP)
		mv.visitLdcInsn("(Point ")
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, sb, "<init>", sbInit, false)

		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitFieldInsn(Opcodes.GETFIELD, "hello/Point", "x", "F")
		appendFloat()

		mv.visitLdcInsn(" ")
		appendStr()

		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitFieldInsn(Opcodes.GETFIELD, "hello/Point", "y", "F")
		appendFloat()

		mv.visitLdcInsn(")")
		appendStr()

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sb, "toString", sbToString, false)
		mv.visitInsn(Opcodes.ARETURN)

		val l1 = Label()
		mv.visitLabel(l1)
		mv.visitLocalVariable("this", "Lhello/Point;", null, l0, l1, 0)
		mv.visitMaxs(0, 0)
		mv.visitEnd()
	}
	addToStr()

	cw.visitEnd()
	return cw.toByteArray()
}

fun foo(): ByteArray {
	val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

	cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "hello/HelloWorld", null, "java/lang/Object", null)
	cw.visitSource("HelloWorld.java", null)

	val mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "()V", null, null)
	mv.visitCode()
	val l0 = Label()
	mv.visitLabel(l0)
	mv.visitLineNumber(7, l0)

	val exc = Type.getInternalName(Exception::class.java)
	val excInit = Type.getConstructorDescriptor(Exception::class.java.getConstructor(String::class.java))

	mv.visitTypeInsn(Opcodes.NEW, exc)
	mv.visitInsn(Opcodes.DUP)
	mv.visitLdcInsn("This exception is thrown intentionally!")
	mv.visitMethodInsn(Opcodes.INVOKESPECIAL, exc, "<init>", excInit, false)
	mv.visitInsn(Opcodes.ATHROW)

	mv.visitInsn(Opcodes.RETURN)
	mv.visitMaxs(0, 0)
	mv.visitEnd()

	//cw.visitEnd() ?
	return cw.toByteArray()
}


fun compileJunk(name: String): ByteArray {
	val cw = ClassWriter(0)

	cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "hello/HelloWorld", null, "java/lang/Object", null)
	cw.visitSource("HelloWorld.java", null)

	fun doA() {
		var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
		mv.visitCode()
		val l0 = Label()
		mv.visitLabel(l0)
		mv.visitLineNumber(4, l0)
		mv.visitVarInsn(Opcodes.ALOAD, 0)
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
		mv.visitInsn(Opcodes.RETURN)
		val l1 = Label()
		mv.visitLabel(l1)
		mv.visitLocalVariable("this", "Lhello/HelloWorld;", null, l0, l1, 0)
		mv.visitMaxs(1, 1)
		mv.visitEnd()
	}

	fun doB() {
		val mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
		mv.visitCode()
		val l0 = Label()
		mv.visitLabel(l0)
		mv.visitLineNumber(7, l0)
		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
		mv.visitLdcInsn(String.format("Hello, %s!", name))
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		val l1 = Label()
		mv.visitLabel(l1)
		mv.visitLineNumber(8, l1)
		mv.visitInsn(Opcodes.RETURN)
		val l2 = Label()
		mv.visitLabel(l2)
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, l0, l2, 0)
		mv.visitMaxs(2, 1)
		mv.visitEnd()
	}

	doA()
	doB()

	cw.visitEnd()

	return cw.toByteArray()
}
