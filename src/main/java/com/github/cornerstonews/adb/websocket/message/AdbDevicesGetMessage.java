package com.github.cornerstonews.adb.websocket.message;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "") // To remove type field from JSON when class is extends other class.
public class AdbDevicesGetMessage extends AdbWebsocketMessage {

    @XmlElement
    private List<DeviceStatus> devices;

    public AdbDevicesGetMessage() {
        this(null);
    }

    public AdbDevicesGetMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.DEVICES_GET, deviceSerial);
    }

    public List<DeviceStatus> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceStatus> devices) {
		this.devices = devices;
	}

	public static class DeviceStatus {
        @XmlElement
        private String deviceSerial;
        
        @XmlElement
        private String status;
        
        public DeviceStatus() {}

		public DeviceStatus(String deviceSerial, String status) {
			this.deviceSerial = deviceSerial;
			this.status = status;
		}

		public String getDeviceSerial() {
			return deviceSerial;
		}

		public void setDeviceSerial(String deviceSerial) {
			this.deviceSerial = deviceSerial;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
    }
}
