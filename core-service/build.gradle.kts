plugins {
    id("java")
    id("org.openapi.generator") version "7.12.0"
}
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.hibernate.orm:hibernate-core")
    implementation("commons-fileupload:commons-fileupload:1.5")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-h2")
    implementation("org.springframework.data:spring-data-r2dbc")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.skyscreamer:jsonassert")
    testImplementation("org.projectlombok:lombok")

    testAnnotationProcessor("org.projectlombok:lombok")
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
