# maindoc-doclet

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  </head>
  <body>
A doclet that generates simple HTML files for a particular method (e.g. "main(String[])") of a set of classes.
<p>
  Generates <code>&lt;dt&gt;</code> / <code>&lt;dd&gt;</code> pairs for all methods that are annotated with <code>&#64;CommandLineOption</code>.
</p>

<h3>Doclet command line options:</h3>

<dl>
<dt><code>-d</code> <var>dest-dir</var></dt>
<dt><code>--destination</code> <var>dest-dir</var></dt>
<dd>
  Where to create the HTML files. The effective name of each file is "<var>dest-dir</var><code>/</code><var>package</var><code>/</code><var>class</var><code>.</code><var>method</var><code>.html</code>". The default destination
directory is "<code>.</code>".
</dd>

<dt><code>--method</code> <var>method</var></dt>
<dd>
  The signature of the method to document. The default is <code>"main(String[])"</code>; for a doclet, e.g., you may
want to specify <code>"--method start(RootDoc)"</code> to document the doclet "main method".
</dd>

<dt><code>--docencoding</code> <var>charset</var></dt>
<dd>
  The charset to use when writing the HTML files. The default is the JVM default charset, "${file.encoding}".
</dd>

<dt><code>--charset</code> <var>name</var></dt>
<dd>
  The HTML character set for this document. If set, then the following tag appears in the <code>&lt;head&gt;</code> of all
generated documents:<br />
<code>&lt;meta http-equiv="Content-Type" content="text/html; charset="</code><var>charset</var><code>"&gt;</code>
</dd>

<dt><code>--doctitle</code> <var>title</var></dt>
<dd>
  The title to place near the top of the output file.
</dd>

<dt><code>--quiet</code></dt>
<dd>
  Suppresses normal output.
</dd>

<dt><code>--windowtitle</code> <var>title</var></dt>
<dt><code>--bottom</code> <var>text</var></dt>
<dt><code>--link</code> <var>ext-doc-url</var></dt>
<dt><code>--linkoffline</code> <var>ext-doc-url</var> <var>package-list-loc</var></dt>
<dd>
  For compatibility with the JAVADOC standard doclet; ignored.
</dd>

</dl>

<h3>Supported inline tags:</h3>

<dl>
  <dt><a name="main.commandLineOptions" /><code>{&#64;main.commandLineOptions}</code></dt>
  <dt><code>{&#64;main.commandLineOptions</code> <var>group-name</var><code>}</code></dt>
  <dd>
    Documentation for all command line options, generated from <code><a href="http://commons.unkrig.de/javadoc/commons-util/de/unkrig/commons/util/annotation/CommandLineOption.html">&#64;CommandLineOption</a></code>-annotated
    setter methods.<br />
    If a <var>group-name</var> is given, then only those options appear which have a
    <a href="#main.commandLineOptionGroup"><code>{&#64;main.commandLineOptionGroup}</code> block tag</a> with
    equal <var>group-name</var>; otherwise, only those options appear which have <em>no</em>
    <code>{&#64;main.commandLineOptionGroup}</code> block tag.
  </dd>
  <dt><code>{&#64;code <var>text</var>}</code></dt>
  <dt><code>{&#64;literal <var>text</var>}</code></dt>
  <dt><code>{&#64;value <var>package</var>.<var>class</var>#<var>field</var>}</code></dt>
  <dt><code>{&#64;link      <var>package</var>.<var>class</var>#<var>member</var> <var>label</var>}</code></dt>
  <dt><code>{&#64;linkplain <var>package</var>.<var>class</var>#<var>member</var> <var>label</var>}</code></dt>
  <dt><code>{&#64;docRoot}</code></dt>
  <dd>
    <a href="http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#javadoctags">Same as for
    the standard doclet</a>.
  </dd>
  <dt><code>{&#64;constantsof      <var>package</var>.<var>enum-type</var>}</code></dt>
  <dt><code>{&#64;constantsofplain <var>package</var>.<var>enum-type</var>}</code></dt>
  <dd>
    Links to all constants of the designated enum type, separated with "<code>, </code>" (a comma and a space).
  </dd>
</dl>

<h3>Supported block tags:</h3>

<dl>
  <dt>
    <a name="main.commandLineOptionGroup" /><code>{&#64;main.commandLineOptionGroup}</code> <var>group-name</var>
  </dt>
  <dd>
    Assigns this command line option setter method to a "group"; see <a
    href="#main.commandLineOptions">the <code>{&#64;main.commandLineOptions}</code> inline tag</a>.
  </dd>
</dl>
  </body>
</html>

(You may have guessed it; the above documentation was generated with MAINDOC itself, by addressing the "start(RootDoc)" method instead of the default "main(String[])".
