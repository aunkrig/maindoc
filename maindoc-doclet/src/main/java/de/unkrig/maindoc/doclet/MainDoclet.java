
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

package de.unkrig.maindoc.doclet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;

import de.unkrig.commons.doclet.Annotations;
import de.unkrig.commons.doclet.Tags;
import de.unkrig.commons.doclet.html.Html;
import de.unkrig.commons.io.IoUtil;
import de.unkrig.commons.lang.AssertionUtil;
import de.unkrig.commons.lang.ExceptionUtil;
import de.unkrig.commons.lang.protocol.ConsumerWhichThrows;
import de.unkrig.commons.lang.protocol.Longjump;
import de.unkrig.commons.lang.protocol.ProducerUtil;
import de.unkrig.commons.lang.protocol.ProducerUtil.BooleanProducer;
import de.unkrig.commons.nullanalysis.Nullable;
import de.unkrig.commons.text.Notations;
import de.unkrig.commons.util.CommandLineOptions;
import de.unkrig.commons.util.annotation.CommandLineOption;
import de.unkrig.commons.util.annotation.CommandLineOption.Cardinality;
import de.unkrig.html2txt.Html2Txt;

/**
 * @see #start(RootDoc)
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/javadoc/doclet/overview.html">Doclet
 *      Overview</a>
 */
public
class MainDoclet {

    static { AssertionUtil.enableAssertionsForThisClass(); }

    // Block tag names.
    public static final String BT_main_commandLineOptionGroup   = "@main.commandLineOptionGroup";
    public static final String BT_main_commandLineOptionComment = "@main.commandLineOptionComment";
    public static final String BT_param                         = "@param";

    // Inline tag names.
    public static final String IT_main_commandLineOptions = "@main.commandLineOptions";
    public static final String IT_main_maindoc            = "@main.maindoc";
    public static final String IT_code                    = "@code";
    public static final String IT_literal                 = "@literal";
    public static final String IT_value                   = "@value";
    public static final String IT_link                    = "@link";
    public static final String IT_linkplain               = "@linkplain";
    public static final String IT_docRoot                 = "@docRoot";
    public static final String IT_constantsof             = "@constantsof";
    public static final String IT_constantsofplain        = "@constantsofplain";

    private static File              destinationDirectory  = new File(".");
    private static String            method                = "main(String[])";
    private static Charset           htmlOutputFileCharset = Charset.defaultCharset();
    @Nullable private static String  charset;
    @Nullable private static String  doctitle;
    private static boolean           quiet;

    private static final Html2Txt html2Txt = new Html2Txt();

    /**
     * Where to create the .html and .txt output files.
     * The effective name of each file is "<var>destinationDirectory</var>{@code /}<var>package</var>{@code
     * /}<var>class</var>{@code .}<var>method</var>{@code .html}", resp. "...{@code .txt}".
     * The default destination directory is "{@code .}".
     *
     * @main.commandLineOptionGroup HTML generation
     */
    @CommandLineOption(name = { "-d", "--destination" }) public static void
    setDestination(File destinationDirectory) { MainDoclet.destinationDirectory = destinationDirectory; }

    /**
     * The signature of the method to document. The default is {@code "main(String[])"}; for a doclet, e.g., you may
     * want to specify {@code "--method start(RootDoc)"} to document the doclet "main method".
     */
    @CommandLineOption public static void
    setMethod(String method) { MainDoclet.method = method; }

    /**
     * The charset to use when writing the {@code .html} files. The default is the JVM default charset,
     * "${file.encoding}".
     *
     * @main.commandLineOptionGroup HTML generation
     */
    @CommandLineOption public static void
    setDocencoding(Charset charset) {
        MainDoclet.htmlOutputFileCharset = charset;
        MainDoclet.html2Txt.setInputCharset(charset);
    }

    /**
     * The HTML character set for this document. If set, then the following tag appears in the {@code <head>} of the
     * generated HTML document:<br />
     * {@code <meta http-equiv="Content-Type" content="text/html; charset="}<var>charset</var>{@code ">}
     *
     * @main.commandLineOptionGroup HTML generation
     */
    @CommandLineOption public static void
    setCharset(String charset) { MainDoclet.charset = charset; }

