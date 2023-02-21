import org.cadixdev.gradle.licenser.tasks.LicenseUpdate
import java.util.Calendar

plugins {
    // https://github.com/CadixDev/licenser
    id("org.cadixdev.licenser")
}

license {
    header(project.file("HEADER.txt"))

    // use /** for kotlin files
    style.put("kt", "JAVADOC")

    // This is kinda weird in kotlin but the plugin is groovy so it works
    properties {
        this.set("year", Calendar.getInstance().get(Calendar.YEAR))
        this.set("company", "Figure Technologies")
    }

    include("**/*.kt") // Apply license header ONLY to kotlin files
}

// Ensure licenses are updated when the app is assembled
// This needs to happen early in the gradle lifecycle or else the checkLicenses task fails
tasks.named("assemble") {
    dependsOn("updateLicenses")
}

tasks.withType<LicenseUpdate>().configureEach {
    notCompatibleWithConfigurationCache("Does not work")
}
