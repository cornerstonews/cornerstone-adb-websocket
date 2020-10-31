package com.github.cornerstonews.adb.websocket;

import java.io.IOException;
//import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.github.cornerstonews.adb.AdbExecutor;
import com.github.cornerstonews.adb.AdbManager;
import com.github.cornerstonews.adb.FileNode;
import com.github.cornerstonews.adb.websocket.message.AdbDirectoryGetMessage;
import com.github.cornerstonews.adb.websocket.message.AdbFilePullMessage;
import com.github.cornerstonews.adb.websocket.message.AdbFilePushMessage;
import com.github.cornerstonews.adb.websocket.message.AdbRebootMessage;
import com.github.cornerstonews.adb.websocket.message.AdbShellCommandMessage;
import com.github.cornerstonews.adb.websocket.message.AdbStatusMessage;
import com.github.cornerstonews.adb.websocket.message.AdbWebsocketMessage;
import com.github.cornerstonews.adb.websocket.message.AdbWebsocketMessageType;
import com.github.cornerstonews.util.JAXBUtils;

//@ServerEndpoint(value = "/adb")
public abstract class AdbWebsocket {

    private static final Logger LOG = LogManager.getLogger(AdbWebsocket.class);

    protected AdbManager adbManager;
    protected AdbExecutor deviceAdbExecutor;

    private AdbFileTransferProcessor filePullProcessor;
    private AdbFileTransferProcessor filePushProcessor;

    public AdbWebsocket(AdbManager adbManager) {
        this.adbManager = adbManager;
    }

    /**
     * This method is invoked once new connection is established. After socket has been opened, it allows us to intercept
     * the creation of a new session. The session class allows us to send data to the user. In the method onOpen, we'll let
     * the client know that the handshake was successful.
     *
     * @param session
     * @param EndpointConfig config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        LOG.info("Connection opened for client: " + session.getId());
    }

    /**
     * This method is invoked when the client closes a WebSocket connection.
     *
     * @param session
     * @return
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.info("Connection closed for device {}, Reason: {}", getDeviceSerial(), reason);
    }

    /**
     * This method is invoked when an error is detected on the connection.
     *
     * @param session
     * @param t
     * @return
     */
    @OnError
    public void onError(Session session, Throwable t) {
        LOG.error("Connection Error with device: {}.", getDeviceSerial(), t);
    }

    /**
     * This method is invoked each time that the server receives a text WebSocket message.
     *
     * @param session
     * @param message
     * @return
     * @throws IOException
     */
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
            if (AdbWebsocketMessageType.AUTH == adbMessage.getMessageType() || this.deviceAdbExecutor == null) {
                authenticate(adbMessage, session);
                return;
            }

            switch (adbMessage.getMessageType()) {

                case DIRECTORY_GET:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbDirectoryGetMessage.class);
                    handleDirectoryGet((AdbDirectoryGetMessage) adbMessage, session);
                    break;

