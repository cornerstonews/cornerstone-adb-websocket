package com.github.cornerstonews.adb.websocket.message;

public class AdbFilePushMessage extends AdbFileMessage {

    public AdbFilePushMessage() {}

    public AdbFilePushMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.FILE_PUSH, deviceSerial);
    }

}
