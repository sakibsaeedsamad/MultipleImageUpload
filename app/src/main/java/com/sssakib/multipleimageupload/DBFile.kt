package com.sssakib.multipleimageupload

import com.google.gson.annotations.SerializedName

class DBFile {

    @SerializedName("id")
    var id: String? = null
    @SerializedName("fileName")
    var fileName: String? = null
    @SerializedName("fileType")
    var fileType: String? = null
    @SerializedName("data")
    lateinit var data: ByteArray

    constructor() {}
    constructor(fileName: String?, fileType: String?, data: ByteArray) {
        this.fileName = fileName
        this.fileType = fileType
        this.data = data
    }
}