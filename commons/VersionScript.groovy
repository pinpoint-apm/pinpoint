println("==== Creating Version.java ====");
File mainDir = new File("${pom.basedir}/src/main");
if (mainDir.exists() && !mainDir.isDirectory()) {
    println("Main dir does not exist, wont create Version.java!");
    return;
}
File versionFile = new File("${pom.basedir}/src/main/java/com/nhn/pinpoint/common/Version.java");
if (versionFile.exists() && versionFile.isDirectory()) {
    println("Version file exists and is directory! Wont overwrite");
    return;
}
if (versionFile.exists()) {
    println("Version file already exists, overwriting!");
}
println("Creating Version.java File");
BufferedWriter writer = new BufferedWriter(new FileWriter(versionFile));

writer.write("package com.nhn.pinpoint.common;\n");
writer.write("public final class Version {\n");
writer.write("  public static final String VERSION = \"${project.version}\";\n");
writer.write("}");
writer.close();