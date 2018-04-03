package com.spaqin.finecompanion.Communications

import com.spaqin.fineapp.Sensors.SensorType
import java.util.Calendar

/**
 * Created by Spaqin on 2018-02-05.
 */
data class FineSet(val sensorType: SensorType,
                   val sensorValue: Float,
                   val lat: Double,
                   val lon: Double,
                   val datetime: Calendar? = Calendar.getInstance())