
/*
 * de.unkrig.doclet.main - A doclet which generates HTML documentation for a Java "main(String[]") method
 *
 * Copyright (c) 2015, Arno Unkrig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *       following disclaimer in the documentation and/or other materials provided with the distribution.
 *    3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.unkrig.maindoc.doclet_test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import de.unkrig.commons.lang.protocol.RunnableWhichThrows;
import de.unkrig.commons.lang.security.ExitCatcher;

public
class DocletTest {

    private static final Method JAVADOC_MAIN_METHOD;

    private String docletClassName;
    private File[] docletPath;
    private File[] classpath;
    private File   destinationDirectory;
    private File[] files;
    static {

        try {
            Class<?> javadocMainClass = Class.forName("com.sun.tools.javadoc.Main");

            JAVADOC_MAIN_METHOD = javadocMainClass.getDeclaredMethod("main", String[].class);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public
    DocletTest(String docletClassName, File[] docletPath, File[] classpath, File destinationDirectory, File[] files) {
        this.docletClassName      = docletClassName;
        this.docletPath           = docletPath;
        this.classpath            = classpath;
        this.destinationDirectory = destinationDirectory;
        this.files                = files;
    }

    /**
     * Runs the JAVADOC tool in <em>this</em> JVM. The tool is configured by {@link #DocletTest(String, File[], File[],
     * File, File[]) the constructor}.
     */
    public final void
    runJavadoc() throws Exception {

        final List<String> args = new ArrayList<String>();

        args.add("-doclet");
        args.add(this.docletClassName);

        args.add("-docletpath");
        args.add(this.makePath(this.docletPath));

        args.add("-classpath");
        args.add(this.makePath(this.classpath));

        args.add("-d");
        args.add(this.destinationDirectory.getPath());

        for (File f : this.files) args.add(f.getPath());

        Integer status = ExitCatcher.catchExit(new RunnableWhichThrows<Exception>() {

            @Override public void
            run() throws Exception {
                try {
                    DocletTest.JAVADOC_MAIN_METHOD.invoke(null, (Object) args.toArray(new String[args.size()]));
                } catch (InvocationTargetException ite) {
                    Throwable te = ite.getTargetException();
                    if (te instanceof Exception) throw (Exception) te;
                    if (te instanceof Error) throw (Error) te;
                    throw ite;
                }
            }
        });

        if (status != null) Assert.assertEquals(0, (int) status);
    }

    private String
    makePath(File[] path) {

        if (path.length == 0) return "";

        StringBuilder sb = new StringBuilder().append(path[0].getPath());
        for (int i = 1; i < path.length; i++) {
            sb.append(File.pathSeparatorChar).append(path[i].getPath());
        }

        return sb.toString();
    }
}
