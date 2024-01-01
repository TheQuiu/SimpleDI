package me.quiu.injector.container;

import lombok.Getter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ildar (quiu)
 * <p>DependencyContainer creation on 1/2/2024 at 2:45 AM</p>
 */

@Getter
public final class DependencyContainer {

    private final Map<String, Object> dependencies = new ConcurrentHashMap<>();
    private final Set<String> inProgressDependencies = ConcurrentHashMap.newKeySet();

    public void addDependency(Object obj) {
        String className = obj.getClass().getSimpleName();
        if (inProgressDependencies.contains(className)) {
            throw new IllegalStateException("Circular Dependency detected involving class: " + className);
        }

        inProgressDependencies.add(className);

        dependencies.put(className, obj);

        inProgressDependencies.remove(className);
    }

    public Object removeDependency(Object obj) {
        return dependencies.remove(obj.getClass().getSimpleName());
    }

    public Object removeDependency(Class<?> obj) {
        return dependencies.remove(obj.getSimpleName());
    }

    public void clear() {
        dependencies.clear();
    }

    public Object getDependency(Object obj) {
        return dependencies.get(obj.getClass().getSimpleName());
    }

    public Object getDependency(Class<?> obj) {
        return dependencies.get(obj.getSimpleName());
    }

    public boolean containsDependency(Object obj) {
        return dependencies.containsKey(obj.getClass().getSimpleName());
    }

    public boolean containsDependency(Class<?> obj) {
        return dependencies.containsKey(obj.getSimpleName());
    }

    public boolean containsDependency(String name) {
        return dependencies.containsKey(name);
    }

}
