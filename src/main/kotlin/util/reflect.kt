package org.noze.util

fun shortClassName(o: Any): String =
	o.javaClass.name.split('.').last()