    /**
     * The title to place near the top of the {@code .html} output file.
     *
     * @main.commandLineOptionGroup HTML generation
     */
    @CommandLineOption public static void
    setDoctitle(String title) { MainDoclet.doctitle = title; }

    /**
     * Suppresses normal output (like "Generating...").
     */
    @CommandLineOption public static void
    setQuiet() { MainDoclet.quiet = true; }

    /**
     * The charset to use when writing the .txt output files. The default is the JVM default charset, "${file.encoding}".
     *
     * @main.commandLineOptionGroup   HTML-to-txt conversion
     * @main.commandLineOptionComment (defaults to the JVM default charset, "${file.encoding}".)
     */
    @CommandLineOption public static void
    setTxtOutputFileCharset(Charset charset) { MainDoclet.html2Txt.setOutputCharset(charset); }

    /**
     * @see MainDoclet#setTxtPageWidth(int)
     * @main.commandLineOptionComment (defaults to 0)
     */
    @CommandLineOption public static void
    setTxtPageLeftMarginWidth(int n) { MainDoclet.html2Txt.setPageLeftMarginWidth(n); }

    /**
     * @see MainDoclet#setTxtPageWidth(int)
     * @main.commandLineOptionComment (defaults to 1)
     */
    @CommandLineOption public static void
    setTxtPageRightMarginWidth(int n) {
        MainDoclet.html2Txt.setPageRightMarginWidth(n);
    }

    /**
     * The maximum length of output lines is "<var>pageWidth</var> - <var>rightMarginWidth</var>".
     *
     * @main.commandLineOptionGroup   HTML-to-txt conversion
     * @main.commandLineOptionComment (defaults to "$COLUMNS" or 80)
     * @see #setTxtPageRightMarginWidth(int)
     */
    @CommandLineOption public static void
    setTxtPageWidth(int n) { MainDoclet.html2Txt.setPageWidth(n); }

    /** @main.commandLineOptionGroup Compatibility */
    @CommandLineOption public static void setBottom(String text) {}
    /** @main.commandLineOptionGroup Compatibility */
    @CommandLineOption public static void addLink(String extDocUrl) {}
    /** @main.commandLineOptionGroup Compatibility */
    @CommandLineOption public static void addLinkoffline(String extDocUrl, String packageListLoc) {}
    /** @main.commandLineOptionGroup Compatibility */
    @CommandLineOption public static void setWindowtitle(String title) {}

    /**
     * See <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/javadoc/doclet/overview.html">"Doclet
     * Overview"</a>.
     */
    public static LanguageVersion languageVersion() { return LanguageVersion.JAVA_1_5; }

    /**
     * See <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/javadoc/doclet/overview.html">"Doclet
     * Overview"</a>.
     */
    public static int
    optionLength(String option) throws IOException {

        if ("-help".equals(option)) {
            CommandLineOptions.printResource(
                MainDoclet.class,
                "start(RootDoc).txt",
                Charset.forName("UTF-8"),
                System.out
            );
            return 1;
        }

        Method m = CommandLineOptions.getMethodForOption(option, MainDoclet.class);

        return m == null ? 0 : 1 + m.getParameterTypes().length;
    }

