package com.huyuhui.hyhutilskotlin.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.huyuhui.hyhutilskotlin.file.FileUtils
import kotlinx.coroutines.launch
import java.io.File

/**
 * 非常基础的选择图片裁剪返回的，都是调用系统页面操作
 */
class ImagePicker private constructor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val permissionDelegate: PermissionDelegate
) {
    // 使用LinkedHashMap保持插入顺序，记录授权过的app，被授权的Uri
    private val uriPermissionMap = linkedMapOf<Uri, MutableSet<String>>()

    private var tokePhotoUri: Uri? = null
    private var cropOutputUri: Uri? = null

    private var tokePhotoFile: File? = null
    private var selectImageFile: File? = null
    private var cropOutFile: File? = null


    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(
                source: LifecycleOwner,
                event: Lifecycle.Event
            ) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    releaseAllPermissions()
                }
            }

        })
    }

    interface PermissionDelegate {
        fun isGrandPermission(permission: String): Boolean
        fun requestPermission(permission: String, callback: (Boolean) -> Unit)
    }

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var photoPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var legacyPickerLauncher: ActivityResultLauncher<String>
    private lateinit var cropLauncher: ActivityResultLauncher<Intent>

    fun registerLaunchers(activity: ComponentActivity) {
        takePictureLauncher = activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            onPhotoTaken?.invoke(
                if (success) tokePhotoFile else null,
                if (success) tokePhotoUri else null
            )
        }

        photoPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            handleSelectedUri(uri)
        }

        legacyPickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            handleSelectedUri(uri)
        }

        cropLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onImageCropped?.invoke(
                if (result.resultCode == Activity.RESULT_OK) cropOutFile else null,
                if (result.resultCode == Activity.RESULT_OK) cropOutputUri else null
            )
            cropOutputUri?.let { revokeExternalPermissions(it) }
        }
    }

    fun registerLaunchers(fragment: Fragment) {
        takePictureLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            onPhotoTaken?.invoke(
                if (success) tokePhotoFile else null,
                if (success) tokePhotoUri else null
            )
        }

        photoPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            handleSelectedUri(uri)
        }

        legacyPickerLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            handleSelectedUri(uri)
        }

        cropLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            onImageCropped?.invoke(
                if (result.resultCode == Activity.RESULT_OK) cropOutFile else null,
                if (result.resultCode == Activity.RESULT_OK) cropOutputUri else null
            )
            cropOutputUri?.let { revokeExternalPermissions(it) }
        }
    }

    enum class ActionType {
        TokenPhoto, SelectImage, Crop
    }
    //拍照返回
    var onPhotoTaken: ((File?, Uri?) -> Unit)? = null
    //选择图片返回
    var onImageSelected: ((File?, Uri?) -> Unit)? = null
    //裁剪返回
    var onImageCropped: ((File?, Uri?) -> Unit)? = null
    //如果你想自定义图片保存的位置，需要放在app内部存储里面，并且在filepath里面给外部访问，默认是时间戳命名
    var onCreateImageFile: ((ActionType) -> File?)? = null

    fun takePhoto() {
        tokePhotoFile = createImageFile(context, ActionType.TokenPhoto)
        tokePhotoUri = createImageUri(context, tokePhotoFile)
        if (tokePhotoUri == null) {
            onPhotoTaken?.invoke(null, null)
        } else {
            if (permissionDelegate.isGrandPermission(Manifest.permission.CAMERA)) {
                takePictureLauncher.launch(tokePhotoUri!!)
            } else {
                permissionDelegate.requestPermission(Manifest.permission.CAMERA) { isGrand ->
                    if (isGrand) {
                        takePictureLauncher.launch(tokePhotoUri!!)
                    }
                }
            }
        }
    }

    fun selectImage() {
        when {
            isPhotoPickerAvailable(context) -> launchPhotoPicker()
            else -> checkAndLaunchLegacyPicker()
        }
    }

    fun cropImage(sourceUri: Uri, width: Int, height: Int) {
        cropOutFile = createImageFile(context, ActionType.Crop)
        cropOutputUri = createImageUri(context, cropOutFile)
        if (cropOutputUri == null) {
            onImageCropped?.invoke(null, null)
        } else {
            createCropIntent(sourceUri, cropOutputUri!!, width, height).also {
                grantUriPermissionForFileProvider(it, cropOutputUri!!)
                cropLauncher.launch(it)
            }
        }
    }

    private fun handleSelectedUri(uri: Uri?) {
        //从相册返回的uri需要复制一份到自己的文件下
        if (uri == null) {
            onImageSelected?.invoke(null, null)
        } else {
            lifecycleOwner.lifecycleScope.launch {
                selectImageFile = createImageFile(context, ActionType.SelectImage)
                val copyUri = createImageUri(context, selectImageFile)
                if (copyUri == null) {
                    onImageSelected?.invoke(null, null)
                } else {
                    val result = FileUtils.copyDataFromUri(context, uri, copyUri)
                    onImageSelected?.invoke(
                        if (result) selectImageFile else null,
                        if (result) copyUri else null
                    )
                }
            }
        }
    }

    private fun checkAndLaunchLegacyPicker() {
        if (permissionDelegate.isGrandPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            launchLegacyPicker()
        } else {
            permissionDelegate.requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE) { granted ->
                if (granted) launchLegacyPicker()
            }
        }
    }

    private fun launchPhotoPicker() {
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun launchLegacyPicker() {
        legacyPickerLauncher.launch("image/*")
    }

    private fun createCropIntent(sourceUri: Uri, outputUri: Uri, width: Int, height: Int): Intent {
        return Intent("com.android.camera.action.CROP").apply {
            setDataAndType(sourceUri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("outputX", width)
            putExtra("outputY", height)
            putExtra("return-data", true)
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
//            putExtra("outputFormat", Bitmap.CompressFormat.JPEG)
            putExtra("noFaceDetection", true)
            putExtra("setDraft", true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun grantUriPermissionForFileProvider(intent: Intent, uri: Uri) {
        // 获取所有可能处理该Uri的应用

        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { resolveInfo ->
                context.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                uriPermissionMap.getOrPut(uri) { mutableSetOf() }
                    .add(resolveInfo.activityInfo.packageName)
            }
    }

    private fun revokeExternalPermissions(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            uriPermissionMap[uri]?.forEach { packageName ->
                context.revokeUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            uriPermissionMap.remove(uri)
        }
    }

    fun releaseAllPermissions() {
        uriPermissionMap.keys.toList().forEach { uri ->
            revokeExternalPermissions(uri)
        }
    }

    private fun createImageFile(context: Context, actionType: ActionType): File? {
        return try {
            onCreateImageFile?.invoke(actionType) ?: File.createTempFile(
                "IMG_${System.currentTimeMillis()}",
                ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
        } catch (e: Exception) {
            Log.e(TAG, e.stackTraceToString())
            null
        }
    }


    companion object {
        fun create(
            activity: ComponentActivity,
            permissionDelegate: PermissionDelegate
        ): ImagePicker {
            return ImagePicker(activity, activity, permissionDelegate).apply {
                registerLaunchers(activity)
            }
        }

        fun create(
            fragment: Fragment,
            lifecycleOwner: LifecycleOwner,
            permissionDelegate: PermissionDelegate
        ): ImagePicker {
            return ImagePicker(
                fragment.requireContext(),
                lifecycleOwner,
                permissionDelegate
            ).apply {
                registerLaunchers(fragment)
            }
        }

        private const val TAG = "ImagePicker"
        private fun createImageUri(context: Context, file: File?): Uri? {
            return file?.let {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            }
        }
    }

}

