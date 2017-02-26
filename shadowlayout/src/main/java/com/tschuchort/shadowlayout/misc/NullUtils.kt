package com.tschuchort.shadowlayout.misc

internal fun allNullOrNoneNull(vararg args: Any?) = allNull(args) || noneNull(args)

internal fun anyNull(vararg args: Any?) = args.any { it == null }

internal fun anyNotNull(vararg args: Any?) = args.any { it != null }

internal fun noneNull(vararg args: Any?) = args.all { it != null }

internal fun allNull(vararg args: Any?) = args.all { it == null }