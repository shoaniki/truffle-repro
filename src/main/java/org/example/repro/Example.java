package org.example.repro;

public interface Example
{
    default String overridden() {
        return "hello from Java";
    }

    default String inherited() {
        return "goodbye from Java";
    }
}
