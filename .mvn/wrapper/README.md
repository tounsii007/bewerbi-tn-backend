# Maven Wrapper

`maven-wrapper.jar` wird vom `mvnw`/`mvnw.cmd` Skript beim ersten Start automatisch
heruntergeladen (URL steht in `maven-wrapper.properties`). Deshalb ist nur das
Properties-File hier eingecheckt.

Falls Sie es offline brauchen:

```bash
curl -fsSL -o .mvn/wrapper/maven-wrapper.jar \
  https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
```
