package com.codeforces.commons.text;

import org.junit.Test;

import static org.junit.Assert.*;

public class PatternsTest {
    private static final String JAVA_PUBLIC_STATIC_VOID_MAIN_PUBLIC_CLASS =
            "import java.util.*;\n" +
            "\n" +
            "public class Abc {\n" +
            "    public static void main(String[] args) {\n" +
            "        // No operations\n" +
            "    }\n" +
            "}\n";

    private static final String JAVA_PUBLIC_STATIC_VOID_MAIN_PACKAGE_PRIVATE_CLASS =
            "import java.util.*;\n" +
            "\n" +
            "class Abc {\n" +
            "    public static void main(String[] args) {\n" +
            "        // No operations\n" +
            "    }\n" +
            "}\n";

    private static final String JAVA_PUBLIC_STATIC_VOID_MAIN_PUBLIC_FINAL_CLASS =
            "import java.util.*;\n" +
            "\n" +
            "public class Abc {\n" +
            "    public static void main(String[] args) {\n" +
            "        // No operations\n" +
            "    }\n" +
            "}\n";

    private static final String JAVA_PUBLIC_STATIC_VOID_MAIN_PACKAGE_PRIVATE_FINAL_CLASS =
            "import java.util.*;\n" +
            "\n" +
            "class Abc {\n" +
            "    public static void main(String[] args) {\n" +
            "        // No operations\n" +
            "    }\n" +
            "}\n";

    private static final String SCALA_OK =
            "object Hello {\n" +
            "    def main(args: Array[String]) = {\n" +
            "        println(\"Hello, world\")\n" +
            "    }\n" +
            "}";

    private static final String SCALA_FAIL =
            "object {\n" +
            "    def main(args: Array[String]) = {\n" +
            "        println(\"Hello, world\")\n" +
            "    }\n" +
            "}";

    @Test
    public void testJavaOk() {
        assertTrue(Patterns.JAVA_SOURCE_PATTERN.matcher(JAVA_PUBLIC_STATIC_VOID_MAIN_PUBLIC_CLASS).matches());
        assertTrue(Patterns.JAVA_SOURCE_PATTERN.matcher(JAVA_PUBLIC_STATIC_VOID_MAIN_PUBLIC_FINAL_CLASS).matches());
    }

    @Test
    public void testJavaFail() {
        assertFalse(Patterns.JAVA_SOURCE_PATTERN.matcher(JAVA_PUBLIC_STATIC_VOID_MAIN_PACKAGE_PRIVATE_CLASS).matches());
        assertFalse(Patterns.JAVA_SOURCE_PATTERN.matcher(JAVA_PUBLIC_STATIC_VOID_MAIN_PACKAGE_PRIVATE_FINAL_CLASS).matches());
    }

    @Test
    public void testScalaOk() {
        assertTrue(Patterns.SCALA_SOURCE_PATTERN.matcher(SCALA_OK).matches());
    }

    @Test
    public void testScalaFail() {
        assertFalse(Patterns.SCALA_SOURCE_PATTERN.matcher(SCALA_FAIL).matches());
    }
}
