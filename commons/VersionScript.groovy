File mainDir = new File("${pom.basedir}/src/main");
if (mainDir.exists() && !mainDir.isDirectory()) {
    println("Main dir does not exist, wont create Version.java!");
    return;
}
final File versionFile = new File("${pom.basedir}/src/main/java/com/navercorp/pinpoint/common/Version.java");
if (versionFile.exists() && versionFile.isDirectory()) {
    println("Version file exists and is directory! Wont overwrite");
    return;
}
final String versionField = "    public static final String VERSION = \"${project.version}\";";
if (versionFile.exists()) {
    FileInputStream fileInputStream = new FileInputStream(versionFile);
    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF8");
    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    String versionCheck;
    while ((versionCheck = bufferedReader.readLine()) != null) {
        if (versionCheck.contains(versionField)) {
            println("Version field already exist.");
            return;
        }
    }

    println("Version file already exists, overwriting!");
}
println("Creating Version.java File");
BufferedWriter writer = new BufferedWriter(new FileWriter(versionFile));

writer.write("package com.navercorp.pinpoint.common;\n");
writer.write("public final class Version {\n");
writer.write(versionField);
writer.write('\n');
writer.write("}");
writer.close();

