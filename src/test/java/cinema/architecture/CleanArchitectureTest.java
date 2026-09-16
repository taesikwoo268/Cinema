package cinema.architecture;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
@AnalyzeClasses(packages = "cinema", importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {
    @ArchTest static final ArchRule domain_is_pure_java = classes().that().resideInAPackage("..domain..")
        .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "cinema.domain..");
    @ArchTest static final ArchRule application_only_depends_inward = classes().that().resideInAPackage("..application..")
        .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "cinema.domain..", "cinema.application..");
    @ArchTest static final ArchRule infrastructure_never_imports_web = noClasses().that().resideInAPackage("..infrastructure..")
        .should().dependOnClassesThat().resideInAPackage("..web..");
    @ArchTest static final ArchRule web_uses_application_boundary = noClasses().that().resideInAPackage("..web..")
        .should().dependOnClassesThat().resideInAnyPackage("..domain..", "..infrastructure..", "org.springframework.data..", "jakarta.persistence..", "java.sql..", "javax.sql..");
    @ArchTest static final ArchRule controllers_never_use_output_ports = noClasses().that().resideInAPackage("..web.controllers..")
        .should().dependOnClassesThat().resideInAPackage("..application.common.interfaces..");
    @ArchTest static final ArchRule every_class_has_a_layer = classes().should()
        .resideInAnyPackage("..domain..", "..application..", "..infrastructure..", "..web..");
}