    /**
     * A doclet that generates {@code .html} and {@code .txt} documentation for the "{@code main()}" method of a Java
     * class.
     *
     * <h2>Command line options:</h2>
     * <dl>
     * {@main.commandLineOptions}
     * </dl>
     *
     * <h3>HTML generation options:</h3>
     * <dl>
     * {@main.commandLineOptions HTML generation}
     * </dl>
     *
     * <h3>HTML-to-txt conversion options:</h3>
     * <dl>
     * {@main.commandLineOptions HTML-to-txt conversion}
     * </dl>
     *
     * <h3>Compatibility options:</h3>
     * These options exist for compatibility with the standard JAVADOC doclet, and are ignored.
     * <dl>
     * {@main.commandLineOptions Compatibility}
     * </dl>
     *
     * <h2>Supported inline tags:</h2>
     *
     * <dl>
     *   <dt><a name="main.commandLineOptions" /><code>{{@value #IT_main_commandLineOptions}}</code></dt>
     *   <dt><code>{{@value #IT_main_commandLineOptions} <var>group-name</var>}</code></dt>
     *   <dd>
     *     Generates {@code <dt>} / {@code <dd>} pairs for all command line options, generated from
     *     <code>@CommandLineOption</code>-annotated setter methods.
     *     <br />
     *     If a <var>group-name</var> is given, then only those options appear which have a
     *     <a href="#main.commandLineOptionGroup"><code>{@value #BT_main_commandLineOptionGroup}</code> block tag</a> with
     *     equal <var>group-name</var>; otherwise, only those options appear which have <em>no</em>
     *     <code>{@value #BT_main_commandLineOptionGroup}</code> block tag.
     *   </dd>
     *   <dt><code>{{@value #IT_code}      <var>text</var>}</code></dt>
     *   <dt><code>{{@value #IT_literal}   <var>text</var>}</code></dt>
     *   <dt><code>{{@value #IT_value}     <var>package</var>.<var>class</var>#<var>field</var>}</code></dt>
     *   <dt><code>{{@value #IT_link}      <var>package</var>.<var>class</var>#<var>member</var> <var>label</var>}</code></dt>
     *   <dt><code>{{@value #IT_linkplain} <var>package</var>.<var>class</var>#<var>member</var> <var>label</var>}</code></dt>
     *   <dt><code>{{@value #IT_docRoot}}</code></dt>
     *   <dd>
     *     <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#javadoctags">Same as for
     *     the standard doclet</a>.
     *   </dd>
     *   <dt><code>{{@value #IT_constantsof}      <var>package</var>.<var>enum-type</var>}</code></dt>
     *   <dt><code>{{@value #IT_constantsofplain} <var>package</var>.<var>enum-type</var>}</code></dt>
     *   <dd>
     *     Links to all constants of the designated enum type, separated with "{@code , }" (a comma and a space).
     *   </dd>
     * </dl>
     *
     * <h2>Supported block tags:</h2>
     *
     * <dl>
     *   <dt>
     *     <a name="main.commandLineOptionGroup" /><code>{@value #BT_main_commandLineOptionGroup}
     *     <var>group-name</var></code>
     *   </dt>
     *   <dd>
     *     Assigns this command line option setter method to a "group"; see <a
     *     href="#main.commandLineOptions">the <code>{@value #IT_main_commandLineOptions}</code> inline tag</a>.
     *   </dd>
     *   <dt>
     *     <code>{@value #BT_main_commandLineOptionComment} <var>text</var></code>
     *   </dt>
     *   <dd>
     *     Renders the <var>text</var> at the end of the command line options's {@code <dt>}; often used to document
     *     the command line option's default value
     *   </dd>
     *   <dt>
     *     <code>{@value #BT_param} <var>param-name</var> <var>text</var></code>
     *   </dt>
     *   <dd>
     *     Use that text as the parameter name
     *   </dd>
     * </dl>
     */
    public static boolean
    start(final RootDoc rootDoc) throws IOException {

        // Apply the doclet options.
        for (String[] option : rootDoc.options()) {

            Method m = CommandLineOptions.getMethodForOption(option[0], MainDoclet.class);

            // It is quite counterintuitive, but "RootDoc.options()" returns ALL options, not only those which
            // qualified by 'optionLength()'.
            if (m == null) continue;

            int res;
            try {
                res = CommandLineOptions.applyCommandLineOption(option[0], m, option, 1, null);
            } catch (Exception e) {
                throw ExceptionUtil.wrap("Parsing command line option \"" + option[0] + "\"", e, IOException.class);
            }
            assert res == option.length;
        }

        // Process all specified classes and packages.
        List<ClassDoc> allClasses = new ArrayList<ClassDoc>();
        allClasses.addAll(Arrays.asList(rootDoc.specifiedClasses()));
        for (PackageDoc pd : rootDoc.specifiedPackages()) allClasses.addAll(Arrays.asList(pd.allClasses()));

        if (allClasses.isEmpty()) {
            System.err.println("No classes specified.");
            throw new IOException();
        }

        // Identify the methods to document.
        for (ClassDoc cd : allClasses) {
            for (MethodDoc md : cd.methods()) {
                String tmp = md.name() + md.flatSignature();
                if (MainDoclet.method.equals(tmp)) {
                    MainDoclet.convertDoc(md, rootDoc);
                }
            }
        }

        return true;
    }

