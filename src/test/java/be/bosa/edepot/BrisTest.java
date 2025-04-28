package be.bosa.edepot;

import be.belgium.fsb.edepot_v3.prsu100u.Prsu100U;
import be.bosa.edepot.util.bris.BrisUtil;
import be.bosa.edepot.util.bris.BrisXsdValidator;
import be.bosa.edepot.util.bris.SampleObjectFactory;
import eu.europa.ec.bris.jaxb.br.generic.notification.template.crossborder.disclosure.merger.v4_0.CrossBorderMergerDisclosureNotificationTemplate;
import jakarta.xml.bind.JAXBContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import be.bosa.edepot.util.JAXBUtil;

@ExtendWith(SpringExtension.class)
public class BrisTest {

    @Test
    @SneakyThrows
    void givenACrossBorderMergerTemplateNotification_whenUnmarshalled_ThenJavaModelFilledIn() {
        // Prepare
        String notificationTemplateXml = buildXml("classpath:/data/bris/crossBorderMergerDisclosureNotificationTemplate_v1.xml");
        JAXBContext jaxbContext = JAXBUtil.CROSS_BORDER_MERGER_DISCLOSURE_NOTIFICATION_JAXB_CONTEXT;

        // Test
        CrossBorderMergerDisclosureNotificationTemplate crossBorderMergerDisclosureNotificationTemplate
                = BrisUtil.unmarshal(new ByteArrayInputStream(notificationTemplateXml.getBytes()), CrossBorderMergerDisclosureNotificationTemplate.class, jaxbContext);

        // Assert
        assertNotNull(crossBorderMergerDisclosureNotificationTemplate.getCrossBorderCompanyData());
        assertEquals("BE", crossBorderMergerDisclosureNotificationTemplate.getIssuingOrganisation().getBusinessRegisterCountry().getValue());
    }

    @Test
    @SneakyThrows
    void givenACorrectCrossBorderMergerTemplateNotification_whenValidating_thenNoErrorIsThrown() {
        String notificationTemplateXml = buildXml("classpath:/data/bris/crossBorderMergerDisclosureNotificationTemplate_v1.xml");
        assertDoesNotThrow(() -> BrisXsdValidator.validate(
                notificationTemplateXml,
                "/xsd/bris/br/generic/notification/v4_0_0/BR-CrossBorderMergerDisclosureNotificationTemplate.xsd"
        ));
    }

    @Test
    @SneakyThrows
    void givenACrossBorderMergerTemplateNotificationWithIncorrectInformation_whenValidating_thenNoErrorIsThrown() {
        String notificationTemplateXml = buildXml("classpath:/data/bris/crossBorderMergerDisclosureNotificationTemplate_v2_error1.xml");
        assertDoesNotThrow(() -> BrisXsdValidator.validate(
                notificationTemplateXml,
                "/xsd/bris/br/generic/notification/v4_0_0/BR-CrossBorderMergerDisclosureNotificationTemplate.xsd"
        ));
    }


    @Test
    void givenACrossBorderMergerDisclosureNotificationSampleObject_whenMarshalled_thenXmlFileIsNotNull() {
        JAXBContext jaxbContext = JAXBUtil.CROSS_BORDER_MERGER_DISCLOSURE_NOTIFICATION_JAXB_CONTEXT;
        CrossBorderMergerDisclosureNotificationTemplate obj =
                SampleObjectFactory.createSample(CrossBorderMergerDisclosureNotificationTemplate.class);

        QName qName = new QName(
                "http://ec.europa.eu/bris/v4_0/br/CrossBorderMergerDisclosureNotificationTemplate",
                "CrossBorderMergerDisclosureNotificationTemplate"
        );

        String xml = BrisUtil.marshalAsElementToString(obj, CrossBorderMergerDisclosureNotificationTemplate.class, qName, jaxbContext);
        assertNotNull(xml);
    }

    @Test
    void givenACrossBorderMergerDisclosureNotificationXml_whenUnmarshalledAndMarshalledAgain_thenFinalObjectContainsCrossBorderCompanyData() {
        JAXBContext jaxbContext = JAXBUtil.CROSS_BORDER_MERGER_DISCLOSURE_NOTIFICATION_JAXB_CONTEXT;
        CrossBorderMergerDisclosureNotificationTemplate obj =
                SampleObjectFactory.createSample(CrossBorderMergerDisclosureNotificationTemplate.class);

        QName qName = new QName(
                "http://ec.europa.eu/bris/v4_0/br/CrossBorderMergerDisclosureNotificationTemplate",
                "CrossBorderMergerDisclosureNotificationTemplate"
        );

        String xml = BrisUtil.marshalAsElementToString(obj, CrossBorderMergerDisclosureNotificationTemplate.class, qName, jaxbContext);
        CrossBorderMergerDisclosureNotificationTemplate crossBorderMergerDisclosureNotificationTemplate
                = BrisUtil.unmarshal(new ByteArrayInputStream(xml.getBytes()), CrossBorderMergerDisclosureNotificationTemplate.class, jaxbContext);
        assertNotNull(crossBorderMergerDisclosureNotificationTemplate.getCrossBorderCompanyData());
    }

    @Test
    void givenAPrsu100UObject_whenMarshalled_thenXmlFileIsNotNull() {
        JAXBContext jaxbContext = JAXBUtil.PRSU100U_JAXB_CONTEXT;
        Prsu100U obj =
                SampleObjectFactory.createSample(Prsu100U.class);

        QName qName = new QName(
                "http://fsb.belgium.be/edepot-v3/prsu100u",
                "prsu100u"
        );

        String xml = BrisUtil.marshalToString(obj, jaxbContext);
        assertNotNull(xml);
    }

    private static String buildXml(String payload) throws IOException {
        DefaultResourceLoader ctx = new DefaultResourceLoader();
        Resource xmlResource = ctx.getResource(payload);
        return Files.readString(Path.of(xmlResource.getURI()));
    }

}
