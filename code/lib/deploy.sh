
cd $(dirname $0)

VERSION="1.2-NX5-SNAPSHOT"
mvn deploy:deploy-file \
    -DgroupId="org.outerj.daisy" \
    -DartifactId="daisydiff" \
    -Dversion="$VERSION" \
    -Dpackaging="jar" \
    -Dfile="daisydiff-$VERSION.jar" \
    -DrepositoryId="github" \
    -Durl="https://maven.pkg.github.com/Codeforces/codeforces-commons"

VERSION="5.0.2-SNAPSHOT"
mvn deploy:deploy-file \
    -DgroupId="com.google.inject" \
    -DartifactId="guice" \
    -Dversion="$VERSION" \
    -Dpackaging="jar" \
    -Dfile="guice-$VERSION.jar" \
    -DrepositoryId="github" \
    -Durl="https://maven.pkg.github.com/Codeforces/codeforces-commons"