package com.spaqin.fineapp.Communications

import com.spaqin.fineapp.Sensors.SensorType
import com.spaqin.fineapp.shl
import com.spaqin.fineapp.shr
import com.spaqin.fineapp.ushr
import kotlin.experimental.and

/**
 * Created by mateusz on 18.09.17.
 */

enum class FineMessageCommand(val value: Byte, val waitForReply: Boolean = false, val isDataQuery: Boolean = value.shr(4) == 0x02.toByte()) {
    CAPABILITIES(0x01, true),
    DATA_QUERY(0x02, true),
    BUILD_DATE(0x04, true),
    CONTINUOUS(0x05, false),
    DIAGNOSTICS(0xF1.toByte(), true),

    TEMPERATURE_DATA_QUERY(0x02, true, true),
    HUMIDITY_DATA_QUERY(0x12, true, true),
    PM25_DATA_QUERY(0x22, true, true),
    PM10_DATA_QUERY(0x32, true, true),
    MQ7_CO_DATA_QUERY(0x42, true, true),
    UNKNOWN(0xFF.toByte());

    //val isDataQuery = value.shr(4) == 0x02.toByte()
    val sensorByte = if (isDataQuery) value.shr(4).toInt() else 0x0F

    companion object {
        fun fromByte(b: Byte) = when(b.toInt()) {
            0x01 -> CAPABILITIES
            0x02 -> TEMPERATURE_DATA_QUERY
            0x04 -> BUILD_DATE
            0x05 -> CONTINUOUS
            0xF1 -> DIAGNOSTICS
            0x12 -> HUMIDITY_DATA_QUERY
            0x22 -> PM25_DATA_QUERY
            0x32 -> PM10_DATA_QUERY
            0x42 -> MQ7_CO_DATA_QUERY
            else -> UNKNOWN
        }
    }

    fun getValue(sensor: SensorType): Byte {
        if (this != DATA_QUERY)
            return this.value
        else
            return this.value and sensor.protoValue.shl(4)
    }

    fun getSensor(): SensorType {
        if (this.value and 0x0F == 0x02.toByte())
            return SensorType.fromByte(this.value.ushr(4))
        return SensorType.UNKNOWN
    }

}