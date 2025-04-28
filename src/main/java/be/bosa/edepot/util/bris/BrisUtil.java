package be.bosa.edepot.util.bris;

import jakarta.activation.DataHandler;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.NoArgsConstructor;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogManager;
import javax.xml.catalog.CatalogResolver;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BrisUtil {
    private static final String CATALOG_PATH = "/catalog/bris-catalog.xml";
    private static final SAXParserFactory SAX_FACTORY;
    private static final CatalogResolver CATALOG_RESOLVER;

    static {
        try {
            SAX_FACTORY = SAXParserFactory.newInstance();
            SAX_FACTORY.setNamespaceAware(true);

            URL url = BrisUtil.class.getResource(CATALOG_PATH);
            if (url != null) {
                URI catalogUri = url.toURI();
                CATALOG_RESOLVER = CatalogManager.catalogResolver(CatalogFeatures.defaults(), catalogUri);
            } else {
                throw new RuntimeException(String.format("Cannot initialize CatalogResolver in BrisStaticUtil: catalog file not found on path: %s", CATALOG_PATH));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize BrisStaticUtil", e);
        }
    }

    public static <T> T unmarshal(InputStream xmlStream, Class<T> targetClass, JAXBContext jaxbContext) {
        try {
            Unmarshaller unm = jaxbContext.createUnmarshaller();
            XMLReader xmlReader = SAX_FACTORY.newSAXParser().getXMLReader();
            xmlReader.setEntityResolver(CATALOG_RESOLVER);
            SAXSource source = new SAXSource(xmlReader, new InputSource(xmlStream));
            JAXBElement<T> root = unm.unmarshal(source, targetClass);
            return root.getValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to unmarshal XML", e);
        }
    }

    public static void marshal(Object jaxbObject, OutputStream outputStream, JAXBContext jaxbContext) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(jaxbObject, outputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshal JAXB object", e);
        }
    }

    public static String marshalToString(Object jaxbObject, JAXBContext jaxbContext) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(jaxbObject, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshal JAXB object to string", e);
        }
    }

    public static <T> String marshalAsElementToString(T jaxbObject, Class<T> declaredType, QName qName, JAXBContext jaxbContext) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            JAXBElement<T> jaxbElement = new JAXBElement<>(qName, declaredType, jaxbObject);
            StringWriter writer = new StringWriter();
            marshaller.marshal(jaxbElement, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to marshal JAXBElement to string", e);
        }
    }
    public static String getStringFromDataHandler(DataHandler dataHandler) throws IOException {
        try (InputStream is = dataHandler.getInputStream();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            return os.toString(StandardCharsets.UTF_8);
        }
    }
}
