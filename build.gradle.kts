plugins {
	plugins {
    java
    id("org.springframework.boot") version "3.4.2" // Cambia 4.0.0 por 3.4.2
    id("io.spring.dependency-management") version "1.1.7"
}
}

group = "ec.edu.ups.icc"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// 1. Cambiar webmvc por web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // 2. Herramientas de desarrollo
    developmentOnly("org.springframework.boot:spring-boot-devtkols")
    
    // 3. Cambiar webmvc-test por starter-test (incluye todo lo necesario)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    
    // 4. Lo demás está perfecto para la práctica 10
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.postgresql:postgresql")
    
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    	// ============== NUEVAS DEPENDENCIAS DE SEGURIDAD ==============
	
	// Spring Security
	implementation("org.springframework.boot:spring-boot-starter-security")
	
	// JWT - JSON Web Token
	implementation("io.jsonwebtoken:jjwt-api:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
	
	// Jackson para manejo de fechas Java 8+ (LocalDateTime, LocalDate, etc.)
	// NECESARIO: ErrorResponse usa LocalDateTime que requiere este módulo
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	
	// Tests de seguridad
	testImplementation("org.springframework.security:spring-security-test")

}

tasks.withType<Test> {
	useJUnitPlatform()
}


tasks.withType<JavaCompile> {
	options.compilerArgs.add("-parameters")
}
