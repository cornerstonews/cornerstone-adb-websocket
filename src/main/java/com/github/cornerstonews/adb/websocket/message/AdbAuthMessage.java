package com.github.cornerstonews.adb.websocket.message;

public class AdbAuthMessage extends AdbWebsocketMessage {

    public AdbAuthMessage() {
        this(null);
    }

    public AdbAuthMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.AUTH, deviceSerial);
    }

}
