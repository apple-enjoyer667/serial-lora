plugins {
    id("java")
}

group = "com.mtgprod"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("io.github.java-native:jssc:2.9.4")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("io.github.java-native:jssc:2.10.2")
    // Source: https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("com.intelligt.modbus:jlibmodbus:1.2.9.11")
}

tasks.test {
    useJUnitPlatform()
}