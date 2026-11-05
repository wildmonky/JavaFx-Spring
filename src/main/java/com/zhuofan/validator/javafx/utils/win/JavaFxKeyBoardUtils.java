package com.zhuofan.validator.javafx.utils.win;

import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.ptr.PointerByReference;
import com.zhuofan.validator.javafx.controller.MainController;
import com.zhuofan.validator.javafx.utils.JavaFxUiUtils;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * FIXME 使用线程池
 */
@Slf4j
public class JavaFxKeyBoardUtils {

    // 固定的 CLSID 和 IID
    private static final Guid.CLSID CLSID_UIHostNoLaunch = new Guid.CLSID("4ce576fa-83dc-4F88-951c-9d0782b4e376");
    private static final Guid.IID IID_ITipInvocation = new Guid.IID("37c994e7-432b-4834-a2f7-dce1f13b834b");

    private static final String KEYBOARD_CLASS_NAME = "IPTip_Main_Window";

    public static final AtomicBoolean keyboardVisible = new AtomicBoolean(false);

    private static User32Ext.WinEventProc callback = (hook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime) -> {
        // 在回调中处理事件，注意这里的代码运行在 hookThread 上
        if (idObject != 0 || hwnd == null) return;
        if (WinEventHookUtils.isKeyboardProcess(hwnd)) {
            if (event == User32Ext.EVENT_OBJECT_UNCLOAKED || event == User32Ext.EVENT_OBJECT_SHOW) {
                log.info("[系统事件] 键盘已弹出");
                // 状态更新是安全的，因为 AtomicBoolean 是线程安全的
                JavaFxKeyBoardUtils.keyboardVisible.set(true);
            } else if (event == User32Ext.EVENT_OBJECT_CLOAKED || event == User32Ext.EVENT_OBJECT_HIDE) {
                log.info("[系统事件] 键盘已收起");
                JavaFxKeyBoardUtils.keyboardVisible.set(false);
            }
        }
    };

    public static void bindWindowsTouchKeyboard() {
        invokeWindowsTouchKeyboard(true);
    }

