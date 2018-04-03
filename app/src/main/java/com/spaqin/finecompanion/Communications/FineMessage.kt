package com.spaqin.fineapp.Communications

import com.spaqin.fineapp.shl
import kotlin.experimental.and

/**
 * Created by mateusz on 18.09.17.
 */
data class FineMessage (
        val header: Byte,
        val command: FineMessageCommand,
        var data0: Byte = 0,
        var data1: Byte = 0,
        val checksum: Byte = (header+command.value+data0+data1).toByte()) {

    val ASK_HEADER: Byte = 0xF2.toByte()
    val REPLY_HEADER = 0xF1.toByte()

    companion object {
        fun fromByteArray(ba: ByteArray): FineMessage? {
            if (ba.size != 5)
                return null
            val toRet: FineMessage = FineMessage(ba[0], FineMessageCommand.fromByte(ba[1]), ba[2], ba[3], ba[4])
            if (toRet.validateMessage())
                return toRet
            return null
        }
    }

    /*
    * Returns true on valid msg.
    * */
    fun validateMessage(): Boolean = (header == REPLY_HEADER ||
            header == ASK_HEADER) &&
            command != FineMessageCommand.UNKNOWN &&
            checkChecksum()

    fun toByteArray(): ByteArray = byteArrayOf(header, command.value, data0, data1, checksum)

    fun getData() = (((data0.toShort() and 0xFF) + data1.toShort().shl(8))).toShort()
    fun setData(data: Int) {
        data0 = (data and 0xFF).toByte()
        data1 = (data shr 8).toByte()
    }

    fun checkChecksum(): Boolean = checksum == (header+command.value+data0+data1).toByte()
}