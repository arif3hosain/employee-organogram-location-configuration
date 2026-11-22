package com.bracits.pms;

import com.bracits.pms.shared.notification.NotificationService;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * Standard Spring layering: Controller -> Service (@Transactional) -> Repository.
 * Side effects after commit go through application events and @TransactionalEventListener.
 * Fails the build if a layer rule is broken.
 */
@AnalyzeClasses(packages = "com.bracits.pms", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** Controllers never touch repositories. */
    @ArchTest
    static final ArchRule controllersDoNotUseRepositories = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    /** Repositories are used only by services. */
    @ArchTest
    static final ArchRule repositoriesOnlyFromServices = classes()
            .that().resideInAPackage("..infrastructure..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..service..", "..infrastructure..");

    /** Transaction boundaries live in the service layer only (class level)... */
    @ArchTest
    static final ArchRule onlyServicesAreTransactional = noClasses()
            .that().resideOutsideOfPackage("..service..")
            .should().beAnnotatedWith(Transactional.class)
            .orShould().beAnnotatedWith(jakarta.transaction.Transactional.class);

    /** ...and method level. */
    @ArchTest
    static final ArchRule onlyServiceMethodsAreTransactional = noMethods()
            .that().areDeclaredInClassesThat().resideOutsideOfPackage("..service..")
            .should().beAnnotatedWith(Transactional.class)
            .orShould().beAnnotatedWith(jakarta.transaction.Transactional.class);

    /** Dependency direction: services never call back into controllers. */
    @ArchTest
    static final ArchRule servicesDoNotDependOnControllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().areAnnotatedWith(RestController.class);

    /** Mail is never sent inside a service transaction; services publish events instead. */
    @ArchTest
    static final ArchRule servicesDoNotSendMail = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().areAssignableTo(NotificationService.class);

    /** Listeners that react to service events live in an ..event.. package. */
    @ArchTest
    static final ArchRule transactionalListenersLiveInEventPackages = methods()
            .that().areAnnotatedWith(TransactionalEventListener.class)
            .should().beDeclaredInClassesThat().resideInAPackage("..event..");
}
