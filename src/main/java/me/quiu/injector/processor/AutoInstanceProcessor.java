package me.quiu.injector.processor;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.quiu.annotation.AutoInstance;
import me.quiu.injector.DependencyInjectorInitializer;

/**
 * @author ildar (quiu)
 * <p>AutoInstanceProcessor creation on 1/2/2024 at 2:45 AM</p>
 */

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public final class AutoInstanceProcessor {

    DependencyInjectorInitializer initializer;

    public AutoInstanceProcessor(DependencyInjectorInitializer initializer) {
        this.initializer = initializer;
        this.start();
    }

    private void start() {
        initializer.getAvailableClasses().forEach(clazz -> {
            if (clazz.isAnnotationPresent(AutoInstance.class)) {
                try {
                    clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        });
    }

}
