package nl.rotterdam.verbonden.core.persistence;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Version;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Bewaakt de regel uit AGENTS.md: velden die na het aanmaken niet meer wijzigen ({@code @Id}, {@code @Version}
 * en {@code updatable = false}) krijgen geen setter, maar worden via de constructor gezet.
 */
@AnalyzeClasses(packagesOf = EntityConventiesTest.class, importOptions = ImportOption.DoNotIncludeTests.class)
class EntityConventiesTest {

    @ArchTest
    static final ArchRule nietWijzigbareVeldenHebbenGeenSetter = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(new ArchCondition<>("geen setter hebben voor @Id-, @Version- of updatable = false-velden") {
                @Override
                public void check(JavaClass entity, ConditionEvents events) {
                    for (JavaField field : entity.getFields()) {
                        String setter = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        boolean heeftSetter = entity.getMethods().stream().anyMatch(m -> m.getName().equals(setter));
                        if (isNietWijzigbaar(field) && heeftSetter) {
                            events.add(SimpleConditionEvent.violated(entity,
                                    entity.getSimpleName() + "." + setter + "() hoort er niet te zijn: "
                                            + field.getName() + " wijzigt na het aanmaken niet meer"));
                        }
                    }
                }
            });

    @ArchTest
    static final ArchRule entityMetConstructorHeeftProtectedNoArgConstructor = classes()
            .that().areAnnotatedWith(Entity.class)
            .should(new ArchCondition<>("een protected no-arg constructor hebben (alleen voor JPA) zodra er een constructor met argumenten is") {
                @Override
                public void check(JavaClass entity, ConditionEvents events) {
                    boolean heeftConstructorMetArgumenten = entity.getConstructors().stream()
                            .anyMatch(c -> !c.getRawParameterTypes().isEmpty());
                    boolean noArgIsProtected = entity.getConstructors().stream()
                            .filter(c -> c.getRawParameterTypes().isEmpty())
                            .map(JavaConstructor::getModifiers)
                            .anyMatch(modifiers -> modifiers.contains(JavaModifier.PROTECTED));
                    if (heeftConstructorMetArgumenten && !noArgIsProtected) {
                        events.add(SimpleConditionEvent.violated(entity,
                                entity.getSimpleName() + " heeft een constructor met argumenten, maar geen protected no-arg constructor"));
                    }
                }
            });

    private static boolean isNietWijzigbaar(JavaField field) {
        return field.isAnnotatedWith(Id.class)
                || field.isAnnotatedWith(Version.class)
                || field.tryGetAnnotationOfType(Column.class).map(c -> !c.updatable()).orElse(false)
                || field.tryGetAnnotationOfType(JoinColumn.class).map(c -> !c.updatable()).orElse(false);
    }
}
