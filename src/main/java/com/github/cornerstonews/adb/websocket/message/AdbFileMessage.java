package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlElement;

public abstract class AdbFileMessage extends AdbWebsocketMessage {

    @XmlElement
    private String fullPath;
    
    @XmlElement
    private String fileName;
    
    @XmlElement
    private Integer size;

    public AdbFileMessage() {
    }

    public AdbFileMessage(AdbWebsocketMessageType type, String deviceSerial) {
        super(type, deviceSerial);
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
