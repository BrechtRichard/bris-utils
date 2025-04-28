package be.bosa.edepot.util.bris;

import lombok.NoArgsConstructor;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BrisXsdValidator {

    private static final String CATALOG_PATH = "/catalog/bris-catalog.xml";
    private static final CatalogResolver CATALOG_RESOLVER;
    private static final ThreadLocal<SAXParserFactory> SAX_FACTORY = ThreadLocal.withInitial(() -> {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory;
    });

    static {
        URL catalogUrl = BrisXsdValidator.class.getResource(CATALOG_PATH);
        if (catalogUrl == null) {
            throw new ExceptionInInitializerError("Catalog file not found: " + CATALOG_PATH);
        }

        CatalogManager manager = new CatalogManager();
        manager.setCatalogFiles(catalogUrl.toString());
        manager.setUseStaticCatalog(false);
        manager.setIgnoreMissingProperties(true);
        CATALOG_RESOLVER = new CatalogResolver(manager);
    }

    public static void validate(String xmlContent, String xsdPath)
            throws IOException, SAXException, ParserConfigurationException {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new CatalogLsResourceResolver(CATALOG_RESOLVER));

        InputStream xsdStream = BrisXsdValidator.class.getResourceAsStream(xsdPath);
        if (xsdStream == null) {
            throw new FileNotFoundException("XSD file not found in JAR: " + xsdPath);
        }

        StreamSource source = new StreamSource(xsdStream);
        Schema schema = factory.newSchema(source);
        Validator validator = schema.newValidator();

        XMLReader reader = SAX_FACTORY.get().newSAXParser().getXMLReader();
        InputSource inputSource = new InputSource(new StringReader(xmlContent));
        validator.validate(new SAXSource(reader, inputSource));
    }
}