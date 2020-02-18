package com.github.cornerstonews.adb.websocket.message;

public class AdbFilePullMessage extends AdbFileMessage {

    public AdbFilePullMessage() {
        this(null);
    }

    public AdbFilePullMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.FILE_PULL, deviceSerial);
    }

}
