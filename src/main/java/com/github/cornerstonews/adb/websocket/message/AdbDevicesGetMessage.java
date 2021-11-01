package com.github.cornerstonews.adb.websocket.message;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.github.cornerstonews.adb.DeviceDO;

@XmlType(name = "") // To remove type field from JSON when class is extends other class.
public class AdbDevicesGetMessage extends AdbWebsocketMessage {

    @XmlElement
    private List<DeviceDO> devices;

    public AdbDevicesGetMessage() {
        this(null);
    }

    public AdbDevicesGetMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.DEVICES_GET, deviceSerial);
    }

    public List<DeviceDO> getDevices() {
        return devices;
    }

    public void setDevices(List<DeviceDO> devices) {
        this.devices = devices;
    }
}
