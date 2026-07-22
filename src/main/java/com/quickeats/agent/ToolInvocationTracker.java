package com.quickeats.agent;

import java.util.ArrayList;
import java.util.List;

public class ToolInvocationTracker {
    private static final ThreadLocal<List<String>> INVOKED_TOOLS = ThreadLocal.withInitial(ArrayList::new);

    public static void logToolCall(String toolName) {
        INVOKED_TOOLS.get().add(toolName);
    }

    public static List<String> getInvokedTools() {
        return new ArrayList<>(INVOKED_TOOLS.get());
    }

    public static void clear() {
        INVOKED_TOOLS.remove();
    }

    public static List<String> getAndClearInvokedTools() {
        List<String> tools = new ArrayList<>(INVOKED_TOOLS.get());
        INVOKED_TOOLS.remove();
        return tools;
    }
}
