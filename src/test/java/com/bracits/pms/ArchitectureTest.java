package com.bracits.pms;

import com.bracits.pms.shared.action.BaseAction;
import com.bracits.pms.shared.notification.NotificationService;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Guide §3.4 "Rules (enforced with ArchUnit in CI)". Fails the build if a layer rule is broken. */
@AnalyzeClasses(packages = "com.bracits.pms", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /** Rule 1: actions are never @Transactional (class level). */
    @ArchTest
    static final ArchRule actionsAreNotTransactional = classes()
            .that().areAssignableTo(BaseAction.class)
            .should().notBeAnnotatedWith(Transactional.class)
            .andShould().notBeAnnotatedWith(jakarta.transaction.Transactional.class);

    /** Rule 1: ...nor any of their methods. */
    @ArchTest
    static final ArchRule actionMethodsAreNotTransactional = methods()
            .that().areDeclaredInClassesThat().areAssignableTo(BaseAction.class)
            .should().notBeAnnotatedWith(Transactional.class)
            .andShould().notBeAnnotatedWith(jakarta.transaction.Transactional.class);

    /** Every action extends BaseAction, so the pre/execute/post flow can't be bypassed. */
    @ArchTest
    static final ArchRule actionsExtendBaseAction = classes()
            .that().resideInAPackage("..action..").and().haveSimpleNameEndingWith("Action")
            .should().beAssignableTo(BaseAction.class);

    /** Rule 6: Controller -> Action only. Controllers never call services or repositories. */
    @ArchTest
    static final ArchRule controllersOnlyUseActions = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().resideInAnyPackage("..service..", "..infrastructure..");

    /** Rule 6: actions call services, never repositories. */
    @ArchTest
    static final ArchRule actionsDoNotUseRepositories = noClasses()
            .that().resideInAPackage("..action..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    /** Repositories are used only by services (of their own module). */
    @ArchTest
    static final ArchRule repositoriesOnlyFromServices = classes()
            .that().resideInAPackage("..infrastructure..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..service..", "..infrastructure..");

    /** Dependency direction: services never call back into actions or controllers. */
    @ArchTest
    static final ArchRule servicesDoNotDependOnActionsOrControllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().resideInAPackage("..action..")
            .orShould().dependOnClassesThat().areAnnotatedWith(RestController.class);

    /** Rule 4: mail is sent from postCondition, never inside a service transaction. */
    @ArchTest
    static final ArchRule servicesDoNotSendMail = noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat().areAssignableTo(NotificationService.class);
}
