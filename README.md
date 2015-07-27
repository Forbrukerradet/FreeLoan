[ ![Download](https://api.bintray.com/packages/finansportalen/maven/no.finansportalen%3Afree-loan/images/download.svg) ](https://bintray.com/finansportalen/maven/no.finansportalen%3Afree-loan/_latestVersion)

# FreeLoan
Finansportalen.no's calculation software of effective interest rate

# Documentation

* [FreeCard](/docs/freeCard.pdf)
* [FreeLoan (short version)](/docs/freeLoan_short.docx)
* [FreeLoan (long version)](/docs/freeLoan_long.docx)

# Maven
To include `free-loan` library to your Maven project:

* Add following repository to your Maven configuration:

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

* Add dependency to `free-loan`:

    ```
    <dependency>
        <groupId>no.finansportalen</groupId>
        <artifactId>free-loan</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```
