package com.spaqin.fineapp.Sensors

/**
 * Created by mateusz on 18.09.17.
 */
data class RawSensorData(open var data: Short, open val type: SensorType){
    fun getConvertedData(): Float {
        val intData = data.toInt()
        return when(type) {
            SensorType.PM25, SensorType.PM10 -> intData.toFloat()/10
            SensorType.HUMIDITY, SensorType.TEMPERATURE -> (intData and 0xFF).toFloat() + (((intData and 0xFF00) shr 8 ).toFloat()/100)
            SensorType.MQ7_CO -> intData.toFloat()
            SensorType.UNKNOWN -> 0.0f
        }
    }
    override fun toString() = getConvertedData().toString() + type.unit
}