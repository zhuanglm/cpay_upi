## Publish Artifact to https://s01.oss.sonatype.org

### Create local.properties, Ask DevOps for credentials in the file
```
touch CPay_Android/CPay/local.properties
```

### How to change the version number

```
PUBLISH_VERSION in /CPay_Android/CPay/sdk/build.gradle
```

### Publish

```
cd  CPay_Android/CPay/
./gradlew sdk:publishReleasePublicationToSonatypeRepository
```