package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "") // To remove type field from JSON when class is extends other class.
public class AdbRebootMessage extends AdbWebsocketMessage {

    public AdbRebootMessage() {
        this(null);
    }

    public AdbRebootMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.DIRECTORY_GET, deviceSerial);
    }

}
