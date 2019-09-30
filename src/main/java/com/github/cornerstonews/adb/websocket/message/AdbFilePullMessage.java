package com.github.cornerstonews.adb.websocket.message;

public class AdbFilePullMessage extends AdbFileMessage {

    public AdbFilePullMessage() {}

    public AdbFilePullMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.FILE_PUSH, deviceSerial);
    }

}
