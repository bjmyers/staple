# Disable jar because of gradle tooling, see https://github.com/quarkusio/quarkus/discussions/40679
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false