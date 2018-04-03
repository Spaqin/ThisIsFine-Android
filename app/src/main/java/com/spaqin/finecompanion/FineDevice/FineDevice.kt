package com.spaqin.finecompanion.FineDevice

import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Communications.FineMessageCommand
import com.spaqin.fineapp.Sensors.RawSensorData
import com.spaqin.fineapp.Sensors.SensorType
import com.spaqin.fineapp.shl
import java.util.*

/**
 * Created by Spaqin on 2017-10-31.
 */
object FineDevice
{
    var connected = false
    var waitingForReply = false
    var lastMessageCommand = FineMessageCommand.UNKNOWN
    var buildMonth = 0
    var buildDay = 0
    var supportedProtocols: MutableMap<SensorType, Boolean> = mutableMapOf()
    var outMessageQueue = ArrayDeque<FineMessage>()
    val sensorData = MutableList<RawSensorData>(16) { RawSensorData(0, SensorType.fromByte(it)) }

    fun setSupported(fm: FineMessage)
    {
        var bitmap = fm.data0 + fm.data1.shl(8)
        var it = 1 shl 15
        for(i in 0..15)
        {
            supportedProtocols[SensorType.fromByte(i)] = (it and bitmap) != 0
            it = it shr 1
        }
    }

    fun setBuildDate(fm: FineMessage)
    {
        buildMonth = fm.data0.toInt()
        buildDay = fm.data1.toInt()
    }

}