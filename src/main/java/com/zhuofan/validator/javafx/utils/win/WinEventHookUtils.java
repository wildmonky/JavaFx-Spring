package com.zhuofan.validator.javafx.utils.win;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinUser.MSG;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WinEventHookUtils {

    private static Thread hookThread;
    private static volatile int hookThreadId;
    private static volatile boolean running = false;
    @Getter
    private static HANDLE hook;
    private static WinDef.HWND hwnd;

    public static void start(int pid, WinDef.HWND hwnd, User32Ext.WinEventProc callback) {
        if (callback == null || running) return;
        running = true;
        hookThread = new Thread(() -> WinEventHookUtils.runMessageLoop(pid, hwnd, callback), "WinEventHook-Thread");
        hookThread.setDaemon(true);
        hookThread.setUncaughtExceptionHandler((t,e) -> {
            clearAllHook();
        });

        hookThread.start();
    }

    private static void runMessageLoop(int pid, WinDef.HWND hwnd, User32Ext.WinEventProc callback) {
        try {
            // 1. 初始化 COM（此线程将用于注册钩子）
            Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED);

            hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId();
            WinEventHookUtils.hwnd = hwnd;
            HANDLE handle = hookProcessEvents(pid, callback);
            if (handle == null) {
                log.info("SetWinEventHook 失败, 错误码: {}", com.sun.jna.Native.getLastError());
            }

            // 2. 运行消息循环（这是事件得以接收的关键）
            MSG msg = new MSG();
            while (running && User32.INSTANCE.GetMessage(msg, hwnd, 0, 0) > 0) {
                User32.INSTANCE.TranslateMessage(msg);
                User32.INSTANCE.DispatchMessage(msg);
            }
        } finally {
            // 3. 清理
            clearAllHook();
            Ole32.INSTANCE.CoUninitialize();
            hookThreadId = 0;
            hookThread = null;
            log.info("Win Event Listener closed");
        }
    }

    public static void stop() {
        running = false;
        if (hookThread == null) {
            return;
        }

        User32.INSTANCE.PostMessage(
                hwnd, // 这里需要 hookThread 的 ID，需调整
                User32.WM_QUIT, // WM_QUIT
                new WinDef.WPARAM(0),
                new WinDef.LPARAM(0)
        );
    }

    public static boolean isKeyboardProcess(WinDef.HWND hwnd) {
        if (hwnd == null) return false;
        IntByReference pidRef = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef);
        if (pidRef.getValue() == 0) return false;

        // 打开进程并获取进程名
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(0x1000, false, pidRef.getValue());
        if (processHandle == null) return false;

        try {
            char[] exeName = new char[1024];
            IntByReference size = new IntByReference(exeName.length);
            if (Kernel32.INSTANCE.QueryFullProcessImageName(processHandle, 0, exeName, size)) {
                String name = com.sun.jna.Native.toString(exeName).toLowerCase();
                return name.endsWith("textinputhost.exe") || name.endsWith("tabtip.exe");
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(processHandle);
        }
        return false;
    }

    /**
     * 根据进程名获取 PID 列表
     */
    public static List<Integer> getProcessIdsByName(String processName) {
        List<Integer> pids = new ArrayList<>();
        // 使用 Toolhelp32Snapshot API 遍历进程，这里为简化，可使用 ProcessHandle (Java 9+)
        ProcessHandle.allProcesses()
                .filter(ph -> ph.info().command().orElse("").toLowerCase().endsWith(processName.toLowerCase()))
                .forEach(ph -> pids.add((int) ph.pid()));
        return pids;
    }

    /**
     * 为指定 PID 的进程注册事件钩子
     */
    public static WinNT.HANDLE hookProcessEvents(int processId, User32Ext.WinEventProc callback) {
        HANDLE handle = User32Ext.INSTANCE.SetWinEventHook(
                User32Ext.EVENT_OBJECT_SHOW,
                User32Ext.EVENT_OBJECT_UNCLOAKED,
                null, // hmodWinEventProc
                callback,
                processId, // <-- 关键：指定目标进程 ID
                0,         // idThread: 0 表示该进程内的所有线程
                User32Ext.WINEVENT_OUTOFCONTEXT
        );
        hook = handle;
        return handle;
    }

    public static boolean unhookProcessEvents(WinNT.HANDLE handle) {
        return User32Ext.INSTANCE.UnhookWinEvent(handle);
    }

    /**
     * 程序关闭时调用
     */
    public static void clearAllHook() {
        unhookProcessEvents(hook);
    }

    public static boolean isHooked(WinNT.HANDLE handle) {
        if (hwnd == null) {
            return false;
        }

        return isHwndEqual(handle, hwnd);
    }

    public static boolean isHwndEqual(PointerType t1, PointerType t2) {
        if (ObjectUtils.anyNull(t1, t2)) {
            return t1 == t2;
        }

        long pNativeVal1 = Pointer.nativeValue(t1.getPointer());
        long pNativeVal2 = Pointer.nativeValue(t2.getPointer());

        return pNativeVal1 == pNativeVal2;
    }

    // SM_CONVERTIBLESLATEMODE 的系统指标常量值
    private static final int SM_CONVERTIBLESLATEMODE = 0x2003;

    /**
     * 判断当前Windows是否处于平板模式
     * @return true 表示处于平板模式，false 表示不是
     */
    public static boolean isInTabletMode() {
        // GetSystemMetrics 返回0表示平板模式，非零表示非平板模式
        int result = User32.INSTANCE.GetSystemMetrics(SM_CONVERTIBLESLATEMODE);
        return result == 0;
    }

}