package com.zhuofan.validator.javafx.utils.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

/**
 * FIXME 是否有键盘判断不准确，平板预置按钮键盘(开关、音量)
 */
@Slf4j
public class KeyboardDetector {

    /**
     * 判断系统中是否存在键盘设备（内置或外接）
     * @return true 表示至少检测到一个键盘设备
     */
    public static boolean isKeyboardPresent() {
        // 第一次调用，获取设备总数
        WinDef.UINTByReference deviceCount = new WinDef.UINTByReference();
        WinUser.RAWINPUTDEVICELIST rawinputdevicelist = new WinUser.RAWINPUTDEVICELIST();
        int result = User32Ext.INSTANCE.GetRawInputDeviceList(null, deviceCount, rawinputdevicelist.sizeof());
        if (result == -1 || deviceCount.getValue().intValue() == 0) {
            return false; // 调用失败或没有任何输入设备
        }

        WinDef.UINT value = deviceCount.getValue();
        int count = value.intValue();
        WinUser.RAWINPUTDEVICELIST[] devices = (WinUser.RAWINPUTDEVICELIST[]) new WinUser.RAWINPUTDEVICELIST().toArray(count);

        // 第二次调用，填充设备列表
        result = User32Ext.INSTANCE.GetRawInputDeviceList(devices, deviceCount, rawinputdevicelist.sizeof());
        if (result == -1) {
            return false;
        }

        // 遍历列表，查找键盘设备
        for (int i = 0; i < result; i++) {
            log.info("键盘设备{}: {}", i + 1, devices[i]);
            if (devices[i].dwType != WinUser.RIM_TYPEKEYBOARD) {
                continue;
            }

            // 关键：byte[] 缓冲区，不是 char[]
            char[] nameBuf = new char[512];
            IntByReference sizeRef = new IntByReference(nameBuf.length); // 初始值 = 字节数

            int ret = User32Ext.INSTANCE.GetRawInputDeviceInfo(
                    devices[i].hDevice,        // HANDLE 类型，直接传
                    User32Ext.RIDI_DEVICENAME,   // 0x20000007
                    nameBuf,
                    sizeRef
            );

            if (ret >= 0) {
                // Native.toString(byte[]) 按 ANSI（平台默认编码）解码
                String path = Native.toString(nameBuf);
                log.info("键盘设备 {}: path={}", i + 1, path);
            } else {
                log.warn("获取设备路径失败, 错误码: {}", Native.getLastError());
            }

            return true; // 发现键盘
        }
        return false;
    }

    // 测试
    public static void main(String[] args) {
        System.out.println("键盘是否存在: " + isKeyboardPresent());
    }
}