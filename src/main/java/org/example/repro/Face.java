package org.example.repro;

public interface Face
{
    default String smile() {
        return "😁";
    }

    default String frown() {
        return "😠";
    }
}
