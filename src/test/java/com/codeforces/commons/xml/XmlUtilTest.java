package com.codeforces.commons.xml;

import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.process.ThreadUtil;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 08.11.11
 */
public class XmlUtilTest {
    @Test
    public void testIsNode() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            Assert.assertTrue(XmlUtil.isNode(testFile, "/a"));
            Assert.assertTrue(XmlUtil.isNode(testFile, "/a/bs"));
            Assert.assertTrue(XmlUtil.isNode(testFile, "/a/bs/b"));
            Assert.assertFalse(XmlUtil.isNode(testFile, "/bs/b"));
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @Test
    public void testExtractFromXml() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml for XPath '/a/bs/b[1]/@attr1'.",
                    "attr1v1", XmlUtil.extractFromXml(testFile, "/a/bs/b[1]/@attr1", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml for XPath '/a/bs/b[2]/@attr1'.",
                    "attr1v2", XmlUtil.extractFromXml(testFile, "/a/bs/b[2]/@attr1", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml for XPath '/a/bs/b[@attr1='attr1v1']/@attr2'.",
                    "attr2v1", XmlUtil.extractFromXml(testFile, "/a/bs/b[@attr1='attr1v1']/@attr2", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml for XPath '/a/bs/b[@attr1='attr1v1']/@attr3'.",
                    Integer.valueOf(1), XmlUtil.extractFromXml(testFile, "/a/bs/b[@attr1='attr1v1']/@attr3", Integer.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml for XPath '/a/d/@attr6'.",
                    "attr6v", XmlUtil.extractFromXml(testFile, "/a/d/@attr6", String.class)
            );
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @Test
    public void testUpdateXml() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            XmlUtil.updateXml(testFile, "/a/c/@intAttr", "7");
            XmlUtil.updateXml(testFile, "/a/c/@strAttr", "cabaca");
            XmlUtil.updateXml(testFile, "/a/c/@boolAttr", "true");
            XmlUtil.updateXml(testFile, "/a/d/@attr5", "newValue");

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.updateXml for XPath '/a/c/@intAttr'.",
                    Integer.valueOf(7), XmlUtil.extractFromXml(testFile, "/a/c/@intAttr", Integer.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.updateXml for XPath '/a/c/@strAttr'.",
                    "cabaca", XmlUtil.extractFromXml(testFile, "/a/c/@strAttr", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.updateXml for XPath '/a/c/@boolAttr'.",
                    Boolean.TRUE, XmlUtil.extractFromXml(testFile, "/a/c/@boolAttr", Boolean.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.updateXml for XPath '/a/c/@notExistingAttr'.",
                    Boolean.FALSE, XmlUtil.extractFromXml(testFile, "/a/c/@notExistingAttr", Boolean.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.updateXml for XPath '/a/d/@attr5'.",
                    "newValue", XmlUtil.extractFromXml(testFile, "/a/d/@attr5", String.class)
            );
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @Test
    public void testEnsureXmlElementExist() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            Map<String, String> filterAttributes = new HashMap<>();
            filterAttributes.put("foo", "fooValue");
            filterAttributes.put("bar", "barValue");

            XmlUtil.ensureXmlElementExists(testFile, "/a", "e", filterAttributes, filterAttributes, null);

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e'.",
                    1, XmlUtil.extractFromXml(testFile, "/a/e", NodeList.class).getLength()
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e/@foo'.",
                    "fooValue", XmlUtil.extractFromXml(testFile, "/a/e/@foo", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e/@bar'.",
                    "barValue", XmlUtil.extractFromXml(testFile, "/a/e/@bar", String.class)
            );

            Map<String, String> newAttributes = new HashMap<>(filterAttributes);
            newAttributes.put("baz", "bazValue");

            XmlUtil.ensureXmlElementExists(testFile, "/a", "e", filterAttributes, newAttributes, null);

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e'.",
                    1, XmlUtil.extractFromXml(testFile, "/a/e", NodeList.class).getLength()
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e/@foo'.",
                    "fooValue", XmlUtil.extractFromXml(testFile, "/a/e/@foo", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e/@bar'.",
                    "barValue", XmlUtil.extractFromXml(testFile, "/a/e/@bar", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e/@baz'.",
                    "bazValue", XmlUtil.extractFromXml(testFile, "/a/e/@baz", String.class)
            );

            filterAttributes.put("foo", "newFooValue");
            newAttributes.put("foo", "newFooValue");

            XmlUtil.ensureXmlElementExists(testFile, "/a", "e", filterAttributes, newAttributes, null);

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e'.",
                    2, XmlUtil.extractFromXml(testFile, "/a/e", NodeList.class).getLength()
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e[2]/@foo'.",
                    "newFooValue", XmlUtil.extractFromXml(testFile, "/a/e[2]/@foo", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e[2]/@bar'.",
                    "barValue", XmlUtil.extractFromXml(testFile, "/a/e[2]/@bar", String.class)
            );

            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.ensureXmlElementExists for XPath '/a/e[2]/@baz'.",
                    "bazValue", XmlUtil.extractFromXml(testFile, "/a/e[2]/@baz", String.class)
            );
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @SuppressWarnings("MessageMissingOnJUnitAssertion")
    @Test
    public void testEnsureXmlElementExistDoesntRewriteFileIfNoChanges() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml-ext");
            File testFile = new File(tempDir, "test-ext.xml");
            FileUtil.writeFile(testFile, getBytes("test-ext.xml"));
            long lastModified = testFile.lastModified();

            Map<String, String> filterAttributes = new HashMap<>();
            filterAttributes.put("boolAttr", "false");
            filterAttributes.put("strAttr", "");

            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", filterAttributes, null, null);
            Assert.assertEquals(lastModified, testFile.lastModified());

            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", filterAttributes, filterAttributes, null);
            Assert.assertEquals(lastModified, testFile.lastModified());

            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", filterAttributes, null, new TreeSet<>(Collections.singletonList("c")));
            Assert.assertEquals(lastModified, testFile.lastModified());

            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", filterAttributes, null, new TreeSet<>(Collections.singletonList("intAttr")));
            Assert.assertNotEquals(lastModified, testFile.lastModified());
            lastModified = testFile.lastModified();

            Map<String, String> expectedAttributes = new HashMap<>();
            expectedAttributes.put("x", "");
            expectedAttributes.put("notBoolAttr", "false");
            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", filterAttributes, expectedAttributes, null);
            Assert.assertNotEquals(lastModified, testFile.lastModified());
            lastModified = testFile.lastModified();

            ThreadUtil.sleep(1001);
            XmlUtil.ensureXmlElementExists(testFile, "/a", "c", expectedAttributes, null, null);
            Assert.assertEquals(lastModified, testFile.lastModified());
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @Test
    public void testRemoveElementsIfExists() throws IOException {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            XmlUtil.removeElementsIfExists(testFile, "/a/bs/b");
            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.removeElementsIfExists for XPath '/a/bs/b'.",
                    0, XmlUtil.extractFromXml(testFile, "/a/bs/b", NodeList.class).getLength()
            );

            Map<String, String> newAttributes = new HashMap<>();
            newAttributes.put("value", "123");
            XmlUtil.ensureXmlElementExists(testFile, "/a", "e", new HashMap<>(), newAttributes,
                    null);
            newAttributes.put("value2", "1234");
            XmlUtil.ensureXmlElementExists(testFile, "/a", "e", new HashMap<>(), newAttributes,
                    null);
            XmlUtil.removeElementsIfExists(testFile, "/a/e");
            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.removeElementsIfExists for XPath '/a/e'.",
                    0, XmlUtil.extractFromXml(testFile, "/a/e", NodeList.class).getLength()
            );
            XmlUtil.removeElementsIfExists(testFile, "/a/e");
            Assert.assertEquals(
                    "Failed XmlUtil.extractFromXml after XmlUtil.removeElementsIfExists for XPath '/a/e'.",
                    0, XmlUtil.extractFromXml(testFile, "/a/e", NodeList.class).getLength()
            );
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    @SuppressWarnings("MessageMissingOnJUnitAssertion")
    @Test
    public void testConcurrentExtractFromXml() throws Exception {
        File tempDir = null;

        try {
            tempDir = FileUtil.createTemporaryDirectory("test-xml");
            File testFile = new File(tempDir, "test.xml");
            FileUtil.writeFile(testFile, getBytes("test.xml"));

            int concurrency = 4 * Runtime.getRuntime().availableProcessors();
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < concurrency; i++) {
                threads.add(new Thread(() -> {
                    for (int i1 = 0; i1 < 100; i1++) {
                        try {
                            Assert.assertEquals("false", XmlUtil.extractFromXml(testFile, "/a/c/@boolAttr", String.class));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }));
            }

            threads.forEach(Thread::start);

            for (Thread thread : threads) {
                thread.join();
            }
        } finally {
            FileUtil.deleteTotallyAsync(tempDir);
        }
    }

    private static byte[] getBytes(String resourceName) throws IOException {
        InputStream resourceStream = XmlUtilTest.class.getResourceAsStream(
                "/com/codeforces/commons/xml/" + resourceName
        );

        try {
            return IoUtil.toByteArray(resourceStream);
        } finally {
            IoUtil.closeQuietly(resourceStream);
        }
    }
}
