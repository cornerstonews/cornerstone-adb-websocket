package com.github.cornerstonews.adb.websocket.message;

public enum AdbWebsocketMessageType {

	DEVICES_GET,
	
    AUTH,
    DIRECTORY_GET,
    FILE_PUSH,
    FILE_PULL,
    REBOOT,
    SHELL_COMMAND,
    STATUS,

}
