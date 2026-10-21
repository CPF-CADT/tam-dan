package com.tamdan;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = TamdanApp.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..", "..controller..", "..exception.web..")
        .optionalLayer("Service").definedBy("..service..", "..mail..", "..mapper..", "..dto..")
        .layer("Security").definedBy("..security..")
        .optionalLayer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..entities..")

        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config")
        .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Config")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

        .ignoreDependency(belongToAnyOf(TamdanApp.class), alwaysTrue())
        .ignoreDependency(alwaysTrue(), belongToAnyOf(
            com.tamdan.config.Constants.class,
            com.tamdan.config.ApplicationProperties.class
        ));

    @ArchTest
    static final ArchRule controllersDoNotAccessRepositories = noClasses()
        .that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule dtoRecordsDoNotDependOnEntities = noClasses()
        .that().resideInAnyPackage("..dto.request..", "..dto.response..")
        .should().dependOnClassesThat().resideInAPackage("..entities..");

    @ArchTest
    static final ArchRule entitiesDoNotDependOnApplicationLayers = noClasses()
        .that().resideInAPackage("..entities..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "..controller..", "..service..", "..repository..", "..security..", "..mail..", "..mapper..", "..dto..", "..web.."
        );
}
