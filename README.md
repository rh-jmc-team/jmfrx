# JMFRX - A bridge for capturing JMX data with JDK Flight Recorder

The JMFRX project allows to periodically capture JMX MBeans and emit a corresponding JDK Flight Recorder (JFR) event.
This allows to

* Access JMX data from offline JFR recording files in situations where you cannot directly connect to JMX
* Emit JMX data via the JFR event streaming API ([JEP 349](https://openjdk.java.net/jeps/349))

## Usage

This project requires OpenJDK 11 or later at runtime.

Tbd.

## Build

This project requires OpenJDK 14 or later for its build.
Apache Maven is used for the build.
Run the following to build the project:

```shell
mvn clean install
```

## License

This code base is available ander the Apache License, version 2.
