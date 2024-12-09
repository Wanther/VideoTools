plugins{
    `kotlin-dsl`
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

dependencies {
    implementation("mysql:mysql-connector-java:5.1.9")
    implementation("com.aliyun.oss:aliyun-sdk-oss:3.12.0")
    implementation("org.apache.poi:poi:4.1.2")
    implementation("org.apache.poi:poi-scratchpad:4.1.2")
    implementation("org.apache.poi:poi-ooxml:4.1.2")
    implementation("com.hankcs:hanlp:portable-1.7.8")
    implementation("org.json:json:20200518")
}
