setlocal enabledelayedexpansion

if not "!JAVA11_64_HOME!"=="" (
    set PATH=!JAVA11_64_HOME!\bin;!PATH!
    set JAVA_HOME=!JAVA11_64_HOME!
)

set JDK_JAVA_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED"

call mvn.cmd -Dfile.encoding=UTF-8 validate
call mvn.cmd -Dfile.encoding=UTF-8 -DcreateChecksum=true clean source:jar javadoc:jar repository:bundle-create install --batch-mode %*
