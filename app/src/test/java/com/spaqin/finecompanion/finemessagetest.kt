package com.spaqin.finecompanion

import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Communications.FineMessageCommand
import junit.framework.Assert.assertEquals
import org.junit.Test

/**
 * Created by Spaqin on 2017-11-04.
 */
class finemessagetest {
    @Test
    fun check_validation()
    {
        var fmBytes = byteArrayOf(0xF1.toByte(), 0x01, 15, 3, 4)
        fmBytes.forEach { x -> print(x) }
        var actualfmBytes = FineMessage.fromByteArray(fmBytes)
        print(actualfmBytes)
        var fm = FineMessage(0xF1.toByte(), FineMessageCommand.CAPABILITIES, 15,3)
        print(fm)
        assert(fm.validateMessage())
        assert(actualfmBytes!!.validateMessage())
        assertEquals(fm, actualfmBytes)
    }
    @Test
    fun helloWorld() {
        println("Hello, World!")
    }


    @Test
    fun notReallyAHelloWorld() {
        var A = listOf(25, 30, 21, 45)
        A = A.map { x -> x * 2}
        A.forEach { x -> print("$x ")}
        val name:String? =  if (A[0] == 50) "world" else null
        val realName:String = name!!.capitalize()
        println("\nHello, $realName!")
    }
}