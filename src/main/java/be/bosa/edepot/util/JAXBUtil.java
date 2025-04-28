package be.bosa.edepot.util;

import be.belgium.fsb.edepot_v3.prsu100c.Prsu100C;
import be.belgium.fsb.edepot_v3.prsu100u.Prsu100U;
import be.belgium.fsb.edepot_v3.prsu120.Prsu120;
import be.belgium.fsb.edepot_v3.prsu500c.Prsu500C;
import be.belgium.fsb.edepot_v3.prsu500u.Prsu500U;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.crossborder.disclosure.merger.v4_0.CrossBorderMergerDisclosureNotificationTemplate;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Utility class that return static JAXB-related instances that are thread-safe.
 */
public class JAXBUtil {

    private JAXBUtil() {
    }

    // thread-safe and can be initialized only once
    public static final JAXBContext PRSU100C_JAXB_CONTEXT =
            createJaxbContext(Prsu100C.class);
    public static final JAXBContext PRSU100U_JAXB_CONTEXT =
            createJaxbContext(Prsu100U.class);
    public static final JAXBContext PRSU120_JAXB_CONTEXT =
            createJaxbContext(Prsu120.class);
    public static final JAXBContext PRSU500C_JAXB_CONTEXT =
            createJaxbContext(Prsu500C.class);
    public static final JAXBContext PRSU500U_JAXB_CONTEXT =
            createJaxbContext(Prsu500U.class);

    public static final JAXBContext CROSS_BORDER_MERGER_DISCLOSURE_NOTIFICATION_JAXB_CONTEXT =
            createJaxbContext(CrossBorderMergerDisclosureNotificationTemplate.class);
    private static JAXBContext createJaxbContext(Class<?> clazz) {
        try {
            return JAXBContext.newInstance(clazz);
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating JAXBContext for class: " + clazz.getName(), e);
        }
    }
}