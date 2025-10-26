package ch.vorburger.jvmtools;

import ch.vorburger.main.ExecutableContext;
import ch.vorburger.main.Main;

import javax.tools.JavaCompiler;

public record JavaCompilerToolMain(JavaCompiler javaCompilerTool) implements Main {

    public JavaCompilerToolMain() {
        this(javax.tools.ToolProvider.getSystemJavaCompiler());
    }

    @Override
    public Integer invoke(ExecutableContext input) throws Exception {
        var io = input.stdIO();
        return javaCompilerTool.run(io.in(), io.out(), io.err(), input.argsAsArray());
    }
}