    /**
     * 节点绑定focus、click事件唤起键盘
     *
     * @param node
     */
    public static void bindWindowsTouchKeyboard(TextInputControl node) {
        if (node == null) {
            return;
        }

        node.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (BooleanUtils.isNotTrue(newVal)) {
                return;
            }
            log.info("focus 调用键盘");
            invokeWindowsTouchKeyboard(true);
        });
        node.setOnMouseClicked(mouseEvent -> {
            log.info("mouse click 调用键盘");
            invokeWindowsTouchKeyboard(true);
        });
        node.setOnInputMethodTextChanged(event -> {
            // 在用户按下Enter之前，event.getCommitted() 很可能返回空字符串
            log.info("键盘数据插入");
            node.appendText(event.getCommitted());
        });
    }

    /**
     * 唤起触摸键盘
     */
    public static synchronized void invokeWindowsTouchKeyboard(boolean show) {
        // 1. 检查 TabTip 窗口是否存在
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(KEYBOARD_CLASS_NAME, null);

        if (hwnd != null) {
            // 2. 窗口存在（进程驻留），调用 COM 的 Toggle() 方法重新显示
            // 此处需要 JNA 的 COM 支持或调用原生方法，逻辑上等同于 ITipInvocation.Toggle()
            toggleKeyboardViaCOM(show);
        } else {
            try {
                // 路径指向Windows自带的触摸键盘程序
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "C:\\Program Files\\Common Files\\microsoft shared\\ink\\tabtip.exe"}, null, null);
                // 监听显示/隐藏事件
                WinEventHookUtils.start(0, hwnd, callback);
                keyboardVisible.set(true);
            } catch (Exception e) {
                log.error("键盘唤起失败", e);
                JavaFxUiUtils.alertMessage(Alert.AlertType.ERROR, "键盘唤起失败", Duration.seconds(5));
            }
        }
    }

    /**
     * 切换触摸键盘的显示状态（需要 TabTip.exe 已在后台运行）
     */
    public static void toggleKeyboardViaCOM(boolean show) {

        // 1. 探测当前状态 2、设置反转状态
        boolean isVisible = keyboardVisible.get();

        // 2. 如果当前状态与期望状态一致，则无需操作
        if (isVisible == show) {
            log.info("键盘当前状态已为 {}，无需切换。", show ? "显示" : "隐藏");
            return;
        }

        PointerByReference ppv = new PointerByReference();
        boolean comInitialized = false;

        try {
            // 1. 初始化 COM 库
            WinNT.HRESULT hrInit = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED);
            if (Objects.equals(hrInit, WinNT.S_OK)) {
                comInitialized = true;
            } else if (!Objects.equals(hrInit, WinNT.S_FALSE)) { // S_FALSE 表示已经初始化过了
                log.error("COM 初始化失败，错误码: {}", hrInit.intValue());
                return;
            }

            // 2. 创建 COM 对象实例
            WinNT.HRESULT hrCreate = Ole32.INSTANCE.CoCreateInstance(
                    CLSID_UIHostNoLaunch,
                    null,
                    WTypes.CLSCTX_INPROC_SERVER | WTypes.CLSCTX_LOCAL_SERVER,
                    IID_ITipInvocation,
                    ppv
            );

            if (WinNT.S_OK.equals(hrCreate)) {
                // 3. 获取指针并调用 Toggle 方法
                TipInvocation comObject = new TipInvocation(ppv.getValue());
                WinDef.HWND hwndDesktop = User32.INSTANCE.GetDesktopWindow();

                // 判断是否绑定监听
                if (!WinEventHookUtils.isHooked(hwndDesktop)) {
                    WinEventHookUtils.stop();
                    WinEventHookUtils.start(0, hwndDesktop, callback);
                }

                // 1. 反转键盘显示状态
                keyboardVisible.set(!keyboardVisible.get());

                // Toggle 是继承 IUnknown (3个方法) 之后的第 1 个方法，所以索引是 3
                // 参数是一个 HWND (桌面窗口句柄)
                comObject.toggle(hwndDesktop);

                // 4. 释放 COM 对象（非常重要，防止内存泄漏）
                comObject.Release();
            } else {
                log.error("创建 COM 对象失败，TabTip 可能未在后台运行。错误码: {}", hrCreate.intValue());
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            // 5. 如果是本线程初始化的 COM，则需要反初始化
            if (comInitialized) {
                Ole32.INSTANCE.CoUninitialize();
            }
        }
    }

    public static class TipInvocation extends Unknown {
        public TipInvocation(com.sun.jna.Pointer p) {
            super(p);
        }

        // 在子类内部，可以合法访问 protected 的 _invokeNativeInt
        public void toggle(WinDef.HWND hwndDesktop) {
            // Toggle 是 vtable 索引 3 的方法（IUnknown 占了 0, 1, 2）
            _invokeNativeInt(3, new Object[]{ getPointer(), hwndDesktop });
        }
    }

    /**
     * 全局绑定键盘唤起
     * @param scene
     */
    public static void bindGlobalKeyboard(Scene scene) {
        // 使用 MOUSE_CLICKED 而非 focus 属性监听，能解决控件已聚焦但键盘未显示时，再次点击无法触发的问题
        scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!MainController.useVirtualKeyboardVal) {
                return;
            }
            // 获取点击事件的目标节点
            Node target = scene.getFocusOwner();
//            Object target = event.getTarget();
            if (target instanceof TextInputControl textInput) {
                if (textInput.isEditable()) {
                    // 延迟一小段时间，确保焦点已切换且窗口已准备就绪
                    Platform.runLater(JavaFxKeyBoardUtils::bindWindowsTouchKeyboard);
                }
            }

            if (target instanceof ComboBox<?> comboBox) {
                if (comboBox.isEditable() && comboBox.getEditor().isEditable()) {
                    // 延迟一小段时间，确保焦点已切换且窗口已准备就绪
                    Platform.runLater(JavaFxKeyBoardUtils::bindWindowsTouchKeyboard);
                }
            }
        });
    }

}
