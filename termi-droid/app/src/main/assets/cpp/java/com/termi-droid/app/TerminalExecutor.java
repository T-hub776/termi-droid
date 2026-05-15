package com.termidroid.app;

import java.io.*;
import java.util.Map;

public class TerminalExecutor {
    
    public static Process startTerminalSession(String customCommand) throws IOException {
        // Direct the process thread engine straight into the termi-droid prefix environment
        ProcessBuilder pb = new ProcessBuilder("/system/bin/sh", "-c", customCommand);
        pb.directory(new File("/data/data/com.termidroid.app/files/home"));

        // Setup the isolated PATH variables explicitly for termi-droid targets
        Map<String, String> env = pb.environment();
        env.put("PATH", "/data/data/com.termidroid.app/files/usr/bin:/data/data/com.termidroid.app/files/usr/bin/applets:/system/bin");
        env.put("HOME", "/data/data/com.termidroid.app/files/home");
        env.put("TERM", "xterm-256color");

        return pb.start();
    }
}

