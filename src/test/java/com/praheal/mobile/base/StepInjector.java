package com.praheal.mobile.base;

import com.praheal.mobile.app.base.BaseSteps;
import io.appium.java_client.android.AndroidDriver;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import org.openqa.selenium.WebElement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class StepInjector {

    private final AndroidDriver androidDriver;
    private final StepInterceptor stepInterceptor;
    private final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    public StepInjector(AndroidDriver androidDriver) {
        this.androidDriver = androidDriver;
        this.stepInterceptor = new StepInterceptor(androidDriver);
    }

    public <T> T getStep(Class<T> stepsClass) {
        if (!stepsClass.getSimpleName().endsWith("Steps")) {
            throw new IllegalArgumentException(
                    "Class " + stepsClass.getSimpleName() + " must end with 'Steps'."
            );
        }
        if (!BaseSteps.class.isAssignableFrom(stepsClass)) {
            throw new IllegalArgumentException(
                    "Class " + stepsClass.getSimpleName() + " must extend " + BaseSteps.class.getSimpleName() + "."
            );
        }
        verifyHasNoLocators(stepsClass);
        return stepsClass.cast(cache.computeIfAbsent(stepsClass, this::createInstance));
    }

    private static void verifyHasNoLocators(Class<?> stepsClass) {
        for (Class<?> clazz = stepsClass; clazz != BaseSteps.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                boolean isElementField = WebElement.class.isAssignableFrom(field.getType())
                        || List.class.isAssignableFrom(field.getType());
                boolean hasFindByAnnotation = false;
                for (Annotation annotation : field.getAnnotations()) {
                    if (annotation.annotationType().getSimpleName().contains("FindBy")) {
                        hasFindByAnnotation = true;
                    }
                }
                if (isElementField || hasFindByAnnotation) {
                    throw new IllegalStateException(
                            clazz.getSimpleName() + "." + field.getName() + " is a locator. Move it to "
                                    + clazz.getSimpleName().replaceFirst("Steps$", "") + ".");
                }
            }
        }
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Class<?> instrumentedClass = new ByteBuddy()
                    .subclass(clazz, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
                    .method(isPublic().and(not(isDeclaredBy(Object.class))))
                    .intercept(MethodDelegation.to(stepInterceptor))
                    .make()
                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
            return instrumentedClass.getDeclaredConstructor(AndroidDriver.class).newInstance(androidDriver);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "Step class " + clazz.getSimpleName() + " must have a public constructor accepting AndroidDriver", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate step class: " + clazz.getSimpleName(), e);
        }
    }
}
