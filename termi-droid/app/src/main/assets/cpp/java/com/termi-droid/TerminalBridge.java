package com.termi-droid;

import android.os.ParcelFileDescriptor;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TerminalBridge {
    static {
        // Loads the native compiled termidroid.cpp library binary
        System.loadLibrary("termidroid");
    }

    private final int masterFd;
    private final int pid;
    private final FileInputStream inputStream;
    private final FileOutputStream outputStream;

    /**
     * Initializes a new backend Terminal Session.
     * Fires up a system fork worker pointing directly to /system/bin/sh.
     */
    public TerminalBridge() {
        int[] processIdArray = new int[1];
        // Executes native process engine creation sequence
        this.masterFd = createSubProcess("/system/bin/sh", null, null, processIdArray);
        this.pid = processIdArray[0];

        // Maps the Linux standard system I/O streams into Java file system wrappers
        FileDescriptor fd = createJavaDescriptor(masterFd);
        this.inputStream = new FileInputStream(fd);
        this.outputStream = new FileOutputStream(fd);
    }

    /**
     * Transmits raw user keyboard input entries to the terminal master stream.
     */
    public void write(String data) {
        try {
            outputStream.write(data.getBytes());
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    /**
     * Spawns an execution loop thread to pass shell streams to the rendering layout view.
     */
    public void startReading(final TerminalView view) {
        new Thread(() -> {
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    final String text = new String(buffer, 0, bytesRead);
                    view.post(() -> view.appendOutput(text));
                }
            } catch (IOException ignored) {}
        }).start();
    }

    /**
     * Signals window changes to the underlying native terminal execution framework.
     */
    public void resizeTerminal(int rows, int cols, int widthPx, int heightPx) {
        if (masterFd > 0) {
            setPtyWindowSize(masterFd, rows, cols, widthPx, heightPx);
        }
    }

    /**
     * Wraps raw Linux file integers cleanly into standard Android Java FileDescriptors.
     */
    private static FileDescriptor createJavaDescriptor(int nativeFd) {
        try {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.fromFd(nativeFd);
            return pfd.getFileDescriptor();
        } catch (Exception e) {
            throw new RuntimeException("Could not map native file descriptor", e);
        }
    }

    // --- Native JNI Method Definitions ---
    
    private native int createSubProcess(String cmd, String[] args, String[] envp, int[] processId);
    
    public native void setPtyWindowSize(int fd, int rows, int cols, int widthPx, int heightPx);
              }
      
