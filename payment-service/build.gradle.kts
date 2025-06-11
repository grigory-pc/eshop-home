plugins {
    id("java")
    id("org.openapi.generator") version "7.12.0"
}

group = "ru.ebs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

//tasks.withType<GenerateTask> {
//    generatorName.set("spring")
//    inputSpec.set("$projectDir/src/main/resources/api-spec.yaml")
//    outputDir.set("$projectDir/build/generated")
//    modelPackage.set("ru.ya.domain")
//    apiPackage.set("ru.ya.api")
//    configOptions.set(mapOf(
//            "sourceFolder" to "src/gen/java/main/codegen",
//            "interfaceOnly" to "false",
//            "library" to "spring-boot",
//            "useTags" to "true"
//    ))
//}