plugins {
    `java-library`
}

dependencies {
    implementation("org.ow2.asm:asm:${Versions.asm}")
    implementation("org.ow2.asm:asm-commons:${Versions.asm}")
    implementation("org.ow2.asm:asm-util:${Versions.asm}")
    implementation("org.ow2.asm:asm-tree:${Versions.asm}")
    implementation("org.ow2.asm:asm-analysis:${Versions.asm}")
}
