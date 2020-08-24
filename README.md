# maindoc

A tool for generating documentation for a single Java method from doc comments, similar to JAVADOC

Comprises the following artifacts:

* [A plugin for APACHE MAVEN](maindoc-maven-plugin/README.md)
* [A Doclet](maindoc-doclet/README.md)

The MAVEN plugin uses the Doclet to run JAVADOC and generate the MAINDOC.

# Example

The DOC comments in this Java source file

https://svn.code.sf.net/p/loggifier/code/de.unkrig.zz/trunk/zz-patch/src/main/java/de/unkrig/zz/patch/Main.java

produce this HTML document:

http://zz.unkrig.de/maindoc/zz-patch/Main.main(String%5b%5d).html

# Change Log