    private static void
    convertDoc(Doc doc, RootDoc rootDoc) throws IOException {

    	final String htmlText = MainDoclet.convertDoc2(doc, rootDoc);

        final ClassDoc cd = (
            doc.isClass() ? (ClassDoc) doc :
            doc instanceof MemberDoc ? ((MemberDoc) doc).containingClass() :
            (ClassDoc) ExceptionUtil.throW(new AssertionError(String.valueOf(doc)))
        );

        File htmlOutputFile = new File(
            MainDoclet.destinationDirectory,
            (
                cd.qualifiedName().replace('.', File.separatorChar)
                + "."
                + doc.name()
                + (doc instanceof ExecutableMemberDoc ? ((ExecutableMemberDoc) doc).flatSignature() : "")
                + ".html"
            )
        );

        if (!MainDoclet.quiet) System.err.println("Generating \"" + htmlOutputFile + "\"...");

        if (MainDoclet.destinationDirectory.exists() && !htmlOutputFile.getParentFile().isDirectory()) htmlOutputFile.getParentFile().mkdirs();

        IoUtil.outputFilePrintWriter(
            htmlOutputFile,
            MainDoclet.htmlOutputFileCharset,
            new ConsumerWhichThrows<PrintWriter, RuntimeException>() {

                @Override public void
                consume(PrintWriter pw) {

                    pw.println("<html>");
                    pw.println("  <head>");
                    if (MainDoclet.charset != null) {
                        pw.println(
                            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset="
                            + MainDoclet.charset
                            + "\" />"
                        );
                    }
                    pw.println("  </head>");
                    pw.println("  <body>");
                    if (MainDoclet.doctitle != null) {
                        pw.println("    <h1>" + MainDoclet.doctitle + "</h1>");
                    }
                    pw.println(htmlText.replaceAll("(?m)^", "    "));
                    pw.println("  </body>");
                    pw.println("</html>");
                }
            }
        );

        // Convert generated HTML document into plain text format.
        File txtOutputFile;
        {
            String ofn = htmlOutputFile.getName();
            assert (ofn.endsWith(".html"));
            ofn = ofn.substring(0, ofn.length() - 5) + ".txt";
            txtOutputFile = new File(htmlOutputFile.getParentFile(), ofn);
        }

        if (!MainDoclet.quiet) System.err.println("Generating \"" + txtOutputFile + "\"...");

        try {
            MainDoclet.html2Txt.html2txt(htmlOutputFile, txtOutputFile);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IOException(null, e);
        }
    }


