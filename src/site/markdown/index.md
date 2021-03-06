#Overview

Mellowtech Core is a set of components that we use for working with disc based
and byte based manipulation of Objects. It is typically useful for any scenario
that involves

* a need for storing your collections on disc
* sorting large collections of objects that will not fit in memory
* consistent access to files that contains objects
* fast transformation of objects from and to byte representations (instead of using normal
  Java Serialization)  
  

In our own work we have used this library mainly for developing search engines.

The library itself was initially created by Martin Svensson and Rickard Cöster while
working at the Swedish Institue of Computer Science in 2002. Since then it has undergone
a lot of changes and enhancements and is now maintained and developed by mellowtech.org.

##Download

All mellowtech.org APIs can be downloaded from our maven repository
http://www.mellowtech.org/nexus/content/repositories/snapshots

```
<repository>
   <id>mellowtech-snapshots</id>
   <name>Mellowtech Snapshots</name>
   <url>http://www.mellowtech.org/nexus/content/repositories/snapshots/</url>
</repository>
```

In your <pom.xml> include the following dependency

```
<dependency>
  <groupId>com.mellowtech</groupId>
  <artifactId>core</artifactId>
  <version>0.7-SNAPSHOT</version>
</dependency>
```





