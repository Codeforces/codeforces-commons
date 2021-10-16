package com.codeforces.commons.xml;

import com.codeforces.commons.io.ByteArrayOutputStream;
import com.codeforces.commons.io.FileUtil;
import com.codeforces.commons.io.IoUtil;
import com.codeforces.commons.process.ThreadUtil;
import com.codeforces.commons.text.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Maxim Shipko (sladethe@gmail.com)
 * Date: 14.09.11
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class XmlUtil {
    private static final Lock factoryLock = new ReentrantLock();
    private static final Lock expressionLock = new ReentrantLock();

    private XmlUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses XML string and extracts value.
     *
     * @param xmlFile XML to be scanned.
     * @param xPath   XPath expression.
     * @param clazz   {@link Boolean}, {@link String}, {@link Integer}, {@link Double},
     *                {@link NodeList} and {@link Node} classes are supported now.
     * @param <T>     Return type.
     * @return Return value.
     * @throws IOException In case of I/O error.
     */
    public static <T> T extractFromXml(@Nonnull File xmlFile, String xPath, Class<T> clazz)
            throws IOException {
        return Objects.requireNonNull(FileUtil.executeIoOperation(new ThreadUtil.Operation<T>() {
            @Nonnull
            @Override
            public T run() throws IOException {
                try {
                    return internalExtractFromXml(new FileInputStream(xmlFile), xPath, clazz);
                } catch (FileNotFoundException e) {
                    throw new IOException(String.format(
                            "Can't find file '%s' while evaluating XPath '%s'.", xmlFile.getCanonicalPath(), xPath
                    ), e);
                }
            }
        }));
    }

    /**
     * Parses XML string and extracts value.
     *
     * @param xmlInputStream XML to be scanned.
     * @param xPath          XPath expression.
     * @param clazz          {@link Boolean}, {@link String}, {@link Integer}, {@link Double},
     *                       {@link NodeList} and {@link Node} classes are supported now.
     * @param <T>            Return type.
     * @return Return value.
     * @throws IOException In case of I/O error.
     */
    @Nonnull
    public static <T> T extractFromXml(InputStream xmlInputStream, String xPath, Class<T> clazz)
            throws IOException {
        return Objects.requireNonNull(FileUtil.executeIoOperation(new ThreadUtil.Operation<T>() {
            @Nonnull
            @Override
            public T run() throws IOException {
                return internalExtractFromXml(xmlInputStream, xPath, clazz);
            }
        }, 1));
    }

    /**
     * Checks if xml contains a node specified by xPath.
     *
     * @param xmlInputStream XML to be scanned.
     * @param xPath XPath expression.
     * @return {@code true} if only if node exists.
     */
    public static boolean isNode(@Nonnull InputStream xmlInputStream, @Nonnull String xPath) {
        try {
            internalExtractFromXml(xmlInputStream, xPath, Node.class);
        } catch (IOException ignored) {
            return false;
        }

        return true;
    }

    /**
     * Checks if xml contains a node specified by xPath.
     *
     * @param xmlFile XML file to be scanned.
     * @param xPath XPath expression.
     * @return {@code true} if only if node exists.
     */
    public static boolean isNode(@Nonnull File xmlFile, @Nonnull String xPath) throws IOException {
        byte[] inputBytes = FileUtil.getBytes(xmlFile);
        InputStream xmlInputStream = new ByteArrayInputStream(inputBytes);

        try {
            internalExtractFromXml(xmlInputStream, xPath, Node.class);
        } catch (IOException ignored) {
            return false;
        }

        return true;
    }

    /**
     * Writes XML document into file.
     *
     * @param xmlFile  File to write.
     * @param document XML document.
     * @throws IOException In case of I/O error.
     */
    public static void writeXml(@Nonnull File xmlFile, @Nonnull Document document) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                try {
                    internalWriteXml(new FileOutputStream(xmlFile), document);
                } catch (FileNotFoundException e) {
                    throw new IOException(
                            "Can't find file '" + xmlFile.getName() + "' while writing XML document.", e
                    );
                }
                return null;
            }
        });
    }

    /**
     * Writes XML document into file.
     *
     * @param xmlOutputStream Stream to write.
     * @param document        XML document.
     * @throws IOException In case of I/O error.
     */
    public static void writeXml(OutputStream xmlOutputStream, @Nonnull Document document) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                internalWriteXml(xmlOutputStream, document);
                return null;
            }
        }, 1);
    }

    /**
     * Changes the value describing by XPath to specific value and updates file.
     *
     * @param xmlFile Which will read first and updated later.
     * @param xPath   XPath to find specific Node.
     * @param value   Value to be set for found node.
     * @throws IOException In case of I/O error.
     */
    public static void updateXml(@Nonnull File xmlFile, String xPath, String value) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                try {
                    byte[] inputBytes = FileUtil.getBytes(xmlFile);
                    ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(inputBytes);
                    ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();

                    internalUpdateXml(xmlInputStream, xmlOutputStream, xPath, value);

                    byte[] outputBytes = xmlOutputStream.toByteArray();
                    if (!Arrays.equals(inputBytes, outputBytes)) {
                        FileUtil.writeFile(xmlFile, outputBytes);
                    }
                } catch (IOException e) {
                    throw new IOException(String.format(
                            "Can't find, read or update file '%s' while evaluating XPath '%s'.",
                            xmlFile.getName(), xPath
                    ), e);
                }
                return null;
            }
        });
    }

    /**
     * Changes the value describing by XPath to specific value and writes modified XML document into output stream.
     *
     * @param xmlInputStream  Stream to read.
     * @param xmlOutputStream Stream to write.
     * @param xPath           XPath to find specific Node.
     * @param value           Value to be set for found node.
     * @throws IOException In case of I/O error.
     */
    public static void updateXml(
            InputStream xmlInputStream, OutputStream xmlOutputStream, String xPath, String value)
            throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                internalUpdateXml(xmlInputStream, xmlOutputStream, xPath, value);
                return null;
            }
        }, 1);
    }

    /**
     * Changes the inner text of an XML-element described by XPath to specific value and updates file.
     *
     * @param xmlFile Which will read first and updated later.
     * @param xPath   XPath to find specific {@code {@link Element }}.
     * @param value   New text value.
     * @throws IOException In case of I/O error.
     */
    public static void updateText(@Nonnull File xmlFile, String xPath, @Nullable String value)
            throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                try {
                    byte[] inputBytes = FileUtil.getBytes(xmlFile);
                    ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(inputBytes);
                    ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();

                    internalUpdateText(xmlInputStream, xmlOutputStream, xPath, value);

                    byte[] outputBytes = xmlOutputStream.toByteArray();
                    if (!Arrays.equals(inputBytes, outputBytes)) {
                        FileUtil.writeFile(xmlFile, outputBytes);
                    }
                } catch (IOException e) {
                    throw new IOException(String.format(
                            "Can't find, read or update file '%s' while evaluating XPath '%s'.",
                            xmlFile.getName(), xPath
                    ), e);
                }
                return null;
            }
        });
    }

    /**
     * Changes the inner text of an XML-element described by XPath to specific value
     * and writes modified XML document into output stream.
     *
     * @param xmlInputStream  Stream to read.
     * @param xmlOutputStream Stream to write.
     * @param xPath           XPath to find specific {@code {@link Element }}.
     * @param value           New text value.
     * @throws IOException In case of I/O error.
     */
    public static void updateText(
            InputStream xmlInputStream, OutputStream xmlOutputStream, String xPath,
            @Nullable String value) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                internalUpdateText(xmlInputStream, xmlOutputStream, xPath, value);
                return null;
            }
        }, 1);
    }

    /**
     * Ensures that XML-element with {@code newAttributes} does exist and creates it if not.
     * <p>&nbsp;</p>
     * Method uses {@code filterAttributes} to uniquely identify an XML-element.
     * If such element does exist, all its attributes will be overridden with values of {@code newAttributes},
     * else a new element will be created.
     *
     * @param xmlFile            Which will read first and updated later.
     * @param parentElementXPath XPath to find element that should contain specified element.
     * @param elementName        Name of the element to create.
     * @param filterAttributes   Collection of attributes which allows to uniquely identify an XML-element.
     * @param newAttributes      Collection of attributes which an XML-element should have
     *                           or {@code null} if {@code filterAttributes} should be considered
     *                           also as {@code newAttributes}.
     * @param obsoleteAttributes Collection of attribute names which should be removed from the element
     *                           or {@code null} if no such action is required.
     * @throws IOException In case of I/O error.
     */
    public static void ensureXmlElementExists(
            @Nonnull File xmlFile, @Nonnull String parentElementXPath, @Nonnull String elementName,
            @Nonnull Map<String, String> filterAttributes, @Nullable Map<String, String> newAttributes,
            @Nullable Set<String> obsoleteAttributes) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                try {
                    byte[] inputBytes = FileUtil.getBytes(xmlFile);
                    ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(inputBytes);
                    ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();

                    internalEnsureXmlElementExists(
                            false, xmlInputStream, xmlOutputStream,
                            parentElementXPath, elementName, filterAttributes, newAttributes, obsoleteAttributes
                    );

                    byte[] outputBytes = xmlOutputStream.toByteArray();
                    if (outputBytes.length > 0) {
                        FileUtil.writeFile(xmlFile, outputBytes);
                    }
                } catch (IOException e) {
                    throw new IOException(String.format(
                            "Can't find, read or update file '%s' while evaluating XPath '%s'.",
                            xmlFile.getName(), parentElementXPath
                    ), e);
                }
                return null;
            }
        });
    }

    /**
     * Ensures that XML-element with {@code newAttributes} does exist and creates it if not.
     * <p>&nbsp;</p>
     * Method uses {@code filterAttributes} to uniquely identify an XML-element.
     * If such element does exist, all its attributes will be overridden with values of {@code newAttributes},
     * else a new element will be created.
     *
     * @param xmlInputStream     Stream to read.
     * @param xmlOutputStream    Stream to write.
     * @param parentElementXPath XPath to find element that should contain specified element.
     * @param elementName        Name of the element to create.
     * @param filterAttributes   Collection of attributes which allows to uniquely identify an XML-element.
     * @param newAttributes      Collection of attributes which an XML-element should have
     *                           or {@code null} if {@code filterAttributes} should be considered
     *                           also as {@code newAttributes}.
     * @param obsoleteAttributes Collection of attribute names which should be removed from the element
     *                           or {@code null} if no such action is required.
     * @throws IOException In case of I/O error.
     */
    public static void ensureXmlElementExists(
            @Nonnull InputStream xmlInputStream, @Nonnull OutputStream xmlOutputStream,
            @Nonnull String parentElementXPath, @Nonnull String elementName,
            @Nonnull Map<String, String> filterAttributes, @Nullable Map<String, String> newAttributes,
            @Nullable Set<String> obsoleteAttributes) throws IOException {
        FileUtil.executeIoOperation(new ThreadUtil.Operation<Void>() {
            @Nullable
            @Override
            public Void run() throws IOException {
                internalEnsureXmlElementExists(
                        true,
                        xmlInputStream, xmlOutputStream,
                        parentElementXPath, elementName,
                        filterAttributes, newAttributes, obsoleteAttributes
                );
                return null;
            }
        }, 1);
    }

    /**
     * Removes all elements Xml by XPath.
     *
     * @param xmlFile      From which will be removed elements.
     * @param elementXPath XPath of elements to remove.
     * @throws IOException In case of I/O error.
     */
    public static void removeElementsIfExists(@Nonnull File xmlFile, @Nonnull String elementXPath)
            throws IOException {
        try {
            byte[] inputBytes = FileUtil.getBytes(xmlFile);
            ByteArrayInputStream xmlInputStream = new ByteArrayInputStream(inputBytes);
            ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();

            removeElementsIfExists(xmlInputStream, xmlOutputStream, elementXPath);

            byte[] outputBytes = xmlOutputStream.toByteArray();
            if (!Arrays.equals(inputBytes, outputBytes)) {
                FileUtil.writeFile(xmlFile, outputBytes);
            }
        } catch (IOException e) {
            throw new IOException(String.format(
                    "Can't find, read or update file '%s' while evaluating XPath '%s'.", xmlFile.getName(), elementXPath
            ), e);
        }
    }

    /**
     * Removes all elements from Xml by XPath.
     *
     * @param xmlInputStream  Stream to read.
     * @param xmlOutputStream Stream to write.
     * @param elementXPath    XPath of elements to remove.
     * @throws IOException In case of I/O error.
     */
    public static void removeElementsIfExists(
            @Nonnull InputStream xmlInputStream, @Nonnull OutputStream xmlOutputStream,
            @Nonnull String elementXPath) throws IOException {
        internalRemoveElementsIfExists(xmlInputStream, xmlOutputStream, elementXPath);
    }

    @Contract("null -> null; !null -> !null")
    @Nullable
    public static String formatEnumValueForXml(@Nullable Enum enumValue) {
        return enumValue == null ? null : enumValue.name().toLowerCase().replace('_', '-');
    }

    @Contract("null, _ -> null")
    @Nullable
    public static <T extends Enum<T>> T extractEnumValueFromXml(@Nullable String enumFormat, Class<T> enumClass) {
        return StringUtil.isBlank(enumFormat)
                ? null
                : Enum.valueOf(enumClass, enumFormat.toUpperCase().replace('-', '_'));
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private static <T> T internalExtractFromXml(InputStream xmlInputStream, String xPath, Class<T> clazz)
            throws IOException {
        byte[] xmlBytes = IoUtil.toByteArray(xmlInputStream);
        InputStream xmlBytesInputStream = new ByteArrayInputStream(xmlBytes);

        XPath xp = newXPathFactory().newXPath();

        QName type;

        if (clazz == Boolean.class) {
            type = XPathConstants.BOOLEAN;
        } else if (clazz == String.class) {
            type = XPathConstants.STRING;
        } else if (clazz == Integer.class || clazz == Double.class) {
            type = XPathConstants.NUMBER;
        } else if (clazz == NodeList.class) {
            type = XPathConstants.NODESET;
        } else if (clazz == Node.class) {
            type = XPathConstants.NODE;
        } else {
            throw new IllegalArgumentException("Illegal argument 'clazz': '" + clazz + "'.");
        }

        try {
            XPathExpression expression = xp.compile(xPath);

            Object result = evaluateXPath(xmlBytesInputStream, expression, type);
            if (XPathConstants.NUMBER.equals(type) && clazz == Integer.class) {
                result = ((Double) result).intValue();
            }
            return (T) result;
        } catch (XPathException e) {
            String xmlString = new String(xmlBytes, "UTF-8");
            throw new IOException("Can't get xpath \"" + xPath + "\" from \""
                    + StringUtil.shrinkTo(xmlString, 128) + "\".", e);
        } finally {
            IoUtil.closeQuietly(xmlBytesInputStream, xmlInputStream);
        }
    }

    private static void internalWriteXml(OutputStream xmlOutputStream, @Nonnull Document document) throws IOException {
        formatDocument(document);
        Source source = new DOMSource(document);
        try {
            Transformer transformer = newTransformerFactory().newTransformer();
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            StreamResult result = new StreamResult(xmlOutputStream);
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw new IOException("Transformer configuration is illegal.", e);
        } catch (TransformerException e) {
            throw new IOException("Transformer failed.", e);
        } finally {
            IoUtil.closeQuietly(xmlOutputStream);
        }
    }

    private static void internalUpdateXml(
            InputStream xmlInputStream, OutputStream xmlOutputStream, String xPath, String value) throws IOException {
        XPath xp = newXPathFactory().newXPath();

        try {
            XPathExpression root = xp.compile("/");
            Document document = (Document) evaluateXPath(xmlInputStream, root, XPathConstants.NODE);
            XPathExpression nodeXPath = xp.compile(xPath);
            Node node = (Node) evaluateXPath(document, nodeXPath, XPathConstants.NODE);
            node.setNodeValue(value);
            internalWriteXml(xmlOutputStream, document);
        } catch (XPathExpressionException e) {
            throw new IOException("Illegal XPath.", e);
        } finally {
            IoUtil.closeQuietly(xmlInputStream, xmlOutputStream);
        }
    }

    private static void internalUpdateText(
            InputStream xmlInputStream, OutputStream xmlOutputStream, String xPath, @Nullable String value)
            throws IOException {
        XPath xp = newXPathFactory().newXPath();

        try {
            Document document = (Document) evaluateXPath(xmlInputStream, xp.compile("/"), XPathConstants.NODE);
            NodeList nodes = (NodeList) evaluateXPath(document, xp.compile(xPath), XPathConstants.NODESET);

            for (int nodeIndex = nodes.getLength() - 1; nodeIndex >= 0; --nodeIndex) {
                Node node = nodes.item(nodeIndex);

                if (!(node instanceof Element)) {
                    throw new IOException("Node specified by XPath '" + xPath + "' is not an XML-element.");
                }

                Element element = (Element) node;

                if (!hasOnlyTextChildren(element)) {
                    throw new IOException(String.format(
                            "Element specified by XPath '%s' has at least one child which isn't plain text node.",
                            xPath
                    ));
                }

                Node childNode = element.getFirstChild();

                if (value == null) {
                    while (childNode != null) {
                        element.removeChild(childNode);
                        childNode = element.getFirstChild();
                    }
                } else {
                    if (childNode == null) {
                        element.appendChild(element.getOwnerDocument().createTextNode(value));
                    } else {
                        ((Text) childNode).replaceWholeText(value);
                    }
                }
            }

            internalWriteXml(xmlOutputStream, document);
        } catch (XPathExpressionException e) {
            throw new IOException("Illegal XPath.", e);
        } finally {
            IoUtil.closeQuietly(xmlInputStream, xmlOutputStream);
        }
    }

    /**
     * Ensures that XML-element with {@code newAttributes} does exist and creates it if not.
     * <p>
     * Method uses {@code filterAttributes} to uniquely identify an XML-element.
     * If such element does exist, all its attributes will be overridden with values of {@code newAttributes},
     * else a new element will be created.
     *
     * @param writeOutputIfNoChanges The flag that indicates that method should write XML-data to output stream
     *                               if no changes was made.
     * @param xmlInputStream         Stream to read.
     * @param xmlOutputStream        Stream to write.
     * @param parentElementXPath     XPath to find element that should contain specified element.
     * @param elementName            Name of the element to create.
     * @param filterAttributes       Collection of attributes which allows to uniquely identify an XML-element.
     * @param newAttributes          Collection of attributes which an XML-element should have
     *                               or {@code null} if {@code filterAttributes} should be considered
     *                               also as {@code newAttributes}.
     * @param obsoleteAttributes     Collection of attribute names which should be removed from the element
     *                               or {@code null} if no such action is required.
     * @throws IOException In case of I/O error.
     */
    @SuppressWarnings({"OverlyLongMethod", "OverlyComplexMethod"})
    private static void internalEnsureXmlElementExists(
            boolean writeOutputIfNoChanges,
            @Nonnull InputStream xmlInputStream, @Nonnull OutputStream xmlOutputStream,
            @Nonnull String parentElementXPath, @Nonnull String elementName,
            @Nonnull Map<String, String> filterAttributes, @Nullable Map<String, String> newAttributes,
            @Nullable Set<String> obsoleteAttributes) throws IOException {
        XPath xp = newXPathFactory().newXPath();

        try {
            // Load DOM-document, compile and execute XPath expression.
            XPathExpression root = xp.compile("/");
            Document document = (Document) evaluateXPath(xmlInputStream, root, XPathConstants.NODE);
            XPathExpression parentNodeXPath = xp.compile(parentElementXPath);
            Node parentNode = (Node) evaluateXPath(document, parentNodeXPath, XPathConstants.NODE);

            NodeList childNodes = parentNode.getChildNodes();
            Element element = null;

            // Search for element with specified characteristics.
            for (int childIndex = 0, childCount = childNodes.getLength(); childIndex < childCount; ++childIndex) {
                Node childNode = childNodes.item(childIndex);

                if (!elementName.equals(childNode.getNodeName())) {
                    continue;
                }

                NamedNodeMap actualAttributes = childNode.getAttributes();
                if (actualAttributes == null) {
                    continue;
                }

                boolean matches = true;

                for (Map.Entry<String, String> filterAttribute : filterAttributes.entrySet()) {
                    Node actualAttribute = actualAttributes.getNamedItem(filterAttribute.getKey());
                    if (actualAttribute == null || !filterAttribute.getValue().equals(actualAttribute.getNodeValue())) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    element = (Element) childNode;
                    break;
                }
            }

            boolean changed = false;

            // Create new element if not found.
            if (element == null) {
                changed = true;
                element = document.createElement(elementName);
                parentNode.appendChild(element);
            }

            Map<String, String> existingAttributes = new HashMap<>();
            NamedNodeMap attributes = element.getAttributes();
            int attributeCount = attributes.getLength();

            for (int attributeIndex = 0; attributeIndex < attributeCount; ++attributeIndex) {
                Node item = attributes.item(attributeIndex);
                existingAttributes.put(item.getNodeName(), item.getNodeValue());
            }

            boolean matches = true;
            Map<String, String> expectedAttributes = newAttributes == null ? filterAttributes : newAttributes;

            for (Map.Entry<String, String> entry : expectedAttributes.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if (obsoleteAttributes != null && obsoleteAttributes.contains(name)) {
                    if (existingAttributes.containsKey(name)) {
                        matches = false;
                        break;
                    }
                } else {
                    if (!existingAttributes.containsKey(name)
                            || !StringUtil.equals(value, existingAttributes.get(name))) {
                        matches = false;
                        break;
                    }
                }
            }

            if (matches) {
                if (obsoleteAttributes != null) {
                    for (String attribute : obsoleteAttributes) {
                        if (existingAttributes.containsKey(attribute)) {
                            matches = false;
                            break;
                        }
                    }
                }
            }

            if (!matches) {
                changed = true;

                // Create or update attributes.
                newAttributes = newAttributes == null ? filterAttributes : newAttributes;
                for (Map.Entry<String, String> newAttribute : newAttributes.entrySet()) {
                    element.setAttribute(newAttribute.getKey(), newAttribute.getValue());
                }

                // Remove obsolete attributes.
                if (obsoleteAttributes != null) {
                    for (String obsoleteAttribute : obsoleteAttributes) {
                        element.removeAttribute(obsoleteAttribute);
                    }
                }
            }

            if (changed || writeOutputIfNoChanges) {
                // Save DOM-document.
                internalWriteXml(xmlOutputStream, document);
            }
        } catch (XPathExpressionException e) {
            throw new IOException("Illegal XPath.", e);
        } finally {
            IoUtil.closeQuietly(xmlInputStream, xmlOutputStream);
        }
    }

    /**
     * Removes all elements from Xml by XPath.
     *
     * @param xmlInputStream  Stream to read.
     * @param xmlOutputStream Stream to write.
     * @param elementXPath    XPath of elements to remove.  @throws IOException In case of I/O error.
     */
    private static void internalRemoveElementsIfExists(
            @Nonnull InputStream xmlInputStream, @Nonnull OutputStream xmlOutputStream,
            @Nonnull String elementXPath) throws IOException {
        XPath xp = newXPathFactory().newXPath();

        try {
            XPathExpression root = xp.compile("/");
            Document document = (Document) evaluateXPath(xmlInputStream, root, XPathConstants.NODE);

            XPathExpression parentNodeXPath = xp.compile(elementXPath);
            NodeList nodeList = (NodeList) evaluateXPath(document, parentNodeXPath, XPathConstants.NODESET);

            for (int nodeIndex = 0; nodeIndex < nodeList.getLength(); nodeIndex++) {
                Node node = nodeList.item(nodeIndex);
                node.getParentNode().removeChild(node);
            }

            internalWriteXml(xmlOutputStream, document);
        } catch (XPathExpressionException e) {
            throw new IOException("Illegal XPath.", e);
        } finally {
            IoUtil.closeQuietly(xmlInputStream, xmlOutputStream);
        }
    }

    @SuppressWarnings({"HardcodedLineSeparator"})
    private static void formatDocument(@Nonnull Document document) {
        formatElement(document, document.getDocumentElement(), 1);
        formatElementEnd(document, document.getDocumentElement(), "\n");
    }

    @SuppressWarnings({"ChainOfInstanceofChecks", "HardcodedLineSeparator", "OverlyComplexMethod"})
    private static void formatElement(@Nonnull Document document, @Nonnull Element element, int depth) {
        String formatString = '\n' + StringUtils.repeat("    ", depth);

        Node node = element.getFirstChild();

        while (node != null) {
            if (node instanceof Element) {
                formatElement(document, (Element) node, depth + 1);

                formatElementStart(document, (Element) node, formatString);
                formatElementEnd(document, (Element) node, formatString);
            } else if (node instanceof Text) {
                Text textNode = (Text) node;
                String text = textNode.getWholeText();

                if (hasOnlyTextSiblings(textNode)) {
                    String trimmedText = text.trim();
                    if (!trimmedText.equals(text)) {
                        textNode.replaceWholeText(trimmedText);
                    }
                } else {
                    if (!text.trim().isEmpty()) {
                        textNode.replaceWholeText(formatString + StringUtil.trimLeft(text));
                    }
                }
            }

            node = node.getNextSibling();
        }
    }

    @SuppressWarnings({"ChainOfInstanceofChecks"})
    private static void formatElementStart(
            @Nonnull Document document, @Nonnull Element element, @Nonnull String formatString) {
        Node previousSibling = element.getPreviousSibling();

        if (previousSibling == null || previousSibling instanceof Element) {
            element.getParentNode().insertBefore(document.createTextNode(formatString), element);
        } else if (previousSibling instanceof Text) {
            Text textNode = (Text) previousSibling;
            String text = textNode.getWholeText();

            if (!formatString.equals(text)) {
                textNode.replaceWholeText(StringUtil.trimRight(text) + formatString);
            }
        }
    }

    @SuppressWarnings({"ChainOfInstanceofChecks"})
    private static void formatElementEnd(
            @Nonnull Document document, @Nonnull Element element, @Nonnull String formatString) {
        Node lastChild = element.getLastChild();

        if (lastChild != null) {
            if (lastChild instanceof Element) {
                element.appendChild(document.createTextNode(formatString));
            } else if (lastChild instanceof Text) {
                Text textNode = (Text) lastChild;
                String text = textNode.getWholeText();

                if (hasOnlyTextSiblings(textNode)) {
                    String trimmedText = text.trim();
                    if (!trimmedText.equals(text)) {
                        textNode.replaceWholeText(trimmedText);
                    }
                } else {
                    if (!formatString.equals(text)) {
                        textNode.replaceWholeText(StringUtil.trimRight(text) + formatString);
                    }
                }
            }
        }
    }

    public static boolean hasOnlyTextSiblings(@Nonnull Node node) {
        Node leftSibling = node.getPreviousSibling();

        while (leftSibling != null) {
            if (!(leftSibling instanceof Text)) {
                return false;
            }

            leftSibling = leftSibling.getPreviousSibling();
        }

        Node rightSibling = node.getNextSibling();

        while (rightSibling != null) {
            if (!(rightSibling instanceof Text)) {
                return false;
            }

            rightSibling = rightSibling.getNextSibling();
        }

        return true;
    }

    public static boolean hasOnlyTextChildren(@Nonnull Node node) {
        Node childNode = node.getFirstChild();

        while (childNode != null) {
            if (!(childNode instanceof Text)) {
                return false;
            }

            childNode = childNode.getNextSibling();
        }

        return true;
    }

    @Nonnull
    public static Document newDomDocument() throws IOException {
        try {
            DocumentBuilderFactory factory = newDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException("Can't create new DOM-document.", e);
        }
    }

    public static XPathFactory newXPathFactory() {
        factoryLock.lock();
        try {
            return XPathFactory.newInstance();
        } finally {
            factoryLock.unlock();
        }
    }

    public static TransformerFactory newTransformerFactory() {
        factoryLock.lock();
        try {
            return TransformerFactory.newInstance();
        } finally {
            factoryLock.unlock();
        }
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        factoryLock.lock();
        try {
            return DocumentBuilderFactory.newInstance();
        } finally {
            factoryLock.unlock();
        }
    }

    public static Object evaluateXPath(InputStream xmlInputStream, @Nonnull XPathExpression xPath, QName returnType)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(new InputSource(xmlInputStream), returnType);
        } finally {
            expressionLock.unlock();
        }
    }

    public static Object evaluateXPath(Document document, @Nonnull XPathExpression xPath, QName returnType)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(document, returnType);
        } finally {
            expressionLock.unlock();
        }
    }

    public static Object evaluateXPath(Element element, @Nonnull XPathExpression xPath, QName returnType)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(element, returnType);
        } finally {
            expressionLock.unlock();
        }
    }

    public static Object evaluateXPath(InputStream xmlInputStream, @Nonnull XPathExpression xPath)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(new InputSource(xmlInputStream));
        } finally {
            expressionLock.unlock();
        }
    }

    public static Object evaluateXPath(Document document, @Nonnull XPathExpression xPath)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(document);
        } finally {
            expressionLock.unlock();
        }
    }

    public static Object evaluateXPath(Element element, @Nonnull XPathExpression xPath)
            throws XPathExpressionException {
        expressionLock.lock();
        try {
            return xPath.evaluate(element);
        } finally {
            expressionLock.unlock();
        }
    }

    public static Map<String, String> toMap(String xml) throws XmlException {
        SAXParserFactory fabrique = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = fabrique.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Can't create SAX parser.", e);
        }
        ToMapHandler handler = new ToMapHandler();
        try {
            parser.parse(new InputSource(new StringReader(xml)), handler);
        } catch (Exception e) {
            throw new XmlException("Can't parse XML.", e);
        }
        return handler.getMap();
    }

    private static class ToMapHandler extends DefaultHandler {
        private final Stack<String> tags = new Stack<>();
        private final Map<String, String> map = new HashMap<>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            StringBuilder tag = new StringBuilder(qName);

            if (attributes != null && attributes.getLength() > 0) {
                Map<String, String> attrs = new TreeMap<>();
                int length = attributes.getLength();
                for (int i = 0; i < length; i++) {
                    attrs.put(attributes.getQName(i), attributes.getValue(i));
                }
                boolean first = true;
                for (Map.Entry<String, String> e : attrs.entrySet()) {
                    tag.append(first ? '@' : '&');
                    tag.append(e.getKey()).append('=').append(e.getValue());
                    first = false;
                }
            }

            tags.add(tag.toString());
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            boolean hasContent = false;
            if (ch != null) {
                for (int i = 0; i < length; i++) {
                    if (!Character.isWhitespace(ch[start + i])) {
                        hasContent = true;
                        break;
                    }
                }
            }
            if (hasContent) {
                String content = new String(ch, start, length).trim();
                StringBuilder key = new StringBuilder();
                for (String tag : tags) {
                    if (key.length() > 0) {
                        key.append('/');
                    }
                    key.append(tag);
                }
                if (map.containsKey(key.toString())) {
                    map.put(key.toString(), map.get(key.toString()) + content);
                } else {
                    map.put(key.toString(), content);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            tags.pop();
        }

        @Override
        public void endDocument() {
            // No operations.
        }

        public Map<String, String> getMap() {
            return map;
        }
    }

    public static final class XmlException extends Exception {
        private XmlException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
