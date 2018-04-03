package com.spaqin.fineapp.Sensors

import com.spaqin.fineapp.Communications.FineMessageCommand
import com.spaqin.fineapp.shl
import com.spaqin.fineapp.shr
import kotlin.experimental.and

/**
 * Created by mateusz on 18.09.17.
 */
enum class SensorType(val protoValue: Byte, val unit: String = "") {
    TEMPERATURE(0x00, "°C"),
    HUMIDITY(0x01, "%"),
    PM25(0x02, "μg/m³"),
    PM10(0x03, "μg/m³"),
    MQ7_CO(0x04, "mV"),
    UNKNOWN(0x0F);
    companion object {
        fun fromByte(b: Byte) = when(b.toInt()) {
            0x00 -> TEMPERATURE
            0x01 -> HUMIDITY
            0x02 -> PM25
            0x03 -> PM10
            0x04 -> MQ7_CO
            else -> UNKNOWN
        }
        fun fromByte(b: Int) = fromByte(b.toByte())
        fun fromCommand(fmc: FineMessageCommand) = fromByte(fmc.value.shr(4))
    }

    fun getCommand() = FineMessageCommand.fromByte(this.protoValue.shl(4) and 0x02)
}

