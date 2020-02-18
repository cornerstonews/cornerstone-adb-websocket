package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "") // To remove type field from JSON when class is extends other class.
public class AdbStatusMessage extends AdbWebsocketMessage {

    private String status;
    
    public AdbStatusMessage() {
        this(null);
    }

    public AdbStatusMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.STATUS, deviceSerial);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