                case FILE_PUSH:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbFilePushMessage.class);
                    handleFilePush((AdbFilePushMessage) adbMessage, session);
                    break;

                case FILE_PULL:
                    adbMessage = JAXBUtils.unmarshalFromJSON(message, AdbFilePullMessage.class);
                    handleFilePull((AdbFilePullMessage) adbMessage, session);
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

    /**
     * This method is invoked each time that the server receives a binary WebSocket message.
     *
     * @param session
     * @param message
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    @OnMessage
    public void onMessage(Session session, ByteBuffer message, boolean isLast) throws IOException, JAXBException {
        LOG.debug("Binary Message received.");
        if (this.filePushProcessor == null) {
            LOG.info("Unsolicited file transfer. Sending rejection to client.");
            sendError("Unsolicited file transfer. Please initiate file transfer request.", new AdbFilePushMessage(getDeviceSerial()),
                    session);
            return;
        }

        try {
            this.filePushProcessor.processFilePush(message, isLast);
//            if (isLast && this.filePushProcessor.isFileValid()) {
            if (this.filePushProcessor.isFileValid()) {
                this.filePushProcessor.pushFileToPhone(this.deviceAdbExecutor);
                this.filePushProcessor.cleanup();
                this.sendSuccess(200, "File push successful.", new AdbFilePushMessage(getDeviceSerial()), session);
                this.filePushProcessor = null;
            }
        } catch (Exception e) {
            LOG.error("Error while transfering binary message: ", e.getMessage(), e);
            sendError("File Transfer error.", new AdbFilePushMessage(getDeviceSerial()), session);
            this.filePushProcessor.cleanup();
            this.filePushProcessor = null;
        }
    }
    
    protected String getDeviceSerial() {
        return (this.deviceAdbExecutor == null) ? "Device serial not available" : this.deviceAdbExecutor.getDeviceSerial();
    }

    protected void sendError(String errorString, AdbWebsocketMessage errorMessage, Session session) {
        try {
            if (errorMessage == null) {
                errorMessage = new AdbWebsocketMessage(null, getDeviceSerial());
            }
            errorMessage.setStatusCode(400);
            errorMessage.setMessage(errorString);
            session.getBasicRemote().sendText(JAXBUtils.marshalToJSON(errorMessage));
            LOG.info("Sent error to client for device: {}, Error: {}", getDeviceSerial(), errorString);
            LOG.trace("ErrorMessage: {}", getDeviceSerial(), errorMessage);
        } catch (IOException | JAXBException e) {
            LOG.error("Error sending message to client: {}, Error: {}", getDeviceSerial(), e.getMessage(), e);
        }
    }

    protected void sendSuccess(Integer statusCode, String message, AdbWebsocketMessage adbMessage, Session session) throws IOException, JAXBException {
        adbMessage.setStatusCode(statusCode);
        adbMessage.setMessage(message);
        session.getBasicRemote().sendText(JAXBUtils.marshalToJSON(adbMessage));
        LOG.debug("Success message sent to client: {}", getDeviceSerial());
        LOG.trace("Success message sent to client: {}, Message: {}", getDeviceSerial(), adbMessage);
    }

    private void authenticate(AdbWebsocketMessage adbMessage, Session session) throws IOException, JAXBException {
        if (this.deviceAdbExecutor != null) {
            sendError("Authentication error. Already authenticated.", adbMessage, session);
            return;
        }

        if (AdbWebsocketMessageType.AUTH != adbMessage.getMessageType()) {
            sendError("Must authenticate to call ADB commands.", adbMessage, session);
            return;
        }

        this.deviceAdbExecutor = this.adbManager.getDevice(adbMessage.getDeviceSerial());
        if (deviceAdbExecutor == null) {
            sendError("Authentication Failure. Device not found.", adbMessage, session);
        }

        this.sendSuccess(200, "Auth success.", adbMessage, session);
    }

    protected void handleDirectoryGet(AdbDirectoryGetMessage adbMessage, Session session) throws IOException, JAXBException {
        FileNode pathDetail = this.deviceAdbExecutor.getPath(adbMessage.getPath(), adbMessage.getGetChildren());
        adbMessage.setPathDetail(pathDetail);
        this.sendSuccess(200, "Directory get successful.", adbMessage, session);
    }

    private void handleFilePush(AdbFilePushMessage adbMessage, Session session) throws IOException, JAXBException {
        if (this.filePushProcessor != null) {
            this.sendError("Only one file transfer allowed.", adbMessage, session);
            return;
        }

        try {
            this.filePushProcessor = new AdbFileTransferProcessor(adbMessage, session);
            this.sendSuccess(201, "Ready for push. Waiting for data.", adbMessage, session);
        } catch (Exception e) {
            this.filePushProcessor = null;
            LOG.error("File transfer Error for client: {}, Error: {}", getDeviceSerial(), e.getMessage(), e);
            this.sendError("File transfer Error.", adbMessage, session);
        }
    }

    private void handleFilePull(AdbFilePullMessage adbMessage, Session session) throws IOException, JAXBException {
        if (this.filePullProcessor != null) {
            this.sendError("Only one file transfer allowed.", adbMessage, session);
            return;
        }

        try {
            this.filePullProcessor = new AdbFileTransferProcessor(adbMessage, session);
            this.sendSuccess(201, "Ready to pull. Starting data transfer.", adbMessage, session);
//            OutputStream outputStream = session.getBasicRemote().getSendStream();
//            this.filePullProcessor.processFilePull(deviceAdbExecutor, outputStream);
            this.filePullProcessor.processFilePull(deviceAdbExecutor, session.getBasicRemote());
            this.sendSuccess(200, "File pull successful.", adbMessage, session);
        } catch (Exception e) {
            LOG.error("File transfer Error for client: {}, Error: {}", getDeviceSerial(), e.getMessage(), e);
            this.sendError("File transfer Error.", adbMessage, session);
        } finally {
            this.filePullProcessor = null;
        }
    }

    protected void handleReboot(AdbRebootMessage adbMessage, Session session) throws JAXBException {
        try {
            this.deviceAdbExecutor.reboot();
            this.sendSuccess(200, "Phone reboot command successfully executed.", adbMessage, session);
        } catch (TimeoutException | AdbCommandRejectedException | IOException e) {
            LOG.error("Error running reboot command on phone {}, Error: {}", adbMessage.getDeviceSerial(),  e);
            this.sendError("Error running reboot command.", adbMessage, session);
        }
    }

    protected void handleShellCommand(AdbShellCommandMessage adbMessage, Session session) throws JAXBException {
        try {
            String commandOutput = this.deviceAdbExecutor.executeShellCommand(adbMessage.getShellCommand());
            adbMessage.setShellCommandOutput(commandOutput);
            this.sendSuccess(200, "Command successfully executed.", adbMessage, session);
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            LOG.error("Error running command on phone {} for client: {}, Error: {}", adbMessage.getDeviceSerial(), getDeviceSerial(), e);
            this.sendError("Error running command.", adbMessage, session);
        }
    }

    protected void handleStatus(AdbStatusMessage adbMessage, Session session) throws IOException, JAXBException {
        String status = this.deviceAdbExecutor.isOnline() ? "online" : "offline";
        adbMessage.setStatus(status);
        this.sendSuccess(200, "Status successfully executed.", adbMessage, session);
    }

}
