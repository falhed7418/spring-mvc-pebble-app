# Pebble + Spring MVC + Spring Securtiy + EclipseLink

### Keystores:
* `keytool -genkey -alias jetty -keyalg RSA -keystore jettyXml/jetty.keystore -storepass secret -keypass secret -dname "CN=localhost"`
* `keytool -genkey -alias tomcat -keyalg RSA -keystore tomcatXml/tomcat.keystore -storepass secret -keypass secret -dname "CN=localhost"`

### Server start:
* `mvn clean package jetty:run`
* `mvn clean package tomcat7:run`