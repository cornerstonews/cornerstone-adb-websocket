package com.github.cornerstonews.adb.websocket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.github.cornerstonews.adb.AdbExecutor;
import com.github.cornerstonews.adb.websocket.message.AdbFileMessage;

public class AdbFileTransferProcessor {

    private static final Logger LOG = LogManager.getLogger(AdbFileTransferProcessor.class);

    private AdbFileMessage adbMessage;
    private String tmpFileName = UUID.randomUUID().toString();

    private FileOutputStream fos = null;
    private File file;
    private FileChannel fc;

    public AdbFileTransferProcessor(AdbFileMessage adbMessage, Session session) throws FileNotFoundException {
        this.adbMessage = adbMessage;
        String tempPath = System.getProperty("java.io.tmpdir");
        this.file = new File(tempPath + File.separator + tmpFileName);
        this.fos = new FileOutputStream(file, true);
        this.fc = fos.getChannel();
    }

    public AdbFileMessage getAdbMessage() {
        return adbMessage;
    }

    public void processFilePush(ByteBuffer message, boolean isLast) throws IOException {
        fc.write(message);
        if (isLast) {
            cleanupStreams();
        }
    }

    public void pushFileToPhone(AdbExecutor adbExecutor) throws IOException, SyncException, AdbCommandRejectedException, TimeoutException {
        LOG.debug("Pushing file to phone: {} -> {}", this.file.getCanonicalPath(), adbMessage.getFullPath());
        adbExecutor.pushFile(this.file.getCanonicalPath(), adbMessage.getFullPath());
    }

    public boolean isFileValid() {
        // Add checksum
        return true;
    }

    public String pullFileFromPhone(AdbExecutor adbExecutor) throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
        LOG.debug("Pulling file from phone: {} -> {}", adbMessage.getFullPath(), this.file.getCanonicalPath());
        adbExecutor.pullFile(adbMessage.getFullPath(), this.file.getCanonicalPath());
        return this.file.getCanonicalPath();
    }

    public void processFilePull(AdbExecutor adbExecutor, OutputStream outputStream) throws FileNotFoundException, IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.file.getCanonicalFile()))) {
            this.pullFileFromPhone(adbExecutor);
            
            int len = 0;
            byte[] buffer = new byte[8192];
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (SyncException | AdbCommandRejectedException | TimeoutException e) {
            throw new IOException(e);
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
            this.cleanup();
        }
    }

    public void cleanup() throws IOException {
        if (this.file != null && this.file.exists()) {
            this.file.delete();
            this.file = null;
        }
        
        cleanupStreams();
    }
    
    private void cleanupStreams() throws IOException {
        if(fc != null && fc.isOpen()) {
            fc.close();            
        }
        
        if(fos != null) {
            fos.flush();
            fos.close();            
        }
    }
}
