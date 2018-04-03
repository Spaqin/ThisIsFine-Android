package com.spaqin.finecompanion.FineDevice

import android.bluetooth.BluetoothDevice
import com.spaqin.fineapp.Communications.FineMessage
import com.spaqin.fineapp.Communications.FineMessageCommand
import com.spaqin.fineapp.Sensors.SensorType
import com.spaqin.finecompanion.Communications.BLeSerialPortService

/**
 * Created by Spaqin on 2017-11-01.
 */
class FineController(var serialPortService: BLeSerialPortService, var fineActivity: FineActivityCallbacks) {

    val askHeader = 0xF2.toByte()
    val replyHeader = 0xF1.toByte()
    val msgCapabilities: FineMessage = FineMessage(askHeader, FineMessageCommand.CAPABILITIES)

    val diagnosticsData0: Byte = 0x11
    val diagnosticsData1: Byte = 0x22

    fun askForCapabilities()
    {
        sendMessage(msgCapabilities)
    }

    fun querySensor(sensor: SensorType)
    {
        val sensorComm = sensor.getCommand()
        val msg = FineMessage(askHeader, sensorComm)
        sendMessage(msg)
    }

    fun setContinuousMode(time: Int)
    {
        val msg = FineMessage(askHeader, FineMessageCommand.CONTINUOUS)
        msg.setData(time)
        sendMessage(msg)
    }

    fun sendNextMessage()
    {
        if (FineDevice.outMessageQueue.isEmpty())
            return
        val fm = FineDevice.outMessageQueue.poll()
        sendMessage(fm)
    }

    fun sendMessage(fm: FineMessage)
    {
        if(!FineDevice.waitingForReply) {
            FineDevice.lastMessageCommand = fm.command
            FineDevice.waitingForReply = fm.command.waitForReply
            serialPortService.send(fm.toByteArray())
        }
        else {
            FineDevice.outMessageQueue.add(fm)
        }
    }

    fun checkMessage(fm: FineMessage): Boolean {
        val header = fm.header == replyHeader
        val command = when(fm.command) {
            FineMessageCommand.BUILD_DATE, FineMessageCommand.CAPABILITIES, FineMessageCommand.DIAGNOSTICS -> fm.command == FineDevice.lastMessageCommand
            else -> true
        }
        val checksum = fm.checkChecksum()
        return header && command && checksum
    }

    fun parseMessage(ba: ByteArray) {
        val fm = FineMessage.fromByteArray(ba)
        if (fm != null)
            parseMessage(fm)
    }

    fun parseMessage(fm: FineMessage)
    {
        FineDevice.waitingForReply = false
        if(!checkMessage(fm)) {
            fineActivity.onFineCommunicationError(fm)
            return
        }
        when(fm.command) {
            FineMessageCommand.BUILD_DATE -> FineDevice.setBuildDate(fm)
            FineMessageCommand.CAPABILITIES -> {FineDevice.setSupported(fm); fineActivity.notifyCapabilities()}
            FineMessageCommand.DIAGNOSTICS -> if (fm.data0 != (diagnosticsData0 + 0xF1).toByte() || fm.data1 != (diagnosticsData1 + 0xF1).toByte()) fineActivity.onFineCommunicationError(fm)
            else -> {
                if(fm.command.isDataQuery) {
                    FineDevice.sensorData[fm.command.sensorByte].data = fm.getData()
                    fineActivity.notifySensorChange(fm.command.getSensor())
                }
                else {
                    fineActivity.onFineCommunicationError(fm)
                }
            }
        }

    }

    fun connect(btDev: BluetoothDevice)
    {
        serialPortService.connect(btDev)
    }
}