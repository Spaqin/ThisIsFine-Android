package com.spaqin.fineapp

/**
 * Created by mateusz on 18.09.17.
 */
fun Byte.shl(shiftBy: Int) = (this.toInt() shl shiftBy).toByte()
fun Byte.shr(shiftBy: Int) = (this.toInt() shr shiftBy).toByte()
fun Byte.ushr(shiftBy: Int) = (this.toInt() ushr shiftBy).toByte()
fun Short.shl(shiftBy: Int) = (this.toInt() shl shiftBy).toShort()
fun Short.shr(shiftBy: Int) = (this.toInt() shr shiftBy).toShort()
fun Short.ushr(shiftBy: Int) = (this.toInt() ushr shiftBy).toShort()