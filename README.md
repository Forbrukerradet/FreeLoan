[ ![Download](https://api.bintray.com/packages/finansportalen/maven/no.finansportalen%3Afree-loan/images/download.svg) ](https://bintray.com/finansportalen/maven/no.finansportalen%3Afree-loan/_latestVersion)

# FreeLoan
Finansportalen.no's calculation software of effective interest rate

# Maven
To include `free-loan` librray to your Maven project:

1. Add following repository to your Maven configuration:

```
<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>bintray-finansportalen-maven</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/finansportalen/maven</url>
</repository>
```

2. Add dependency to `free-loan`:

```
<dependency>
    <groupId>no.finansportalen</groupId>
    <artifactId>free-loan</artifactId>
    <version>1.0.0</version>
</dependency>
```