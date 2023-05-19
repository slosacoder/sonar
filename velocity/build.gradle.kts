repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/") // Velocity
}

dependencies {
    implementation(project(":sonar-api"))
    implementation(project(":sonar-common"))

    compileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    testCompileOnly("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    testAnnotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17