package org.noze.util

import java.io.ByteArrayOutputStream
import java.io.InputStream

// Wrap this around a 'when' statement to ensure that every possibility is covered even without an 'else' branch.
fun noElse(value: Unit): Unit {}


fun inputStreamToByteArray(input: InputStream): ByteArray {
	val out = ByteArrayOutputStream()
	val tmp = ByteArray(4096)

	while (true) {
		val nRead = input.read(tmp)
		if (nRead == 0)
			break
		else
			out.write(tmp, 0, nRead)
	}

	return out.toByteArray()
}
