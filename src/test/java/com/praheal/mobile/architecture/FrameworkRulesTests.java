package com.praheal.mobile.architecture;

import com.praheal.mobile.app.users.UsersPool;
import com.praheal.mobile.base.BaseMobileTest;
import com.praheal.mobile.listeners.PrahealLabels;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class FrameworkRulesTests {

    private static final String ROOT_PACKAGE = "com.praheal.mobile";
    private static final String STEPS_SUFFIX = "Steps";
    private static final String SCREEN_SUFFIX = "Screen";
    private static final String CONSTANTS_PACKAGE = "..app.constants..";
    private static final String ZEPHYR_KEY_PATTERN = "HAT-T\\d+";

    private JavaClasses classes;

    @BeforeClass(alwaysRun = true)
    public void importClasses() {
        classes = new ClassFileImporter().importPackages(ROOT_PACKAGE);
    }

    @Test
    public void assertionsAreOnlyInStepsClasses() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameNotEndingWith(STEPS_SUFFIX)
                .should().dependOnClassesThat().haveFullyQualifiedName("org.testng.Assert")
                .orShould().dependOnClassesThat().resideInAnyPackage(
                        "org.testng.asserts..", "org.junit..", "org.assertj..", "org.hamcrest..")
                .because("assertions belong in check... methods of Steps classes");
        rule.check(classes);
    }

    @Test
    public void testsDoNotUseDriverOrElements() {
        ArchRule rule = noClasses()
                .that().areAssignableTo(BaseMobileTest.class)
                .and().doNotHaveFullyQualifiedName(BaseMobileTest.class.getName())
                .should().dependOnClassesThat().resideInAnyPackage("org.openqa.selenium..", "io.appium..")
                .because("tests only call Steps methods; driver and element logic lives in Steps classes");
        rule.check(classes);
    }

    @Test
    public void testsDoNotUseScreenClasses() {
        ArchRule rule = noClasses()
                .that().areAssignableTo(BaseMobileTest.class)
                .should().dependOnClassesThat().haveSimpleNameEndingWith(SCREEN_SUFFIX)
                .because("tests reach screens only through their Steps classes");
        rule.check(classes);
    }

    @Test
    public void constantsArePublicStaticFinal() {
        ArchRule rule = fields()
                .that().areDeclaredInClassesThat(
                        resideInAPackage(CONSTANTS_PACKAGE).or(simpleName(UsersPool.class.getSimpleName())))
                .should().bePublic()
                .andShould().beStatic()
                .andShould().beFinal()
                .because("constants must be public static final so they can be static imported");
        rule.check(classes);
    }

    @Test
    public void testsHaveZephyrTestId() {
        ArchRule rule = methods()
                .that().areAnnotatedWith(Test.class)
                .and().areDeclaredInClassesThat().areAssignableTo(BaseMobileTest.class)
                .should(haveZephyrTestId())
                .because("every test is linked to its Zephyr test case in project HAT");
        rule.check(classes);
    }

    private static ArchCondition<JavaMethod> haveZephyrTestId() {
        return new ArchCondition<>("have @TestID with a Zephyr key matching " + ZEPHYR_KEY_PATTERN) {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean valid = method.tryGetAnnotationOfType(PrahealLabels.TestID.class)
                        .map(testId -> testId.value().matches(ZEPHYR_KEY_PATTERN))
                        .orElse(false);
                if (!valid) {
                    events.add(SimpleConditionEvent.violated(method,
                            method.getFullName() + " has no @TestID(\"HAT-T<n>\")"));
                }
            }
        };
    }
}
