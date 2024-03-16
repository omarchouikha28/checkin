package checkin_spring.checkin.archunit;

import checkin.CheckinSpringApplication;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

@AnalyzeClasses(packagesOf = CheckinSpringApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    ArchRule no_members_should_be_autowired = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;


    @ArchTest
    ArchRule onionTest = onionArchitecture()
            .withOptionalLayers(true)
            .domainModels("..checkin_core..")
            .applicationServices("..checkin_application..")
            .adapter("web", "..checkin_web..")
            .adapter("db", "..checkin_db..");

    @ArchTest
    ArchRule nur_controller_klassen_duerfen_auf_controller_klassen_zugreifen = noClasses()
            .that()
            .areNotAnnotatedWith(Controller.class)
            .should()
            .accessClassesThat()
            .areAnnotatedWith(Controller.class);

    @ArchTest
    ArchRule variablen_in_controller_klassen_sollen_privat_sein = fields()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Controller.class)
            .should()
            .bePrivate();

    @ArchTest
    ArchRule variablen_in_service_klassen_sollen_privat_sein = fields()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Service.class)
            .should()
            .bePrivate();


    @ArchTest
    ArchRule variablen_in_repository_klassen_sollen_privat_sein = fields()
            .that()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(Repository.class)
            .should()
            .bePrivate();

    @ArchTest
    ArchRule klassen_im_service_ordner_sind_mit_service_annotiert = classes()
            .that()
            .resideInAPackage("..appservices.services..")
            .should()
            .beMetaAnnotatedWith(Service.class);

    @ArchTest
    ArchRule klassen_im_repository_ordner_sind_mit_repository_annotiert = classes()
            .that()
            .resideInAPackage("checkin.persistence..repositories")
            .should()
            .beMetaAnnotatedWith(Repository.class);

    @ArchTest
    ArchRule klassen_im_controller_ordner_sind_mit_controller_annotiert = classes()
            .that()
            .resideInAPackage("..controller..")
            .should()
            .beMetaAnnotatedWith(Controller.class);

}
