To run the examples:
1. Run mvn clean package
2. Run java -Dspring.profiles.active=<profile> -jar target/jeromq-<version>.jar

where:
<profile> - single Spring profile that defines which example to run, see: pl.com.psl.zeromq.jeromq.Profiles
<version> - version from pom.xml, most probably 1.0-SNAPSHOT.

example:
java -Dspring.profiles.active=req-rep -jar target/jeromq-1.0-SNAPSHOT.jar