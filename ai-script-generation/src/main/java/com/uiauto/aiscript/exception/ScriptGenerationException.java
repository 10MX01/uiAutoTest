package com.uiauto.aiscript.exception;

/**
 * 脚本生成异常
 */
public class ScriptGenerationException extends RuntimeException {

    public ScriptGenerationException(String message) {
        super(message);
    }

    public ScriptGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
