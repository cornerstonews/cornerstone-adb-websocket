package com.github.cornerstonews.adb.websocket.message;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="")
public class AdbShellCommandMessage extends AdbWebsocketMessage {

    @XmlElement
    private String shellCommand;
    
    @XmlElement
    private String shellCommandOutput;
    
    public AdbShellCommandMessage() {
        this(null);
    }

    public AdbShellCommandMessage(String deviceSerial) {
        super(AdbWebsocketMessageType.SHELL_COMMAND, deviceSerial);
    }

    public String getShellCommand() {
        return shellCommand;
    }

    public void setShellCommand(String shellCommand) {
        this.shellCommand = shellCommand;
    }

    public String getShellCommandOutput() {
        return shellCommandOutput;
    }

    public void setShellCommandOutput(String shellCommandOutput) {
        this.shellCommandOutput = shellCommandOutput;
    }
    
}
