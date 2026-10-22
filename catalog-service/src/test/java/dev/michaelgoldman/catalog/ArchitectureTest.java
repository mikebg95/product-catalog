package dev.michaelgoldman.catalog;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.michaelgoldman.catalog", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domainIsPureJava = classes()
		.that().resideInAPackage("..catalog.domain..")
		.should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..catalog.domain..");

	// application may only use java, the domain, its own ports, and @Service / @Transactional
	@ArchTest
	static final ArchRule applicationOnlyUsesDomainAndSpringAnnotations = classes()
		.that().resideInAPackage("..catalog.application..")
		.should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..catalog.domain..", "..catalog.application..",
				"org.springframework.stereotype..", "org.springframework.transaction.annotation..");

	@ArchTest
	static final ArchRule coreDoesNotKnowAdapters = noClasses()
		.that().resideInAnyPackage("..catalog.domain..", "..catalog.application..")
		.should().dependOnClassesThat().resideInAPackage("..catalog.adapter..");

	@ArchTest
	static final ArchRule onlyWebAdapterUsesGeneratedApi = noClasses()
		.that().resideOutsideOfPackages("..catalog.adapter.in.web..", "..catalog.api..")
		.should().dependOnClassesThat().resideInAPackage("..catalog.api..");

	@ArchTest
	static final ArchRule adaptersDoNotKnowEachOther = slices()
		.matching("..catalog.adapter.(*).(*)..")
		.should().notDependOnEachOther()
		.allowEmptyShould(true); // no adapters yet
}
