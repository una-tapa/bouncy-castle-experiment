# Reported Issue

- AES encryption is not working on OpenLiberty with Temurin V17
- The same encryption code works with Temurin V17 without Liberty


## Java standalone program 

`SampleAES.java` was created based on the snippet of customer's sample program. It works on OpenJDK 17 as follows. 

- Loading bouncy castle provider using -cp
- Passing in the java.security.properties to override the Java JCE Provider configuration

Example1 (Calling .java directly)
```
$ java -cp bcprov-jdk18on-177.jar -Djava.security.properties==bc.java.security.jasonk SampleAES.java
Calling AES encryption code!
Encryption successful
```
Example2 (Compile and Call .class)
```
$ javac SampleAES.java
$ java -cp ./bcprov-jdk18on-177.jar:. -Djava.security.properties==bc.java.security SampleAES
```
Java version
```
$ java -version
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment Temurin-17.0.9+9 (build 17.0.9+9)
OpenJDK 64-Bit Server VM Temurin-17.0.9+9 (build 17.0.9+9, mixed mode, sharing)
```
If the code does not have access to the Bouncy Castle provider class, the same code shows the following error. `AES SecretKeyFactory not available`
```
$ java SampleAES
Calling AES encryption code!
AES logic threw exception.AES SecretKeyFactory not available
-- Error Stack ---
java.security.NoSuchAlgorithmException: AES SecretKeyFactory not available
        at java.base/javax.crypto.SecretKeyFactory.<init>(SecretKeyFactory.java:118)
        at java.base/javax.crypto.SecretKeyFactory.getInstance(SecretKeyFactory.java:164)
        at SampleAES.doWork(SampleAES.java:49)
        at SampleAES.main(SampleAES.java:31)
--------END Dump stack ---------
```

<!-- 
Reference: [My question thread on #java-at-ibm regarding how to pass in the JVM properties](https://ibm-cloud.slack.com/archives/C59HR9D5X/p1707418678693949) 
-->

## WebApplication

If the customer put the same code into a web application and run on the Liberty, the application does not seem to have access to the Bouncy Castle, even though the same properties are set in [jvm.options](https://github.ibm.com/htakamiy/bouncy-castle-case/blob/main/src/main/liberty/config/jvm.options) as follows: 
```
-Djava.security.properties==/home/hiroko/myGit/htakamiy/bouncy-castle-case/bc.java.security.jasonk
```
and the [server.xml](https://github.ibm.com/htakamiy/bouncy-castle-case/blob/main/src/main/liberty/config/server.xml#L22) has the following configuration:
```
<library id="global">
  <fileset dir="/home/hiroko/myGit/htakamiy/bouncy-castle-case" includes="*.jar" />
</library>

  <application location="guide-security-intro.war" type="war"
               id="guide-security-intro.war"
               name="guide-security-intro.war" context-root="/">
    <!-- tag::application-bnd[] -->
     <classloader commonLibraryRef="global" />
 </application>    
```

The application can be run on Liberty using dev mode: 
```
~/bouncy-castle-case$ mvn liberty:dev
[INFO] Scanning for projects...
[INFO]
[INFO] -------------< io.openliberty.guides:guide-security-intro >-------------
[INFO] Building guide-security-intro 1.0-SNAPSHOT
[INFO] --------------------------------[ war ]---------------------------------
....

[INFO] Source compilation was successful.
[INFO] Tests compilation was successful.
[INFO] [AUDIT   ] CWWKT0017I: Web application removed (default_host): http://172.21.46.155:9080/
[INFO] [AUDIT   ] CWWKZ0009I: The application guide-security-intro.war has stopped successfully.
[INFO] [AUDIT   ] CWWKT0016I: Web application available (default_host): http://172.21.46.155:9080/
[INFO] [AUDIT   ] CWWKZ0003I: The application guide-security-intro.war updated in 0.302 seconds.
```
The web application is accessble using `http://172.21.46.155:9080/` (the ip address is my machine) with userid/password=alice/alicepwd. 

Unfortuantely, the same code run in the application does not seem to have access to the BouncyCastle provider classes. 
```
AES logic threw exception.AES SecretKeyFactory not available

-- Error Stack --- 
java.security.NoSuchAlgorithmException: AES SecretKeyFactory not available at java.base/javax.crypto.SecretKeyFactory.(SecretKeyFactory.java:118) at java.base/javax.crypto.SecretKeyFactory.getInstance(SecretKeyFactory.java:164) at io.openliberty.guides.ui.HomeServlet.doGet(HomeServlet.java:87) at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:527) at jakarta.
....
ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136) at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) at java.base/java.lang.Thread.run(Thread.java:840)
 --------END Dump stack ---------
```
## Current Solution

The curent solution is to [load the BouncyCastle explicitly](https://github.ibm.com/htakamiy/bouncy-castle-case/blob/main/src/main/java/io/openliberty/guides/ui/HomeServlet.java#L184) in the code. 

After the code change, the code ran successfully without exception. 
![webapp](https://github.com/una-tapa/bouncy-castle-experiment/blob/main/BouncyCastleApp.png "Sample WebApplication Output") 

## Reference:
- [Providing global libraries for all Java EE applications](
https://www.ibm.com/docs/en/was-liberty/base?topic=applications-providing-global-libraries-all-java-ee)
- [Stackoverflow Setting Java classpath for Liberty](https://stackoverflow.com/questions/23658494/websphere-liberty-8-5-setting-java-classpath)
- [Bouncy castle installation doc](https://github.com/bcgit/bc-java/wiki/Provider-Installation)
