package com.github.cornerstonews.util;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.oxm.XMLConstants;



public abstract class JAXBUtils {

    private final static Map<Class<?>, JAXBContext> contextMap = new HashMap<>();
    private final static String APPLICATION_JSON = "application/json";

    public static String marshalToJSON(Object jaxbElement) throws JAXBException {

        return marshalToJSON(jaxbElement, false);
    }

    public static String marshalToJSON(Object jaxbElement, boolean formattedOutput) throws JAXBException {

        return marshalToJSON(jaxbElement, formattedOutput, null);
    }

    public static String marshalToJSON(Object jaxbElement, boolean formattedOutput, JAXBContext jaxbContext) throws JAXBException {

        if (jaxbElement == null) {
            throw new JAXBException("Object must be provided to marshal to JSON");
        }

        if (jaxbContext == null) {
            jaxbContext = getContext(jaxbElement.getClass());
        }

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, APPLICATION_JSON);
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, Boolean.FALSE);
        marshaller.setProperty(MarshallerProperties.JSON_NAMESPACE_SEPARATOR, XMLConstants.DOT);
        if (formattedOutput) {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }

        StringWriter writer = new StringWriter();
        marshaller.marshal(jaxbElement, writer);
        return writer.toString();
    }

    public static <T> T unmarshalFromJSON(String jsonObjectString, Class<T> expectedType) throws JAXBException {

        return unmarshalFromJSON(jsonObjectString, null, expectedType);
    }

    public static <T> T unmarshalFromJSON(String jsonObjectString, JAXBContext ctx, Class<T> expectedType) throws JAXBException {

        if (jsonObjectString == null || jsonObjectString.isEmpty()) {
            throw new JAXBException("JSON String must be provided to unmarshal.");
        }

        if (ctx == null) {
            ctx = getContext(expectedType);
        }

        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, APPLICATION_JSON);
        unmarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, Boolean.FALSE);

        Reader reader = new StringReader(jsonObjectString);
        StreamSource source = new StreamSource(reader);

        JAXBElement<T> jaxbElement = unmarshaller.unmarshal(source, expectedType);
        return jaxbElement.getValue();

    }

    private static <T> JAXBContext getContext(Class<T> type) throws JAXBException {
        if (!contextMap.containsKey(type)) {
            contextMap.put(type, JAXBContextFactory.createContext(new Class[] { type }, null));
        }
        return contextMap.get(type);
    }

    public static void clearJAXBContexts() {
        contextMap.clear();
    }
}