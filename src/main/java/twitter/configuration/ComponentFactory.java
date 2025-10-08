package twitter.configuration;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ComponentFactory {
    private final Map<Class<?>, Object> components;
    private final String packageName;
    private final Environment environment;
    private final Class<?> mainClass;

    public ComponentFactory(Class<?> mClass, Environment environment) {
        this.mainClass = mClass;
        this.packageName = mClass.getPackage().getName();
        this.environment = environment;
        this.components = new HashMap<>();
    }

    public <T> T getComponent(Class<T> clazz) {
        return (T) this.components.get(clazz);
    }

    public void configure() {
        try {
            List<Class<?>> classes = this.scanPackage(mainClass);

            List<ComponentDefinition<?>> componentDefinitionList = new LinkedList<>();

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Component.class)) {
                    Constructor<?> constructorForInject = this.findInjectionConstructor(clazz);

                    if (Objects.isNull(constructorForInject)) {
                        System.out.println("Не найден конструктор, или он не помечен аннотацией Injection " + clazz.getSimpleName());
                        System.exit(1);
                    }

                    //Проверка, есть ли над компонентом профилирование
                    if (clazz.isAnnotationPresent(Profile.class)) {
                        Profile profile = clazz.getAnnotation(Profile.class);
                        List<String> activeProfiles = Arrays.asList(profile.active());
                        if (!activeProfiles.contains(environment.getApplicationProfile())) {
                            continue;
                        }
                    }

                    List<Class<?>> interfaces = List.of(clazz.getInterfaces());
                    if (!interfaces.isEmpty()) {
                        for (Class<?> anInterface : interfaces) {
                            if (anInterface.getPackage().getName().startsWith(packageName)) {
                                componentDefinitionList.add(new ComponentDefinition<>(anInterface, clazz, constructorForInject, List.of(constructorForInject.getParameterTypes()), ElementType.TYPE));
                            }
                        }
                    } else {
                        componentDefinitionList.add(new ComponentDefinition<>(clazz, clazz, constructorForInject, List.of(constructorForInject.getParameterTypes()), ElementType.TYPE));
                    }

                }

                if (clazz.isAnnotationPresent(SideComponent.class)) {

                    //Проверка, есть ли над компонентом профилирование
                    if (clazz.isAnnotationPresent(Profile.class)) {
                        Profile profile = clazz.getAnnotation(Profile.class);
                        List<String> activeProfiles = Arrays.asList(profile.active());
                        if (!activeProfiles.contains(environment.getApplicationProfile())) {
                            continue;
                        }
                    }

//                 1. СНАЧАЛА РЕГИСТРИРУЕМ САМ SideComponent КАК ОБЫЧНЫЙ КОМПОНЕНТ
                    Constructor<?> constructorForInject = this.findInjectionConstructor(clazz);

                    // Добавляем "чертёж" для самого класса-фабрики (Source.class)
                    componentDefinitionList.add(new ComponentDefinition<>(clazz, clazz, constructorForInject, List.of(constructorForInject.getParameterTypes()), ElementType.TYPE));

                    List<Method> methods = Arrays.stream(clazz.getMethods()).filter(method -> method.isAnnotationPresent(SideMethod.class)).toList();
                    for (Method method : methods) {
                        Class<?> keyClass = method.getReturnType();
                        List<Class<?>> methodArgs = List.of(method.getParameterTypes());
                        componentDefinitionList.add(new ComponentDefinition<>(keyClass, clazz, method, methodArgs, ElementType.METHOD));
                    }
                }
            }

            List<Class<?>> configurableClasses = new LinkedList<>();

            // ИСПРАВЛЕННЫЙ ЦИКЛ: Мы больше не удаляем элементы
            for (ComponentDefinition<?> componentDefinition : componentDefinitionList) {
                // Собираем компонент, только если он еще не был собран как зависимость
                if (!components.containsKey(componentDefinition.getKeyClass())) {
                    this.buildComponent(componentDefinition, componentDefinitionList, configurableClasses);
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка конфигурации приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }


    //Находим правильный конструктор для инъекции зависимостей, следуя правилам.
    private Constructor<?> findInjectionConstructor(Class<?> clazz) {
        // 1. Ищем конструктор с аннотацией @Injection. Это высший приоритет.
        Optional<Constructor<?>> annotatedConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(Injection.class))
                .findFirst();

        if (annotatedConstructor.isPresent()) {
            return annotatedConstructor.get();
        }

        // 2. Если такого нет, ищем единственный публичный конструктор.
        Constructor<?>[] publicConstructors = clazz.getConstructors();
        if (publicConstructors.length == 1) {
            return publicConstructors[0];
        }

        // 3. Если конструкторов несколько и нет аннотации - это ошибка.
        if (publicConstructors.length > 1) {
            throw new IllegalStateException("В классе " + clazz.getSimpleName() +
                    " найдено несколько публичных конструкторов. Укажите нужный с помощью аннотации @Injection.");
        }

        // 4. Если нет ни аннотированных, ни публичных конструкторов.
        throw new IllegalStateException("Не найден подходящий конструктор, для класса " + clazz.getSimpleName());
    }

    private List<Class<?>> scanPackage(Class<?> mainClass) throws Exception {
        //Передаем имя нашего package с проектом, и меняем формат пути для файловой системы,
        // на понятный для ClassLoader который ищет ресурсы.
        String packageName = mainClass.getPackageName().replace(".", "/");
        //Получаем загрузчик у текущего потока, для поиска ресурсов внутри скомпилированного проекта.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //Просим загрузчик найти переданный package с проектом, и вернуть
        // местоположение в виде URL (универсального указателя на ресурс).
        URL url = classLoader.getResource(packageName);

        if (url == null) {
            throw new IllegalStateException("Не удалось найти папку: " + packageName);
        }

        List<Class<?>> classes = new LinkedList<>();
        //Берем протокол у нашего URL c проектом
        String protocol = url.getProtocol();
        System.out.println(protocol);

        if (protocol.equals("jar")) {
            //Утсанавливаем соединение с JAR-файлом
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            //Достаем jar-файл
            JarFile jarFile = jarConnection.getJarFile();

            //Получаем все "записи" (файлы и папки) внутри JAR-архива
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Проверяем, что это .class файл из нашего пакета (но не папка)
                if (entryName.startsWith(packageName) && entryName.endsWith(".class")) {
                    // Превращаем путь внутри JAR в полное имя класса
                    String className = entryName.replace(".class", "").replace("/", ".");
                    classes.add(Class.forName(className));
                }
            }
        } else if (protocol.equals("file")) {
            //Превращаем URL в File объект, который теперь указывает на абсолютный путь к папке с проектом
            File packageDir = new File(url.getFile());
            //Получаем родительскую папку относительно переданного package с проектом,
            //чтобы построить полный путь до класса.
            File classpathRoot = packageDir.getParentFile();
            this.findClasses(classpathRoot, classpathRoot, classes);
        }

        return classes;
    }

    private void findClasses(File classPathRoot, File currentDir, List<Class<?>> classes) throws ClassNotFoundException {
        // Получаем массив всех файлов и подпапок в текущей директории.
        File[] files = currentDir.listFiles();

        // Проверка на случай, если директория пуста или недоступна.
        if (files == null) {
            return;
        }

        // Проходимся по каждому файлу/папке в текущей директории.
        for (File file : files) {
            // Если текущий элемент - это папка...
            if (file.isDirectory()) {
                // ...то мы вызываем этот же метод для этой папки (погружаемся вглубь).
                // classPathRoot передается дальше без изменений.
                findClasses(classPathRoot, file, classes);

                // Если же это файл и его имя заканчивается на ".class"...
            } else if (file.getName().endsWith(".class")) {
                // ...начинается процесс преобразования пути файла в полное имя класса.

                // Получаем текстовый путь к корню classpath (например, "D:\MyTwitter\target\classes").
                String rootPath = classPathRoot.getPath();
                // Получаем текстовый путь к найденному .class файлу (например, "D:\MyTwitter\target\classes\twitter\controller\UserCommandController.class").
                String filePath = file.getPath();

                // Вычисляем относительный путь, "отрезая" от полного пути к файлу путь к корню classpath.
                // В результате получаем что-то вроде "twitter\controller\UserCommandController.class".
                String relativePath = filePath.substring(rootPath.length() + 1);

                // Превращаем относительный путь в полное имя класса, понятное для Java.
                // 1. Убираем расширение ".class".
                // 2. Заменяем системные разделители пути ('\' или '/') на точки '.'.
                // В итоге получаем строку "twitter.controller.UserCommandController".
                String className = relativePath.replace(".class", "").replace(File.separator, ".");

                // Используем стандартный механизм Java, чтобы по имени класса загрузить его в память
                // и получить объект типа Class<?>, который и добавляем в наш итоговый список.
                classes.add(Class.forName(className));
            }
        }
    }

    private void buildComponent(ComponentDefinition<?> componentDefinition, List<ComponentDefinition<?>> componentDefinitions, List<Class<?>> configurableClasses) throws Exception {
        // ИСПРАВЛЕНИЕ: Добавляем проверку в самом начале
        if (components.containsKey(componentDefinition.getKeyClass())) {
            return; // Этот компонент уже собран, выходим
        }

        // НОВОЕ ПРАВИЛО: Если компонент создаётся методом...
        if (componentDefinition.getElementType() == ElementType.METHOD) {
            // ...то сначала мы должны убедиться, что сама "класс-фабрика" (SideComponent) уже создана.
            Class<?> factoryClass = componentDefinition.getOriginalClass();

            // Если фабрики ещё нет в контейнере...
            if (!components.containsKey(factoryClass)) {
                // ...находим её "чертёж" и рекурсивно собираем её ПЕРЕД тем, как продолжить.
                ComponentDefinition<?> factoryDefinition = this.getDefinition(factoryClass, componentDefinitions);
                this.buildComponent(factoryDefinition, componentDefinitions, configurableClasses);
            }
        }

        //Если пришедший definition не требует аргументов для создания, создаем instance и ложим в контейнер.
        if (componentDefinition.getConstructorArgumentTypes().isEmpty()) {
            this.components.put(componentDefinition.getKeyClass(), this.createInstance(componentDefinition));
            return;
        }

        //Иначе ложим definition в список конфигурируемых классов.
        configurableClasses.add(componentDefinition.getKeyClass());

        List<Object> arguments = new LinkedList<>();

        //Перебираем аргументы нашего definition
        for (Class<?> clazz : componentDefinition.getConstructorArgumentTypes()) {
            //Если находим аргумент для создания которого требуется наш пришедший definition,
            //мы обнаружили цикл. зависимость.
            if (configurableClasses.contains(clazz)) {
                System.out.println("Обнаружена циклическая зависимость между " + componentDefinition.getOriginalClass().getSimpleName() + " и " + clazz.getSimpleName());
                System.exit(1);
            }

            //Иначе мы отправляем аргумент в метод getDefinition и рекурсивно собираем сначала аргумент.
            if (!configurableClasses.contains(clazz)) {
                ComponentDefinition<?> dependency = this.getDefinition(clazz, componentDefinitions);
                this.buildComponent(dependency, componentDefinitions, configurableClasses);

            }
            //Ложим в список аргументов, рекурсивно созданные аргументы.
            arguments.add(this.components.get(clazz));

        }
        //Достаем тип данных у нашего definition, отправляем сам definition и собранные экземпляры аргументов в метод createInstance,
        //Получаем instance нашего definition и ложим в контейнер.
        this.components.put(componentDefinition.getKeyClass(), this.createInstance(componentDefinition, arguments.toArray()));
        //удаляем definition из списка конфигурируемых.
        configurableClasses.remove(componentDefinition.getKeyClass());
    }

    //getDefinition метод нужен для того чтобы получить описание пришедшого аргумента.
    private ComponentDefinition<?> getDefinition(Class<?> args, List<ComponentDefinition<?>> componentDefinitionList) {
        //Перебираем definitionList и если находим нашу зависимость возвращаем ее definition.
        Optional<ComponentDefinition<?>> definition = componentDefinitionList.stream()
                .filter(defs -> defs.getKeyClass().equals(args))
                .findFirst();

        //Удаляем definition из списка после того как нашли и собрали компонент.
        componentDefinitionList.remove(definition);

        return definition
                .orElseThrow(() -> {
                    System.out.println("ОШИБКА: Не удалось найти определение для компонента: " + args.getName());
                    System.out.println(">> Проверьте, что на классе-реализации стоит аннотация @Component и на его конструкторе @Injection.");
                    return new IllegalStateException("Определение компонента не найдено");
                });
    }

    private <T> T convertValue(Object value, Class<T> valueType) {
        if (valueType.isInstance(value)) {
            return (T) value;
        }
        return switch (valueType.getName()) {
            case "java.lang.String" -> (T) value.toString();
            case "java.lang.Integer" -> (T) Integer.valueOf(value.toString());
            case "java.lang.Boolean" -> (T) Boolean.valueOf(value.toString());
            case "java.lang.Double" -> (T) Double.valueOf(value.toString());
            case "java.lang.Float" -> (T) Float.valueOf(value.toString());
            case "java.lang.Long" -> (T) Long.valueOf(value.toString());
            case "java.lang.Short" -> (T) Short.valueOf(value.toString());
            case "java.lang.Byte" -> (T) Byte.valueOf(value.toString());
            case "java.lang.Character" -> (T) Character.valueOf(value.toString().charAt(0));
            default -> throw new IllegalArgumentException("Неподдерживаемый тип данных " + valueType.getName());
        };
    }

    private void setFieldProperties(ComponentDefinition<?> componentDefinition, Object instance) {
        try {
            for (Field field : componentDefinition.getOriginalClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Value.class)) {
                    Value annotation = field.getAnnotation(Value.class);
                    Object value = this.environment.getProperty(annotation.key());
                    if (Objects.isNull(value)) {
                        System.out.println("Ошибка при конфигурации приложения: ");
                        System.out.println("Не найдено свойство " + annotation.key());
                        System.exit(1);
                    }
                    field.setAccessible(true);
                    field.set(instance, this.convertValue(value, field.getType()));
                    field.setAccessible(false);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //createInstance метод нужен для создания экземпляра конкретного definition
    private Object createInstance(ComponentDefinition<?> componentDefinition, Object... args) throws Exception {

        if (componentDefinition.getElementType().equals(ElementType.METHOD)) {

            Class<?> sideComponentClass = componentDefinition.getOriginalClass();
            Object sideComponentInstance = this.components.get(sideComponentClass);
            if (sideComponentInstance == null) {
                throw new IllegalStateException("Экземпляр для SideComponent " + sideComponentClass.getName() + " не был создан заранее.");
            }

            this.setFieldProperties(componentDefinition, sideComponentInstance);

            Method method = (Method) componentDefinition.getHowToCreate();
            return method.invoke(sideComponentInstance, args);
        }

        if (componentDefinition.getElementType().equals(ElementType.TYPE)) {
            Constructor<?> constructor = (Constructor<?>) componentDefinition.getHowToCreate();
            Object instance = constructor.newInstance(args);
            this.setFieldProperties(componentDefinition, instance);
            return instance;
        }

        throw new IllegalStateException("Неподдерживаемый тип элемента для создания: " + componentDefinition.getElementType());
    }

}
