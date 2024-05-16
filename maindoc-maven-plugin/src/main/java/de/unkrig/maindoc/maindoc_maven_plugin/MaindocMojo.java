
/*
 * maindoc - A tool for generating documentation for a single Java method from doc comments, similar to JAVADOC
 *
 * Copyright (c) 2020, Arno Unkrig
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

package de.unkrig.maindoc.maindoc_maven_plugin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import de.unkrig.maindoc.doclet.MainDoclet;

@Mojo(
    name                         = "maindoc",
    defaultPhase                 = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.COMPILE
) public
class MaindocMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true) MavenProject project;
    @Parameter(defaultValue = "target/classes")                               File         destination;
    @Parameter(defaultValue = "src/main/java")                                List<File>   sourcepath;
    @Parameter(defaultValue = "main(String[])")                               String       method;
    @Parameter                                                                String       docEncoding;
    @Parameter                                                                String       charset;
    @Parameter                                                                String       doctitle;
    @Parameter(defaultValue = "false")                                        boolean      quiet;
    @Parameter                                                                String[]     packages;

    public void
    execute() throws MojoExecutionException {
        try {
            this.execute2();
        } catch (MojoExecutionException mee) {
            throw mee;
        } catch (Exception e) {
            throw new MojoExecutionException("maindoc", e);
        }
    }
    
    public void
    execute2() throws Exception {
        
        String classpath;
        {
            List<File> cp = new ArrayList<>();
            for (Artifact a : this.project.getArtifacts()) cp.add(a.getFile());
            classpath = makePath(cp);
        }
        
        List<String> args = new ArrayList<>();

        args.add("-doclet");
        args.add(MainDoclet.class.getName());
        
        args.add("-classpath");
        args.add(classpath);

        args.add("-d");
        args.add(this.destination.getAbsolutePath());

        args.add("-sourcepath");
        args.add(makePath(this.sourcepath));
        
        if (this.method != null) {
            args.add("-method");
            args.add(this.method);
        }

        if (this.docEncoding != null) {
            args.add("-docencoding");
            args.add(this.docEncoding);
        }
        
        if (this.charset != null) {
            args.add("-charset");
            args.add(this.charset);
        }
        
        if (this.doctitle != null) {
            args.add("-doctitle");
            args.add(this.doctitle);
        }
        
        if (this.quiet) args.add("-quiet");
        
        for (String p : this.packages) args.add(p);
        
        int status = com.sun.tools.javadoc.Main.execute(args.toArray(new String[args.size()]));
        
        if (status != 0) throw new MojoExecutionException("Javadoc failed with status " + status);
    }

    private static String
    makePath(List<File> files) {
        
        StringBuilder sb = new StringBuilder();

        Iterator<File> it = files.iterator();
        for (;;) {
            sb.append(it.next().getAbsolutePath());
            if (!it.hasNext()) break;
            sb.append(File.pathSeparatorChar);
        }
        
        return sb.toString();
    }
}
