#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/ioctl.h>
#include <sys/types.h>

// Matches Java package 'com.termi_droid' and class 'TerminalBridge'
extern "C" JNIEXPORT jint JNICALL
Java_com_termi_1droid_TerminalBridge_createSubProcess(
        JNIEnv* env, jobject thiz, jstring cmd, jobjectArray args, jobjectArray envp, jintArray processId) {
    
    int masterFd = posix_openpt(O_RDWR | O_CLOEXEC);
    if (masterFd < 0 || grantpt(masterFd) < 0 || unlockpt(masterFd) < 0) return -1;

    pid_t pid = fork();
    if (pid < 0) return -1;

    if (pid == 0) { // Child execution fork
        int slaveFd = open(ptsname(masterFd), O_RDWR);
        
        dup2(slaveFd, 0); // stdin
        dup2(slaveFd, 1); // stdout
        dup2(slaveFd, 2); // stderr

        if (slaveFd > 2) close(slaveFd);

        setsid();
        ioctl(0, TIOCSCTTY, 1);

        char* execArgs[] = {(char*)"/system/bin/sh", (char*)"-l", NULL};
        execve("/system/bin/sh", execArgs, NULL);
        _exit(1);
    } else { // Parent context
        jint* pids = env->GetIntArrayElements(processId, NULL);
        pids[0] = pid;
        env->ReleaseIntArrayElements(processId, pids, 0);
        return masterFd;
    }
}

// Matches Java package 'com.termi_droid' and class 'TerminalBridge'
extern "C" JNIEXPORT void JNICALL
Java_com_termi_1droid_TerminalBridge_setPtyWindowSize(
        JNIEnv* env, jobject thiz, jint fd, jint rows, jint cols, jint widthPx, jint heightPx) {
    
    struct winsize sz;
    sz.ws_row = (unsigned short)rows;
    sz.ws_col = (unsigned short)cols;
    sz.ws_xpixel = (unsigned short)widthPx;
    sz.ws_ypixel = (unsigned short)heightPx;
    
    ioctl((int)fd, TIOCSWINSZ, &sz);
}
