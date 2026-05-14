#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/ioctl.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_termidroid_MainActivity_createSubprocess(JNIEnv *env, jobject thiz, jstring cmd, jobjectArray envp, jintArray processIdArray) {
    // Open the master side of the pseudo-terminal
    int ptmxFd = posix_openpt(O_RDWR | O_CLOEXEC);
    if (ptmxFd < 0) return -1;
    
    if (grantpt(ptmxFd) != 0 || unlockpt(ptmxFd) != 0) {
        close(ptmxFd);
        return -1;
    }
    
    int pid = fork();
    if (pid == 0) { // Inside the child process execution bubble
        int ptsFd = open(ptsname(ptmxFd), O_RDWR);
        close(ptmxFd);
        
        // Redirect standard input, output, and error streams to the terminal channel
        dup2(ptsFd, 0); 
        dup2(ptsFd, 1); 
        dup2(ptsFd, 2);
        
        if (ptsFd > 2) close(ptsFd);
        
        setsid();
        ioctl(0, TIOCSCTTY, 1); // Set as the controlling terminal
        
        // Launch Android's native recovery shell as a fallback baseline
        char* args[] = {(char*)"/system/bin/sh", NULL};
        execve(args[0], args, NULL);
        _exit(1);
    }
    return ptmxFd; // Returns the file descriptor back to Java
}

