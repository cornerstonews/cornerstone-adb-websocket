package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.github.cornerstonews.adb.FileNode;

@XmlType(name = "") // To remove type field from JSON when class is extends other class.
public class AdbDirectoryGetMessage extends AdbWebsocketMessage {

    @XmlElement
    private String path;

    @XmlElement
    private FileNode pathDetail;

    public AdbDirectoryGetMessage() {
        this(null);
    }

    public AdbDirectoryGetMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.DIRECTORY_GET, deviceSerial);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileNode getPathDetail() {
        return pathDetail;
    }

    public void setPathDetail(FileNode pathDetail) {
        this.pathDetail = pathDetail;
    }

}
