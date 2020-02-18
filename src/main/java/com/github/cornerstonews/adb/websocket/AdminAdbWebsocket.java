package com.github.cornerstonews.adb.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.cornerstonews.adb.AdbExecutor;
import com.github.cornerstonews.adb.AdbManager;
import com.github.cornerstonews.adb.websocket.message.AdbDevicesGetMessage;
import com.github.cornerstonews.adb.websocket.message.AdbDevicesGetMessage.DeviceStatus;
import com.github.cornerstonews.adb.websocket.message.AdbDirectoryGetMessage;
import com.github.cornerstonews.adb.websocket.message.AdbRebootMessage;
import com.github.cornerstonews.adb.websocket.message.AdbShellCommandMessage;
import com.github.cornerstonews.adb.websocket.message.AdbStatusMessage;
import com.github.cornerstonews.adb.websocket.message.AdbWebsocketMessage;
import com.github.cornerstonews.adb.websocket.message.AdbWebsocketMessageType;
import com.github.cornerstonews.util.JAXBUtils;

//@ServerEndpoint(value = "/admin/adb")
public abstract class AdminAdbWebsocket extends AdbWebsocket {

    private static final Logger LOG = LogManager.getLogger(AdminAdbWebsocket.class);

    private Map<String, AdbExecutor> devices;
    
    public AdminAdbWebsocket(AdbManager adbManager) {
        super(adbManager);
        this.devices = this.adbManager.getFilteredDevices(null);
    }

    
    @OnMessage
    public void onMessage(Session session, String message, boolean isLast) {
        LOG.info("Received a string message.");
        LOG.debug("Message received: {}", message);
        
        AdbWebsocketMessage adbMessage;
        try {
            adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbWebsocketMessage.class);

            if (adbMessage == null || adbMessage.getMessageType() == null) {
                LOG.info("Invalid message received, rejecting and sending error client. Message is not Adb Websocket Message Type. ");
                sendError("Invalid message. Please make sure message is formatted properly and includes a messageType.", adbMessage, session);
                return;
            }
        } catch (JAXBException e) {
            LOG.info("Error unmarshalling message: '{}', Error: '{}'", message, e.getMessage(), e);
            sendError("Invalid message. Please make sure message is formatted properly and includes a messageType.", null, session);
            return;
        }

        try {
        	if (AdbWebsocketMessageType.DEVICES_GET == adbMessage.getMessageType()) {
        		adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbDevicesGetMessage.class);
        		handleDevicesGet((AdbDevicesGetMessage) adbMessage, session);
                return;
            }
        	
        	this.deviceAdbExecutor = getDeviceAdbExecutor(adbMessage.getDeviceSerial());
            if (deviceAdbExecutor == null) {
                sendError("Authentication Failure. Device not found.", adbMessage, session);
            }


            switch (adbMessage.getMessageType()) {

                case DIRECTORY_GET:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbDirectoryGetMessage.class);
                    handleDirectoryGet((AdbDirectoryGetMessage) adbMessage, session);
                    break;

                case REBOOT:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbRebootMessage.class);
                    handleReboot((AdbRebootMessage) adbMessage, session);
                    break;

                case SHELL_COMMAND:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbShellCommandMessage.class);
                    handleShellCommand((AdbShellCommandMessage) adbMessage, session);
                    break;

                case STATUS:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbStatusMessage.class);
                    handleStatus((AdbStatusMessage) adbMessage, session);
                    break;

                default:
                    sendError("Unknown ADB command.", adbMessage, session);
                    return;
            }
        } catch (JAXBException | IOException e) {
            LOG.error("Error processing message for device serial '{}', Error: {}", adbMessage.getDeviceSerial(), e.getMessage(), e);
            sendError("Error processing message. Error: " + e.getMessage(), adbMessage, session);
            return;
        }
    }
    
    private void handleDevicesGet(AdbDevicesGetMessage adbMessage, Session session) throws IOException, JAXBException {
    	List<DeviceStatus> devicesWithStatus = new ArrayList<DeviceStatus>();
    	this.adbManager.getFilteredDevices(null).entrySet().parallelStream()
    		.forEach((entry) -> devicesWithStatus.add(new DeviceStatus(entry.getKey(), entry.getValue().isOnline() ? "online" : "offline")));   	
    	adbMessage.setDevices(devicesWithStatus);
        this.sendSuccess(200, "Devices get successful.", adbMessage, session);
    }
    
    private AdbExecutor getDeviceAdbExecutor(String deviceSerial) {   	
    	this.deviceAdbExecutor = this.devices.get(deviceSerial);
    	
    	if(this.deviceAdbExecutor == null) {
        	this.devices = this.adbManager.getFilteredDevices(null);
        	this.deviceAdbExecutor = this.devices.get(deviceSerial);
    	}
    	
    	return this.deviceAdbExecutor;
    }

}
