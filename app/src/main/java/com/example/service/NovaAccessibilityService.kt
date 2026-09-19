package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NovaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Collect current window state or package change
        val pkg = event?.packageName?.toString()
        if (pkg != null) {
            _currentPackage.value = pkg
        }
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
    }

    fun findAndClick(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            if (performClickOnNode(node)) {
                return true
            }
        }
        return false
    }

    fun findAndSetText(targetHintOrText: String, textToSet: String): Boolean {
        val root = rootInActiveWindow ?: return false
        // Search editable fields or matching text
        val nodes = root.findAccessibilityNodeInfosByText(targetHintOrText)
        if (nodes.isNotEmpty()) {
            for (node in nodes) {
                if (node.isEditable) {
                    val arguments = Bundle()
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToSet)
                    return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                }
            }
        }

        // Search any editable node on active screen
        return searchAndSetTextRecursive(root, textToSet)
    }

    private fun searchAndSetTextRecursive(node: AccessibilityNodeInfo?, textToSet: String): Boolean {
        if (node == null) return false
        if (node.isEditable) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToSet)
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        for (i in 0 until node.childCount) {
            if (searchAndSetTextRecursive(node.getChild(i), textToSet)) {
                return true
            }
        }
        return false
    }

    fun clickCoordinates(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply {
                moveTo(x, y)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(true)
                }
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(false)
                }
            }, null)
        } else {
            onComplete?.invoke(false)
        }
    }

    fun performScroll(forward: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return performActionRecursive(root, action)
    }

    private fun performClickOnNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        val parent = node.parent
        if (parent != null && parent.isClickable) {
            return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        return false
    }

    private fun performActionRecursive(node: AccessibilityNodeInfo?, action: Int): Boolean {
        if (node == null) return false
        if (node.performAction(action)) return true
        for (i in 0 until node.childCount) {
            if (performActionRecursive(node.getChild(i), action)) return true
        }
        return false
    }

    fun getScreenNodeHierarchy(): String {
        val root = rootInActiveWindow ?: return "Screen content unavailable or locked."
        val builder = StringBuilder()
        dumpNode(root, builder, 0)
        return builder.toString()
    }

    private fun dumpNode(node: AccessibilityNodeInfo?, builder: StringBuilder, depth: Int) {
        if (node == null || depth > 8) return
        val indent = "  ".repeat(depth)
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val className = node.className?.toString()?.substringAfterLast(".")
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        if (!text.isNullOrEmpty() || !desc.isNullOrEmpty() || isClickable || isEditable) {
            builder.append("$indent• [$className] ")
            if (!text.isNullOrEmpty()) builder.append("text=\"$text\" ")
            if (!desc.isNullOrEmpty()) builder.append("desc=\"$desc\" ")
            if (isClickable) builder.append("[Clickable] ")
            if (isEditable) builder.append("[Editable] ")
            builder.append("bounds=(${bounds.left},${bounds.top},${bounds.right},${bounds.bottom})\n")
        }

        for (i in 0 until node.childCount) {
            dumpNode(node.getChild(i), builder, depth + 1)
        }
    }

    companion object {
        var instance: NovaAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _currentPackage = MutableStateFlow("")
        val currentPackage: StateFlow<String> = _currentPackage.asStateFlow()
    }
}
