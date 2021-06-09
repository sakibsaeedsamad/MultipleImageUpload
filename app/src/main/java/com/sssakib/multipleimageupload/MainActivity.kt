package com.sssakib.multipleimageupload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.squareup.picasso.Picasso
import com.sssakib.multipleimageupload.FileUtil.getPath
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File

//Done
class MainActivity : AppCompatActivity() {
    var selectedImage: ImageView? = null
    var files: MutableList<Uri> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv_add_image.setOnClickListener {
            requestPermission()
        }

        btnUploadImage.setOnClickListener {
            uploadImages()
        }
    }


    //===== add image in layout
    @SuppressLint("ServiceCast")
    fun addImage() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView: View = inflater.inflate(R.layout.image, null)
        // Add the new row before the add field button.
        parent_linear_layout!!.addView(rowView, parent_linear_layout!!.childCount - 1)
        parent_linear_layout!!.isFocusable
        selectedImage = rowView.findViewById(R.id.imageViewSet)
    }

    //===== select image
    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = android.app.AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Choose a Media")
        builder.setItems(options, DialogInterface.OnClickListener { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto = Intent(Intent.ACTION_GET_CONTENT)
                pickPhoto.type = "image/*"
                pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(pickPhoto, 1)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        })
        builder.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    addImage()
                    val img = data.extras!!["data"] as Bitmap?
                    selectedImage!!.setImageBitmap(img)
                    Picasso.get().load(getImageUri(this@MainActivity, img)).into(selectedImage)
                    val imgPath = getPath(this@MainActivity, getImageUri(this@MainActivity, img))
                    files.add(Uri.parse(imgPath))
                    Log.e("image", imgPath!!)
                }
                1 -> if (resultCode == RESULT_OK) {
                    if (data?.clipData != null) {
                        val mClipData = data.clipData
                        for (i in 0 until mClipData!!.itemCount) {
                            addImage()
                            val item = mClipData.getItemAt(i)
                            val uri = item.uri
                            selectedImage?.setImageURI(uri)
                            Picasso.get().load(uri).into(selectedImage)
                            val imgPath = getPath(this@MainActivity, uri!!)
                            files.add(Uri.parse(imgPath))
                        }
                    } else if (data?.data != null) {
                        addImage()
                        val uri = data.data
                        Picasso.get().load(uri).into(selectedImage)
                        val imgPath = getPath(this@MainActivity, uri!!)
                        files.add(Uri.parse(imgPath))
                    }
                }
            }
        }
    }

    //===== bitmap to Uri
    fun getImageUri(inContext: Context, inImage: Bitmap?): Uri {
        val bytes = ByteArrayOutputStream()
        inImage!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.getContentResolver(),
            inImage,
            "intuenty",
            null
        )
        Log.d("image uri", path)
        return Uri.parse(path)
    }

    //===== Upload files to server
    fun uploadImages() {

        val list: MutableList<MultipartBody.Part> = ArrayList()
        for (uri in files) {
            Log.i("uris", uri.path!!)
            list.add(prepareFilePart("files", uri))
        }

        val call: Call<ResponseBody>? = RetrofitClient
            .instance
            ?.aPI
            ?.uploadImages(list)
        call?.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable) {
                Log.i("my", t.message.toString())
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                if (response!!.isSuccessful) {
                    Toast.makeText(
                        this@MainActivity,
                        "Files uploaded successfuly",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

        })

    }

    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part {
        val file = File(fileUri.getPath())
        Log.i("here is error", file.getAbsolutePath())
        // create RequestBody instance from file
        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse("image/*"),
            file
        )

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile)
    }

    // this is all you need to grant your application external storage permision
    private fun requestPermission() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        selectImage(this@MainActivity)

                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Error occurred! ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
    }

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton(
            "GOTO SETTINGS",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
                openSettings()
            })
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

}



