package org.example.repro;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        var res = requireNonNull(Main.class.getResource("test.js"));
        var src = Source.newBuilder("js", res).build();
        var ctx = Context.newBuilder("js").allowAllAccess(true).build();

        // parse JS and use polyglot Value.as() to cast it to a Java interface
        var face = ctx.eval(src).as(Face.class);

        // Invoking a method implemented in JS always works
        System.out.println(face.smile());

        // Invoking an inherited default method only works if:
        //  (a) JDK-8226916 is implemented, i.e. this is Java 14 or newer;
        //  (b) the interface is accessible both to this class *and to Truffle*,
        //      i.e. org.graalvm.truffle reads org.example.repro.
        System.out.println(face.frown());
    }
}
