package com.bracits.pms;

import com.bracits.pms.domain.SharedAcrossTenants;
import com.bracits.pms.domain.TenantChildOf;
import com.bracits.pms.domain.TenantScopedEntity;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Multi-tenancy rules (V6). Every table must say how it relates to tenants, so a new table cannot
 * silently leak data between customers:
 * <ul>
 *   <li>PARENT: extends {@link TenantScopedEntity} (has tenant_id, filtered by Hibernate);</li>
 *   <li>CHILD: {@link TenantChildOf} (no tenant_id, reached only through its parent);</li>
 *   <li>SHARED: {@link SharedAcrossTenants} (same data for every tenant).</li>
 * </ul>
 */
@AnalyzeClasses(packages = "com.bracits.pms", importOptions = ImportOption.DoNotIncludeTests.class)
class TenancyRulesTest {

    @ArchTest
    static final ArchRule everyEntityDeclaresItsTenancy = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(TenantScopedEntity.class)
            .orShould().beAnnotatedWith(TenantChildOf.class)
            .orShould().beAnnotatedWith(SharedAcrossTenants.class)
            .because("every table must be a tenant parent, a tenant child or shared reference data");

    /** A child table has no tenant_id, so an unscoped findById/findAll on it would cross tenants. */
    @Test
    void childRepositoriesHaveNoUnscopedFinders() {
        List<String> offenders = new ArrayList<>();
        for (JavaClass jc : new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.bracits.pms.repository")) {
            Class<?> repo = jc.reflect();
            if (!repo.isInterface() || !Repository.class.isAssignableFrom(repo)) {
                continue;
            }
            Class<?> entity = ResolvableType.forClass(repo).as(Repository.class).getGeneric(0).resolve();
            boolean child = entity != null && entity.isAnnotationPresent(TenantChildOf.class);
            if (child && (CrudRepository.class.isAssignableFrom(repo) || ListCrudRepository.class.isAssignableFrom(repo))) {
                offenders.add(repo.getSimpleName() + " (" + entity.getSimpleName() + ")");
            }
        }
        assertThat(offenders).as("repositories of @TenantChildOf entities must extend Repository, not JpaRepository").isEmpty();
    }
}
