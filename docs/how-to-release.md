How to release
===============================================

* Write a [release notes](/docs/release-notes.md) and commit whem to master branch.

* Tag `master` branch with release information:

    ```
    git tag -a -m "Release 2.0.0" v2.0.0
    ```

* Push tag to remote repository:

    ```
    git push origin master --tags
    ```

* Checkout a tag:

    ```
    git checkout tags/v2.0.0
    ```

* Update project version to `2.0.0`

    ```
    mvn versions:set -DnewVersion=2.0.0
    ```

* Build project and deploy to local repository

    ```
    mvn clean deploy
    ```

    This will deploy free-loan into directory `target/mvn-repo`


* Create a release from a tag `v2.0.0` in GitHub. Write release notes. Attach an artifact.

* Go to [Bintray maven repository](https://bintray.com/finansportalen/maven/no.finansportalen%3Afree-loan/view) and create new version `2.0.0`.

* zip `target/mvn-repo/no` directory and upload to Bintray version `2.0.0` (make sure to check "explode archive" option).

* Revert project version to snapshot

    ```
    mvn versions:set -DnewVersion=0.0.1-SNAPSHOT
    ```