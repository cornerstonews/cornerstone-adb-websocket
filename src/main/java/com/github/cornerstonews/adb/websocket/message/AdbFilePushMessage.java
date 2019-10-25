package com.github.cornerstonews.adb.websocket.message;

public class AdbFilePushMessage extends AdbFileMessage {

    public AdbFilePushMessage() {
        this(null);
    }

    public AdbFilePushMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.FILE_PUSH, deviceSerial);
    }

}
