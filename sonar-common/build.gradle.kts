dependencies {
  compileOnly(project(":api"))
  implementation(project(":captcha"))

  compileOnly(rootProject.libs.adventure.nbt)

  implementation(rootProject.libs.capja)
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
