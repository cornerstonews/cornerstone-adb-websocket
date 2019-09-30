package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlElement;

public class AdbWebsocketMessage {

    @XmlElement
    private AdbWebsocketMessageType messageType;

    @XmlElement
    private Integer statusCode;

    @XmlElement
    private String message;

    @XmlElement
    private String deviceSerial;

    public AdbWebsocketMessage() {
    }

    public AdbWebsocketMessage(AdbWebsocketMessageType type, String deviceSerial) {
        this.messageType = type;
        this.deviceSerial = deviceSerial;
    }

    public AdbWebsocketMessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(AdbWebsocketMessageType messageType) {
        this.messageType = messageType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

}
