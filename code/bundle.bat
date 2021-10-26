setlocal enabledelayedexpansion

if not "!JAVA8_64_HOME!"=="" (
    set PATH=!JAVA8_64_HOME!\bin;!PATH!
    set JAVA_HOME=!JAVA8_64_HOME!
)

call mvn.cmd -Dfile.encoding=UTF-8 -DcreateChecksum=true clean source:jar javadoc:jar repository:bundle-create install --batch-mode %*
