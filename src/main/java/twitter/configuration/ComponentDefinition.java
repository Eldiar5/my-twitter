package twitter.configuration;

import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.util.List;

public class ComponentDefinition<T extends Executable>{

    private final Class<?> keyClass;
    private final Class<?> originalClass;
    private final T howToCreate;
    private final List<Class<?>> constructorArgumentTypes;
    private final ElementType elementType;

    public ComponentDefinition(Class<?> keyClass, Class<?> originalClass, T howToCreate, List<Class<?>> constructorArgumentTypes, ElementType elementType) {
        this.keyClass = keyClass;
        this.originalClass = originalClass;
        this.howToCreate = howToCreate;
        this.constructorArgumentTypes = constructorArgumentTypes;
        this.elementType = elementType;
    }

    public Class<?> getKeyClass() {
        return keyClass;
    }

    public Class<?> getOriginalClass() {
        return originalClass;
    }

    public T getHowToCreate() {
        return howToCreate;
    }

    public List<Class<?>> getConstructorArgumentTypes() {
        return constructorArgumentTypes;
    }

    public ElementType getElementType() {
        return elementType;
    }
}
