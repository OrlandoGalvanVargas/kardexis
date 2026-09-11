package com.kardexis;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.kardexis", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // 1. Verifica los límites de los módulos de Spring Modulith
    @Test
    void verifyModulithStructure() {
        ApplicationModules.of(KardexisApplication.class).verify();
    }

    // 2. La capa de Dominio no debe depender de infraestructura o web
    @ArchTest
    static final ArchRule domain_does_not_depend_on_infrastructure_or_web =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure..",
                            "org.springframework.web..",
                            "..api.."
                    );

    // 3. La capa de Aplicación no debe depender de los DTOs o controladores de la API
    @ArchTest
    static final ArchRule application_does_not_depend_on_api =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAPackage("..api..");
}