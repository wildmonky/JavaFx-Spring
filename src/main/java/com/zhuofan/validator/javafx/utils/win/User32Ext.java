package com.zhuofan.validator.javafx.utils.win;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.*;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ext extends StdCallLibrary {
    User32Ext INSTANCE = com.sun.jna.Native.load("user32", User32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

    // 事件常量
    int EVENT_OBJECT_CLOAKED = 0x8017;
    int EVENT_OBJECT_UNCLOAKED = 0x8018;
    int WINEVENT_OUTOFCONTEXT = 0x0000;
    int EVENT_OBJECT_SHOW = 0x8002;
    int EVENT_OBJECT_HIDE = 0x8003;

    int RIDI_DEVICENAME = 0x20000007;

    // 回调函数接口
    interface WinEventProc extends StdCallCallback {
        void callback(HANDLE hWinEventHook, int event, HWND hwnd, int idObject, int idChild, int dwEventThread, int dwmsEventTime);
    }

    HANDLE SetWinEventHook(int eventMin, int eventMax, HANDLE hmodWinEventProc, WinEventProc lpfnWinEventProc, int idProcess, int idThread, int dwflags);
    boolean UnhookWinEvent(HANDLE hWinEventHook);

    /**
     * 获取原始输入设备列表
     * @param pRawInputDeviceList 设备列表缓冲区，为 null 时返回设备总数
     * @param puiNumDevices 设备数量
     * @param cbSize 结构体大小
     * @return 成功返回设备数量，失败返回 -1
     */
    int GetRawInputDeviceList(WinUser.RAWINPUTDEVICELIST[] pRawInputDeviceList,
                              WinDef.UINTByReference puiNumDevices,
                              int cbSize);

    int GetRawInputDeviceInfo(WinNT.HANDLE hDevice, int uiCommand, char[] pData, IntByReference pcbSize);

}