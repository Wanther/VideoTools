# Introduction

Try to use gradle + kotlin as script

# Usage

Create a empty gradle project, use kotlin dsl

```bash
> cd projectDir
> git submodule add git@github.com:Wanther/videotools.git buildSrc
```

project structure:
  - buildSrc <-- videotools submodule
  - gradle
  - gradlew
  - gradlew.bat
  - gradle.properties
  - build.gradle.kts
  - settings.gradle.kts
  - project1
    - build.gradle.kts
  - project2
    - build.gradle.kts

# Configuration

add ffmpeg, ffprobe to your PATH environment variable

gradle.properties
```
DB_USER=your db username
DB_PASSWORD=your db password

OSS_ACCESS_KEY_ID=your aliyun oss key id
OSS_ACCESS_KEY_SECRET=your aliyun oss key secret

DINGTALK_ROBOT_URI=your dingtalk robt hook url
```

```kotlin
docTools {
    // source files directory & result file directory
    srcDir = file("path/to/input/directory")
    outDir = file("path/to/output/directory")

    // database, change driver dependency in buildSrc/build.gradle.kts when you use another jdbc driver
    db {
        url = "jdbc:mysql://localhost/doctools?useSSL=false&useUnicode=true&characterEncoding=UTF-8"
        driver = "com.mysql.jdbc.Driver"
    }

    // aliyun oss
    oss {
        endpoint = "http://oss-cn-beijing.aliyuncs.com"
        bucket = "your oss bucket"
        dir = "path/to/upload"
    }
}
```

# Tasks
  - initTable: create tables for current project, table name is `${projectName}_doc`
  - clearTable: truncate project table data
  - dropTable: drop project tables
  - makeIndex: scan input folder, insert file base info to tables
  - docSummary: summary of input files after makeIndex, file count/size per format .etc
  - guessCommonAttr: guess common attribute from file path
    - grade
    - stage/period
    - subject
    - title
  - setShortPath: according to input file directory structure, generate short path for each file. For example:
    - path/to1/file1 --> a/a/a
    - path/to1/file2 --> a/a/b
    - path/to2/file1 --> a/b/a
  - uploadAliOSS: upload output directory to aliyun OSS
    ```kotlin
    tasks.named<UploadAliOSS>("uploadAliOSS") {
        taskQuery = "select * from ${project.name}_doc where status=1 limit 1"
        // optional
        uploadAction = { doc, oss, extension ->
            listOf(".jpg", "-hd.mp4").forEach { suffix ->
                val ossKey = "${extension.oss.dir}/${doc.shortPath!!}$suffix"
                val localFile = file("${extension.outDir.resolve(doc.shortPath!!).absolutePath}$suffix")
                logger.quiet("[${doc.id!!}]${localFile.absolutePath} ----> $ossKey")
                oss.putObject(extension.oss.bucket, ossKey, localFile)
            }
        }
    }
    ```
  - makeCategory: guess a category for each file

# Task Types
- Unzip: unzip zip files
  ```kotlin
    tasks.register<Unzip>("unzipPaper") {
        // use gradle unzip or system unzip(linux), expand-archive(windows)
        useNative = true
        // some unicode path in zip file, native linux unzip error
        useCp936 = true
        // when unpack failed, delete it immediately
        deleteBroken = true
    }
  ```
- SqlExec: execute a sql
  ```kotlin
    tasks.register<SqlExec>("markUnused") {
        sql = "update ${project.name}_doc set status=-100 where format <> 'mp4'"
    }
  ```
- EzSqlExec: eazy version of `SqlExec`
  ```kotlin
  tasks.register<EzSqlExec>("markUnused") {
      set = "status=-100"
      where = "format <> 'mp4'"
  }
  ```
- SendDingtalkMessage: notify Dingtalk robot
  ```kotlin
  tasks.register<SendDingtalkMessage>() {
    message = "${project.name} upload success!"
  }
  ```

## Video
  - ExtractVideoInfo: if input files containes video, extract video's duration,format,width,height,video bitrate,audio bitrate.. with ffprobe
  - ExtractThumb: generate a thumb for each video
    ```kotlin
    tasks.register<ExtractThumb>("extractThumb") {
        time = "8"
    }
    ```
  - ConvertVideo: convert source video to expected format

## Paper
  - GroupPackage: group unzip files into a package
  - GuessPaperAttr: kind,year,province

# API
## Db
```kotlin
Db.withConnection { connection ->
    // connection is JDBC Connection
}

Db.withInstance { sql ->
    val result = sql.query("select * from ${project.name}_doc where format='mp4'") { rs ->
        // rs is JDBC's ResultSet
        while(rs.next()) {
            // ...
        }
    }

    val entities = sql.rows("select * from xxx where attr=value") { rs ->
        // deserialize rs to entity
    }

    sql.insert("insert into t_table(attr1, attr2) values(?, ?)") { ps ->
        // ps is JDBC's PreparedStatement
    }

    sql.withBatch(1000, "update t_xxx set attr1=value1 where attr2=value2") { ps ->
        // use ps as normal, proxy will flush automatically
    }
}

Db.withResultSet("select * from t_table where attr=value") { rs ->
    while(rs.next()) {
        // ...
    }
}

Db.withResult("select * from t_table where attr=value") { rs->
    // ...
}

Db.withPs("insert into t_table(attr1, attr2) value(?,?)", 2000) { ps ->
    // use ps normally, proxy will auto flush every 2000(batchSize)
}
```

## Video

### preset convert quanlity:
  - ConvertTemplate.NHD: 640x360, 400k(V) + 64k(A) bitrate
  - ConvertTemplate.QHD: 960x540, 900k(V) + 96k(A) bitrate
  - ConvertTemplate.HD: 720p, 1500k(V) + 128k(A) bitrate
  - ConvertTemplate.FHD: 1080p, 3000k(V) + 160k(A) bitrate

### Tool Implementation
  - VideoToolFFmpeg: use ffmpeg normal commands(CPU)
  - VideoToolFFmpegNvidia: use ffmpeg hwaccel commands(GPU)