package me.quiu.injector;

import lombok.Getter;
import lombok.SneakyThrows;
import me.quiu.annotation.Priority;
import me.quiu.injector.container.DependencyContainer;
import me.quiu.injector.processor.AutoInstanceProcessor;
import me.quiu.injector.processor.DependencyInjectorProcessor;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * {@code @author:} <a href="https://github.com/TheQuiu">quiu</a>
 */
@Getter
public final class DependencyInjectorInitializer {

    private final Set<Class<?>> loadedClasses = ConcurrentHashMap.newKeySet();
    private final String packageName;
    private final ClassLoader cld;
    private final DependencyContainer container;
    private List<Class<?>> availableClasses = new ArrayList<>();

    public DependencyInjectorInitializer(String packageName, ClassLoader loader) {
        this.packageName = packageName;
        this.cld = loader;
        this.container = new DependencyContainer();
    }

    private static int getDependencyPriority(Class<?> clazz) {
        Priority annotation = clazz.getAnnotation(Priority.class);
        if (annotation != null) {
            return annotation.value();
        }
        return 0;
    }

    @SneakyThrows
    private void checkDirectory(File directory, String pckgname, Set<Class<?>> classes) {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        classes.add(Class.forName(pckgname + '.' + file.substring(0, file.length() - 6)));
                    } catch (NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file, classes);
                }
            }
        }
    }

    @SneakyThrows
    private void checkJarFile(JarURLConnection connection, String packageName, Set<Class<?>> classes) {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry; entries.hasMoreElements() && ((jarEntry = entries.nextElement()) != null); ) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(packageName)) {
                    classes.add(Class.forName(name));
                }
            }
        }
    }

    public void inject() {
        long start = System.currentTimeMillis();
        System.out.println("Loading classes from package " + packageName);
        try {
            loadClasses();
        } catch (Exception e) {
            System.err.println("Error loading classes: " + e);
        }
        System.out.println("Loaded " + loadedClasses.size() + " classes");

        availableClasses = loadedClasses
                .stream()
                .sorted((o1, o2) -> Integer.compare(getDependencyPriority(o2), getDependencyPriority(o1)))
                .toList();
        new DependencyInjectorProcessor(this, container);
        new AutoInstanceProcessor(this);
        System.out.println("Dependency injector initialized in " + (System.currentTimeMillis() - start) + "ms");
    }

    @SneakyThrows
    private void loadClasses() {
        if (cld == null) throw new ClassNotFoundException("Can't get class loader.");

        Enumeration<URL> resources = cld.getResources(packageName.replace('.', '/'));

        URLConnection connection;

        for (URL url; resources.hasMoreElements() && ((url = resources.nextElement()) != null); ) {
            try {
                connection = url.openConnection();

                if (connection instanceof JarURLConnection) {
                    System.out.println("Loading classes from jar file " + url.getPath());
                    checkJarFile((JarURLConnection) connection, packageName, loadedClasses);
                } else {
                    System.out.println("Loading classes from directory " + URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
                    checkDirectory(new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8)), packageName, loadedClasses);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
