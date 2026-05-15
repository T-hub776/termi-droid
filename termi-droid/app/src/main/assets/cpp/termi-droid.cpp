#include <sys/ioctl.h>

extern "C" JNIEXPORT void JNICALL
Java_com_thub776_termidroid_TerminalBridge_setPtyWindowSize(
        JNIEnv* env, jobject thiz, jint fd, jint rows, jint cols, jint widthPx, jint heightPx) {
    
    struct winsize sz;
    sz.ws_row = (unsigned short)rows;
    sz.ws_col = (unsigned short)cols;
    sz.ws_xpixel = (unsigned short)widthPx;
    sz.ws_ypixel = (unsigned short)heightPx;
    
    // Inject TIOCSWINSZ signal directly into the Linux PTY master file descriptor
    ioctl((int)fd, TIOCSWINSZ, &sz);
}

