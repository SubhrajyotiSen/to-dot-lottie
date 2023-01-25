package com.github.theapache64.todotlottie

import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

suspend fun main(args: Array<String>) {
    val projectDir = File(System.getProperty("user.dir"))

    projectDir.walk()
        .forEach { jsonFile ->
            val isAndroidRawFile = !jsonFile.path.contains("/build/") &&
                    jsonFile.path.contains("/raw/") &&
                    jsonFile.extension == "json"
            if (
                isAndroidRawFile // TODO || iOSLottieFile || webLottieFile
            ) {
                val json = JSONObject(jsonFile.readText())
                val isLottieJson = json.has("v")
                if (isLottieJson) {
                    convertToDotLottie(jsonFile)
                    jsonFile.delete()
                    println("------------------------------")
                }else {
                    println("⚠️ Not a lottie JSON. Skipping ${jsonFile.absolutePath}")
                }
            }
        }
}

suspend fun convertToDotLottie(
    jsonFile: File
): File {
    val fileUrl = uploadFile(jsonFile)
    val convertedUrl = convert(fileUrl)
    return download(jsonFile, convertedUrl)
}

fun download(jsonFile: File, convertedUrl: String): File {
    println("⬇️ Downloading...")
    val targetFile = File("${jsonFile.parent}${File.separator}${jsonFile.nameWithoutExtension}.lottie")
    targetFile.delete()
    URL(convertedUrl).openStream().use { input ->
        FileOutputStream(
            targetFile
        ).use { output ->
            input.copyTo(output)
        }
    }

    return targetFile.also {
        println("✅ Downloaded -> ${it.absolutePath}")
    }
}

suspend fun convert(fileUrl: String): String {
    println("🔀 Converting '$fileUrl'...")
    val fileName = convertApi.toDotLottie(
        ConvertRequest(
            url = fileUrl
        )
    ).file

    return "https://lottie-editor-api-temp.s3.amazonaws.com/$fileName".also {
        println("✅ Converted -> $it")
    }
}

suspend fun uploadFile(jsonFile: File): String {
    println("🆙 Uploading '${jsonFile.name}...'")
    val respJson = uploadApi.tempFileUpload(
        UploadRequest(
            payload = jsonFile.readText()
        )
    )
    return respJson.payload.dataFile.also {
        println("✅ Uploaded -> $it")
    }
}
