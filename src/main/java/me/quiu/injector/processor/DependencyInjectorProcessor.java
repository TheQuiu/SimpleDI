package me.quiu.injector.processor;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.quiu.annotation.Dependency;
import me.quiu.annotation.Singleton;
import me.quiu.injector.DependencyInjectorInitializer;
import me.quiu.injector.container.DependencyContainer;

import java.lang.reflect.Field;

/**
 * @author ildar (quiu)
 * <p>DependencyInjectorProcessor creation on 1/2/2024 at 2:45 AM</p>
 */

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class DependencyInjectorProcessor {

    DependencyInjectorInitializer initializer;
    DependencyContainer container;

    public DependencyInjectorProcessor(DependencyInjectorInitializer initializer, DependencyContainer container) {
        this.initializer = initializer;
        this.container = container;
        this.start();
    }

    private void start() {
        initializer.getAvailableClasses().forEach(clazz -> {
            for (Field field : clazz.getFields()) {
                if (field.isAnnotationPresent(Dependency.class)) {
                    System.out.printf("Processing dependency %s%n", field.getType().getSimpleName());
                    processField(field);
                }
            }
        });
    }


    private void processField(Field field) {
        Object dep = container.getDependencies().get(field.getType().getSimpleName());
        field.setAccessible(true);
        try {
            if (dep == null) {
                dep = field.getType().getDeclaredConstructor().newInstance();
                container.getDependencies().put(field.getType().getSimpleName(), dep);
            }
            if (field.getType().isAnnotationPresent(Singleton.class)) {
                field.set(field.getType(), dep);
            } else {
                field.set(field.getType(), dep.getClass().getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
