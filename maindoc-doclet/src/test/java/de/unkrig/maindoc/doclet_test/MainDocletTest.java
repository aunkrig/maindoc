
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

import org.junit.Ignore;
import org.junit.Test;

public
class MainDocletTest extends DocletTest {

    public static final String JDISASM_VERSION = "1.0.2-SNAPSHOT";

    public
    MainDocletTest() {
        super(
            "de.unkrig.doclet.main.MainDoclet", // docletClassName
            new File[] {                        // docletPath
                new File("../de.unkrig.doclet.main/target/classes"),
                new File("../commons-doclet/target/classes"),
                new File("../commons-io/target/classes"),
                new File("../commons-lang/target/classes"),
                new File("../commons-text/target/classes"),
                new File("../commons-util/target/classes"),
            },
            new File[] {                        // classpath
                new File("../zz-find/src/main/java"),
                new File("../commons-file/target/classes"),
                new File(
                    System.getProperty("user.home")
                    + "/.m2/repository/de/unkrig/jdisasm/jdisasm/"
                    + MainDocletTest.JDISASM_VERSION
                    + "/jdisasm-"
                    + MainDocletTest.JDISASM_VERSION
                    + ".jar"
                ),
                new File("../commons-io/target/classes"),
                new File("../commons-lang/target/classes"),
                new File("../commons-nullanalysis/target/classes"),
                new File("../commons-text/target/classes"),
                new File("../commons-util/target/classes"),
                new File(
                    System.getProperty("user.home")
                    + "/.m2/repository/org/apache/ant/ant/1.8.4/ant-1.8.4.jar"
                ),
                new File("../org.apache.commons.compress-1.9/lib/commons-compress-1.9.jar"),
            },
            new File("."),                      // destinationDirectory
            new File[] {                        // files
                new File("../zz-find/src/main/java/de/unkrig/zz/find/Main.java"),
            }
        );
        // TODO Auto-generated constructor stub
    }

    @Ignore @Test public void
    test1() throws Exception {
        this.runJavadoc();
    }
}
