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

# Example

You can use the plugin in your own projects like this:

    <project ...>
        ...
        <build>
            <plugins>
                ....
                <plugin>
                    <groupId>de.unkrig.maindoc</groupId>
                    <artifactId>maindoc-maven-plugin</artifactId>
                    <version>1.0.6</version>
    
                    <executions><execution><goals><goal>maindoc</goal></goals></execution></executions>
    
                    <configuration>
                        <packages><param>de.unkrig.clodemo</param></packages>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </project>
