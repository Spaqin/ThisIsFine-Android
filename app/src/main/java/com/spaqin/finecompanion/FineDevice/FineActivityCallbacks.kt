package com.spaqin.finecompanion.FineDevice

import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Sensors.SensorType

/**
 * Created by Spaqin on 2017-11-04.
 */
interface FineActivityCallbacks {
    fun notifyCapabilities()
    fun notifySensorChange(s : SensorType)
    fun onFineCommunicationError(fm: FineMessage)
}