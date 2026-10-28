package br.com.fiap.oficina;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "br.com.fiap.oficina", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    private static final String DOMAIN = "br.com.fiap.oficina.domain..";
    private static final String APPLICATION = "br.com.fiap.oficina.application..";
    private static final String INFRASTRUCTURE = "br.com.fiap.oficina.infrastructure..";

    @ArchTest
    static final ArchRule domainESemFramework = noClasses().that().resideInAnyPackage(DOMAIN, APPLICATION)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule domainNaoDependeDeApplicationNemInfrastructure = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage(APPLICATION, INFRASTRUCTURE)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule applicationNaoDependeDeInfrastructure = noClasses().that().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule contextosIsoladosNoDomain = slices()
            .matching("br.com.fiap.oficina.domain.(*)..")
            .should().notDependOnEachOther()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule contextosIsoladosNaApplication = slices()
            .matching("br.com.fiap.oficina.application.(*)..")
            .should().notDependOnEachOther()
            .allowEmptyShould(true);
}
