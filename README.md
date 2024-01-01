# SimpleDI
A simple dependency injection system for java

```java
//add this code on top of class
DependencyInjectorInitializer initializer = new DependencyInjectorInitializer("me.quiu.example", getClassLoader()); //initializing main class of di
initializer.getContainer().addDependency(this); //manualy add dependency
initializer.inject(); // start injection
```
