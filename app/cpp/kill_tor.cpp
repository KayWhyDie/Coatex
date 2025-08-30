#include <jni.h>
#include <cstring>
#include <sys/types.h>
#include <dirent.h>
#include <csignal>
#include <cstdlib>
#include <unistd.h>
#include <cstdio>
#include <sys/stat.h>
#include <fcntl.h>
#include <android/log.h>

// Helper to check if a string ends with a suffix
bool endswith(const char *str, const char *suffix) {
    if (!str || !suffix) return false;
    size_t lenstr = strlen(str);
    size_t lensuffix = strlen(suffix);
    if (lensuffix > lenstr) return false;
    return strncmp(str + lenstr - lensuffix, suffix, lensuffix) == 0;
}

// Native implementation of killTor
void killTor_native(JNIEnv *env, jclass clazz) {
    __android_log_print(ANDROID_LOG_INFO, "killTor_native", "Executing killTor_native");
    DIR *d = opendir("/proc");
    if (!d) {
        __android_log_print(ANDROID_LOG_ERROR, "PROCESS", "Failed to open /proc");
        return;
    }

    dirent *de;
    while ((de = readdir(d)) != nullptr) {
        // Check if the directory name is a number (a PID)
        bool is_pid = true;
        for (char *p = de->d_name; *p; p++) {
            if (*p < '0' || *p > '9') {
                is_pid = false;
                break;
            }
        }
        if (!is_pid) continue;

        pid_t pid = atol(de->d_name);
        if (pid <= 0) continue;

        char cmdline_path[1024];
        snprintf(cmdline_path, sizeof(cmdline_path), "/proc/%d/cmdline", pid);

        char cmdline[1024] = {0};
        int fd = open(cmdline_path, O_RDONLY);
        if (fd >= 0) {
            read(fd, cmdline, sizeof(cmdline) - 1);
            close(fd);
        }

        // Check if the command line ends with tor, ftor, or ctor
        if (strlen(cmdline) > 0 && (endswith(cmdline, "/tor") || endswith(cmdline, "/ftor") || endswith(cmdline, "/ctor"))) {
            __android_log_print(ANDROID_LOG_INFO, "PROCESS", "Found Tor process %d: %s", pid, cmdline);
            if (kill(pid, SIGKILL) == 0) {
                __android_log_print(ANDROID_LOG_INFO, "PROCESS", "Killed process %d", pid);
            } else {
                __android_log_print(ANDROID_LOG_WARN, "PROCESS", "Failed to kill process %d", pid);
            }
        }
    }
    closedir(d);
}

// JNI_OnLoad is called when the library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    __android_log_print(ANDROID_LOG_INFO, "JNI_OnLoad", "JNI_OnLoad called");
    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_OnLoad", "Failed to get JNIEnv");
        return JNI_ERR;
    }

    // Find the Native class
    jclass native_class = env->FindClass("com/ivor/kriptex/tor/Native");
    if (native_class == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_OnLoad", "Failed to find class com/ivor/kriptex/tor/Native");
        return JNI_ERR;
    }
    __android_log_print(ANDROID_LOG_INFO, "JNI_OnLoad", "Found class com/ivor/kriptex/tor/Native");

    // Register the native method
    JNINativeMethod methods[] = {
        {"killTor", "()V", (void*)killTor_native}
    };

    int rc = env->RegisterNatives(native_class, methods, sizeof(methods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "JNI_OnLoad", "Failed to register native methods, error code: %d", rc);
        env->DeleteLocalRef(native_class);
        return JNI_ERR;
    }

    env->DeleteLocalRef(native_class);
    __android_log_print(ANDROID_LOG_INFO, "JNI_OnLoad", "Successfully registered native methods");

    return JNI_VERSION_1_6;
}