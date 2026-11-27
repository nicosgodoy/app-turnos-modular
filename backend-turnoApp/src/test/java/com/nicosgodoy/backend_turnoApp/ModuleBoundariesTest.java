package com.nicosgodoy.backend_turnoApp;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.nicosgodoy.backend_turnoApp", importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

	@ArchTest
	static final ArchRule modulosSinCiclos = slices()
			.matching("com.nicosgodoy.backend_turnoApp.(*)..")
			.should().beFreeOfCycles();

	@ArchTest
	static final ArchRule controladoresNoAccedenARepositorios = noClasses()
			.that().resideInAPackage("..controller..")
			.should().dependOnClassesThat().resideInAPackage("..repository..");
}
