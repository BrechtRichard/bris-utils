package be.bosa.edepot.util.bris;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SampleObjectFactory {

    // DatatypeFactory is used to create XMLGregorianCalendar instances (and Durations)
    private static final DatatypeFactory datatypeFactory;
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new ExceptionInInitializerError("Could not initialize DatatypeFactory: " + e.getMessage());
        }
    }

    /**
     * Creates a sample instance of the given class, with all fields populated.
     *
     * @param clazz the JAXB-generated class to instantiate
     * @return a new instance of clazz with sample data in all settable fields
     */
    public static <T> T createSample(Class<T> clazz) {
        Object result = createSampleRecursive(clazz, new HashSet<Class<?>>());
        // Cast the result to the requested type (or null if creation failed)
        return clazz.cast(result);
    }

    /**
     * Internal recursive helper that creates a sample object of the given class.
     * Uses a set of seen classes to avoid infinite recursion on cyclic references.
     */
    private static Object createSampleRecursive(Class<?> clazz, Set<Class<?>> seen) {
        if (clazz == null) {
            return null;
        }

        // Handle simple/base types first:
        if (clazz == String.class) {
            return "Sample" + clazz.getSimpleName();  // e.g., "SampleString"
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return Boolean.TRUE;
        }
        if (clazz == Character.class || clazz == char.class) {
            return 'A';
        }
        if (clazz == Byte.class || clazz == byte.class) {
            return (byte) 1;
        }
        if (clazz == Short.class || clazz == short.class) {
            return (short) 1;
        }
        if (clazz == Integer.class || clazz == int.class) {
            return 42;
        }
        if (clazz == Long.class || clazz == long.class) {
            return 42L;
        }
        if (clazz == Float.class || clazz == float.class) {
            return 3.14f;
        }
        if (clazz == Double.class || clazz == double.class) {
            return 3.14d;
        }
        if (clazz == BigDecimal.class) {
            return new BigDecimal("123.45");
        }
        if (clazz == BigInteger.class) {
            return new BigInteger("123");
        }
        if (XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
            // Create a sample XMLGregorianCalendar (current date/time for example)
            return datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
        }
        if (Duration.class.isAssignableFrom(clazz)) {
            // Create a sample duration of 1 day
            return datatypeFactory.newDuration("P1D");
        }
        if (QName.class.isAssignableFrom(clazz)) {
            // Create a sample QName with dummy namespace and local part
            return new QName("http://example.com", "SampleQName");
        }
        if (clazz.isEnum()) {
            // If it's an enum, return the first constant (if available)
            Object[] constants = clazz.getEnumConstants();
            return (constants != null && constants.length > 0) ? constants[0] : null;
        }
        if (clazz == Object.class) {
            // Cannot determine a specific sample for generic Object type
            return null;
        }
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            // Cannot instantiate interface or abstract class
            return null;
        }

        // Prevent infinite recursion for cyclic references: if we're already constructing this class, break the cycle
        if (seen.contains(clazz)) {
            return null;
        }
        seen.add(clazz);

        // Try to instantiate the object using the no-arg constructor
        Object instance;
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            instance = ctor.newInstance();
        } catch (Exception e) {
            // If we cannot instantiate this class, return null (skip populating this branch)
            seen.remove(clazz);
            return null;
        }

        // Track which properties we handle via setters (to avoid duplicate handling for collections)
        Set<String> handledProperties = new HashSet<>();

        // Iterate over all public methods to find setters
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            String name = m.getName();
            // We consider only "setXxx" methods with one parameter
            if (!name.startsWith("set") || name.length() <= 3) {
                continue;
            }
            if (m.getParameterCount() != 1) {
                continue;
            }
            // Skip setters inherited from Object (if any) or irrelevant
            if (name.equals("setClass")) {
                continue;
            }

            Class<?> paramType = m.getParameterTypes()[0];
            String propertyName = name.substring(3);  // e.g. "FirstName" for setFirstName
            handledProperties.add(propertyName.toLowerCase());  // mark as handled (use lowercase for consistency)

            try {
                // Determine a sample value for the parameter type
                Object value;
                if (List.class.isAssignableFrom(paramType)) {
                    // If there's a setter that takes a List (rare in JAXB), populate a list with one element
                    Type genericParam = m.getGenericParameterTypes()[0];
                    Class<?> elementType = Object.class;
                    if (genericParam instanceof ParameterizedType) {
                        Type[] typeArgs = ((ParameterizedType) genericParam).getActualTypeArguments();
                        if (typeArgs != null && typeArgs.length == 1 && typeArgs[0] instanceof Class<?>) {
                            elementType = (Class<?>) typeArgs[0];
                        }
                    }
                    // Create a new list and add one sample element (if elementType is resolvable)
                    List<Object> sampleList = new ArrayList<>();
                    Object sampleElement = createSampleRecursive(elementType, seen);
                    if (sampleElement != null) {
                        sampleList.add(sampleElement);
                    }
                    value = sampleList;
                } else if (paramType.isArray()) {
                    // If property is an array, create a one-element array
                    Class<?> compType = paramType.getComponentType();
                    Object array = Array.newInstance(compType, 1);
                    Object element = createSampleRecursive(compType, seen);
                    if (element != null) {
                        Array.set(array, 0, element);
                    }
                    value = array;
                } else {
                    // For all other types, recursively create a sample value
                    value = createSampleRecursive(paramType, seen);
                }

                // Only invoke the setter if we obtained a value (avoid passing null for primitives or unnecessary nulls)
                if (value != null) {
                    m.invoke(instance, value);
                }
            } catch (Exception e) {
                // Ignore exceptions setting this property and continue with others
            }
        }

        // Handle List properties without a setter: find any "getXxx" that returns a List and was not handled above
        for (Method m : methods) {
            String name = m.getName();
            if (!name.startsWith("get") || name.length() <= 3) {
                continue;
            }
            // Only consider getters that return a List type
            if (!List.class.isAssignableFrom(m.getReturnType())) {
                continue;
            }
            // Derive property name (after "get"), and check if it was already handled by a setter
            String propertyName = name.substring(3);  // e.g. "Items" for getItems
            if (handledProperties.contains(propertyName.toLowerCase())) {
                continue;  // already handled via a setter
            }
            try {
                @SuppressWarnings("unchecked")
                List<Object> listRef = (List<Object>) m.invoke(instance);
                if (listRef != null) {
                    // Determine element type via the generic return type
                    Type returnType = m.getGenericReturnType();
                    Class<?> elementType = Object.class;
                    if (returnType instanceof ParameterizedType) {
                        Type[] typeArgs = ((ParameterizedType) returnType).getActualTypeArguments();
                        if (typeArgs != null && typeArgs.length == 1 && typeArgs[0] instanceof Class<?>) {
                            elementType = (Class<?>) typeArgs[0];
                        }
                    }
                    // Create one sample element and add to the list
                    Object sampleElement = createSampleRecursive(elementType, seen);
                    if (sampleElement != null) {
                        listRef.add(sampleElement);
                    }
                }
            } catch (Exception e) {
                // Ignore exceptions from invoking the getter or adding to list
            }
        }

        // Remove the class from the seen set before returning, to allow other branches to use it if needed
        seen.remove(clazz);
        return instance;
    }
}