package be.bosa.edepot.util.bris;

import org.apache.cxf.common.xmlschema.LSInputImpl;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;


public class CatalogLsResourceResolver implements LSResourceResolver {
    private final CatalogResolver catalogResolver;

    public CatalogLsResourceResolver(CatalogResolver catalogResolver) {
        this.catalogResolver = catalogResolver;
    }

    @Override
    public LSInput resolveResource(
            String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            systemId = null;
            publicId = namespaceURI;
            InputSource inputSource = catalogResolver.resolveEntity(publicId, systemId);
            return new LSInputImpl(publicId, systemId, inputSource.getByteStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve resource", e);
        }
    }
}