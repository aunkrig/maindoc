# maindoc-maven-plugin

The MAVEN plugin for generating documentation for a single Java method from doc comments, similar to JAVADOC

# Goals

## maindoc:maindoc

### Parameters

* File `<destination>` (default = "target/classes")
* List<File> `<sourcepath>` (default = "src/main/java")
* String `<method>` (default = "main(String[])")
* Charset `<docEncoding>`
* String `<charset>`
* String `<doctitle>`
* boolean `<quiet>` (default = false)
* String[] `<packages>`
