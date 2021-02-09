package org.example.repro;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class Main
{
    public static void test() throws IOException
    {
        // parse JS and cast the result to a Java interface
        var res = requireNonNull(Main.class.getResource("test.js"));
        var src = Source.newBuilder("js", res).build();
        var ctx = Context.newBuilder("js").allowAllAccess(true).build();
        var example = ctx.eval(src).as(Example.class);

        // Invoking a method implemented in JS always works
        System.out.println(example.overridden());

        // Invoking an inherited default method only works if:
        //  (a) JDK-8226916 is implemented, i.e. this is Java 14 or newer;
        //  (b) the interface is accessible both to this class *and to Truffle*,
        //      i.e. org.graalvm.truffle reads org.example.repro.
        System.out.println(example.inherited());
    }


    public static void main(String[] args) throws IOException
    {
        var jver = Integer.getInteger("java.specification.version");
        System.out.printf("Running test case with %s (Java %d)%n",
                          System.getProperty("java.vendor.version"), jver);

        if (jver < 14) {
            System.out.println("Expecting to fail because JDK-8226916 requires Java 14+");
        } else {
            var truffle = ModuleLayer.boot().findModule("org.graalvm.truffle").orElseThrow();
            if (truffle.canRead(Example.class.getModule())) {
                System.out.println("Expecting to succeed");
            } else {
                System.out.println("Expecting to fail if Truffle does not choose to read the module");
            }
        }

        try {
            test();
        } catch (UnsupportedOperationException ex) {
            try {
                System.err.println(ex.getSuppressed()[0].getCause().getMessage());
            } catch (Exception e) {
                throw ex;
            }
        }
    }
}