    private static String
    convertDoc2(final Doc doc, RootDoc rootDoc) {

        final ClassDoc cd = (
            doc.isClass() ? (ClassDoc) doc :
            doc instanceof MemberDoc ? ((MemberDoc) doc).containingClass() :
            (ClassDoc) ExceptionUtil.throW(new AssertionError(String.valueOf(doc)))
        );

        final Html html = new Html(Html.STANDARD_LINK_MAKER) {

            @Override protected String
            expandTag(Doc ref, RootDoc rootDoc, Tag tag) throws Longjump {

                String tagName = tag.name();

                // Backwards compatibility.
                if ("@command-line-options".equals(tagName)) {
                    rootDoc.printWarning(
                        ref.position(),
                        (
                            "\"@command-line-options\" is deprecated; use \""
                            + MainDoclet.IT_main_commandLineOptions
                            + "\" instead"
                        )
                    );
                    tagName = MainDoclet.IT_main_commandLineOptions;
                }

                if (MainDoclet.IT_main_commandLineOptions.equals(tagName)) {

                    StringBuilder sb = new StringBuilder();

                    // Process this class and all superclasses.
                    final BooleanProducer first = ProducerUtil.once();
                    for (ClassDoc c = cd; c != null; c = c.superclass()) {

                        // Process all methods.
                        for (MethodDoc md : c.methods()) {

                            // We're only interested in those with a "@CommandLineOption" annotation.
                            AnnotationDesc cload = Annotations.get(md, CommandLineOption.class, rootDoc);
                            if (cload == null) continue;

                            // Skip options that have NO text and a @see block tag - they are "companions" to other
                            // options and will be documented TOGETHER with these.
                            if (MainDoclet.getCompanionOf(md, rootDoc) != null) continue;

                            // Check if the command line option GROUP matches.
                            GROUP_MATCHES: {
                                Tag[] clogts = md.tags(MainDoclet.BT_main_commandLineOptionGroup);

                                // Backwards compatibility:
                                if (clogts.length == 0) {

                                    // Discouraged, because the tag name lacks a ".".
                                    clogts = md.tags("@command-line-option-group");
                                    if (clogts.length > 0) {
                                        rootDoc.printWarning(
                                            ref.position(),
                                            (
                                                "\"@command-line-option-group\" is deprecated; "
                                                + "use \""
                                                + MainDoclet.BT_main_commandLineOptionGroup
                                                + "\"\" instead"
                                            )
                                        );
                                    }
                                }

                                if (tag.inlineTags().length == 0) {
                                    if (clogts.length != 0) continue;
                                } else {
                                    for (Tag clogt : clogts) {
                                        if (clogt.text().equals(tag.text())) {
                                            break GROUP_MATCHES;
                                        }
                                    }
                                    continue;
                                }
                            }

                            if (!first.produce()) sb.append('\n');

                            // Render <dt> elements for THIS attribute.
                            this.attributeTerm(md, sb, rootDoc);

                            // Render <dt> elements for all "companion" attributes (methods that have a bare @see block
                            // tag pointing to THIS attribute).
                            for (MethodDoc md2 : c.methods()) {

                                // Notice: "Doc.inlineTags()" tends to print "warning - Tag @see: can't find ... in
                                // ..."; to debug, set a breakpoint at "DocEnv.warning()".

                                if (MainDoclet.getCompanionOf(md2, rootDoc) == md) this.attributeTerm(md2, sb, rootDoc);
                            }

                            String dd;
                            {
                                Tag[] its = md.inlineTags();

                                // Iff the DOC comment contains a bare "@see" tag, substitute the inline tags
                                // from the target of the @see tag.
                                if (its.length == 0) {

                                    SeeTag seeTag = (SeeTag) Tags.optionalTag(md, "see", rootDoc);
                                    if (seeTag != null) {

                                        Doc target = Html.targetOfSeeTag(seeTag);
                                        if (target != null) its = target.inlineTags();
                                    }
                                }

                                dd = this.fromTags(its, md, rootDoc);
                            }

                            {
                                Tag deprecatedTag = Tags.optionalTag(md, "deprecated", rootDoc);
                                if (deprecatedTag != null) {
                                    dd = "<i><b>Deprecated</b> - " + this.fromTags(deprecatedTag.inlineTags(), md, rootDoc) + "</i> " + dd;
                                }
                            }

                            sb.append("  <dd>\n");
                            sb.append(dd.replaceAll("(?m)^", "    "));
                            sb.append("\n  </dd>\n");
                        }
                    }

                    return sb.toString();
                } else
                if (MainDoclet.IT_main_maindoc.equals(tagName)) {
                	try {
	                	Doc target = Html.hrefToDoc(tag.text(), rootDoc, cd);
	                	return MainDoclet.convertDoc2(target, rootDoc);
                	} catch (Longjump l) {}
                }

                return super.expandTag(ref, rootDoc, tag);
            }

            /**
             * Appends zero or more {@code <dt>} elements to <var>out</var>.
             */
            private void
            attributeTerm(MethodDoc md, StringBuilder out, RootDoc rootDoc) throws Longjump {

                AnnotationDesc cload = Annotations.get(md, CommandLineOption.class, rootDoc);
                if (cload == null) return;

                // Determine the attribute name.
                assert cload != null;
                String[] names = Annotations.getElementValue(cload, "name", String[].class);
                if (names == null) {
                    String n = md.name();
                    if (n.startsWith("set")) {
                        n = n.substring(3);
                    } else
                    if (n.startsWith("add")) {
                        n = n.substring(3);
                    } else
                    if (n.startsWith("is")) {
                        n = n.substring(2);
                    }
                    names = new String[] { Notations.fromCamelCase(n).toLowerCaseHyphenated() };
                }

                // Convert the method parameters into command line argument placeholders.
                String suffix = "";
                PARAMETERS:
                for (Parameter p : md.parameters()) {

                    // If there is an "@param" tag for the parameter, use that.
                    for (Tag paramTag : md.tags(MainDoclet.BT_param)) {
                        String fs = this.fromTags(paramTag.firstSentenceTags(), md, rootDoc);
                        if (paramTag.text().startsWith(p.name() + " ")) {
                            suffix += fs.substring(p.name().length());
                            continue PARAMETERS;
                        }
                    }

                    // If it is an enum type, concatenate the constants with "|".
                    ClassDoc parameterType = p.type().asClassDoc();
                    if (parameterType != null && parameterType.isEnum()) {
                        suffix += " ";
                        Iterator<FieldDoc> it = Arrays.asList(parameterType.enumConstants()).iterator();
                        for (;;) {
                            suffix += "<code>" + it.next().name() + "</code>";
                            if (!it.hasNext()) break;
                            suffix += "|";
                        }
                        continue;
                    }

                    // As a last resort, use the parameter name.
                    suffix += (
                        " <var>"
                        + Notations.fromCamelCase(p.name()).toLowerCaseHyphenated()
                        + "</var>"
                    );
                }

                // Examine the "cardinality" element of the "@CommandLineOption" annotation.
                Cardinality cardinality = Cardinality.OPTIONAL;
                for (ElementValuePair ev : cload.elementValues()) {
                    if (ev.element().name().equals("cardinality")) {
                        cardinality = Cardinality.valueOf(((FieldDoc) ev.value().value()).name());
                    }
                }

                switch (cardinality) {
                case OPTIONAL:     break;
                case MANDATORY:    suffix += " (mandatory)";           break;
                case ONCE_OR_MORE: suffix += " (once or multiple)";    break;
                case ANY:          suffix += " (may appear multiply)"; break;
                default:           throw new AssertionError(cardinality);
                }

                for (Tag commentTag : md.tags(MainDoclet.BT_main_commandLineOptionComment)) {
                    suffix += ' ' + this.fromTags(commentTag.firstSentenceTags(), md, rootDoc);
                }

                for (String name : names) {
                    if (!name.startsWith("-")) {
                        name = (name.length() == 1 ? "-" : "--") + name;
                    }
                    out.append("  <dt><code>" + name + "</code>" + suffix + "</dt>\n");
                }
            }
        };

        try {
            return html.fromTags(doc.inlineTags(), doc, rootDoc);
        } catch (Longjump e) {
            return "";
        }
    }

    /**
     * @return {@code null} iff the <var>source</var> is not a companion of another attribute of the same class
     */
    @Nullable protected static MethodDoc
    getCompanionOf(MethodDoc source, RootDoc rootDoc) throws Longjump {

        if (source.inlineTags().length != 0) return null;

        SeeTag seeTag = (SeeTag) Tags.optionalTag(source, "see", rootDoc);
        if (seeTag == null) return null;

        Doc target = Html.targetOfSeeTag(seeTag);
        if (
            target instanceof MethodDoc
            && Annotations.get((MethodDoc) target, CommandLineOption.class, rootDoc) != null
        ) return (MethodDoc) target;

        return null;
    }
}
