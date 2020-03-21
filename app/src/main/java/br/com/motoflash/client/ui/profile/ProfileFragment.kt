package br.com.motoflash.client.ui.profile


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseFragment
import br.com.motoflash.client.ui.login.LoginMvpView
import br.com.motoflash.client.ui.main.MainActivity
import br.com.motoflash.client.ui.splash.SplashActivity
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.core.data.network.model.User
import br.com.motoflash.core.ui.util.*
import com.asksira.bsimagepicker.BSImagePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment(), ProfileMvpView, BSImagePicker.OnSingleImageSelectedListener, BSImagePicker.ImageLoaderDelegate {

    @Inject
    lateinit var presenter: ProfileMvpPresenter<ProfileMvpView>

    private lateinit var user: User

    private var loading = false
    private var editMode = false
    private var updatePasswordMode = false

    private var currentPhotoPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onSingleImageSelected(uri: Uri?, tag: String?) {
        log("onSingleImageSelected ${File(uri.toString()).exists()}")

        currentPhotoPath = uri.toString()
        val file = File(currentPhotoPath!!)
        val bmOptions =  BitmapFactory.Options()
        val bitmap = BitmapFactory.decodeFile(file.absolutePath,bmOptions)

        profilePhoto.imageTintMode = null
        Glide.with(context!!).applyDefaultRequestOptions(RequestOptions().circleCrop()).load(bitmap).into(profilePhoto)
    }

    override fun loadImage(imageFile: File?, ivImage: ImageView?) {
        log("loadImage ${imageFile!!.exists()}")


//        val ei = ExifInterface(currentPhotoPath!!)
//        val orientation = ei.getAttributeInt(
//            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.ORIENTATION_UNDEFINED)
//
//
//        var rotatedBitmap: Bitmap? = null
//        when (orientation) {
//
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
//                TransformationUtils.rotateImage(bitmap, 90)
//
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
//                TransformationUtils.rotateImage(bitmap, 180)
//
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
//                TransformationUtils.rotateImage(bitmap, 270)
//
//            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
//            else -> rotatedBitmap = bitmap
//        }


//        currentPhotoPath = Uri.fromFile(FileUtils.saveBitmap(context!!, rotatedBitmap!!)).toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(ImagePicker.shouldHandle(requestCode, resultCode, data)){
            val image = ImagePicker.getFirstImageOrNull(data)
            log("picker: ${File(image.path).exists()} ${image.path}")

            currentPhotoPath = image.path

            if(File(currentPhotoPath!!).exists()){
                log("file exist")
                val file = File(currentPhotoPath!!)
                val bmOptions =  BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions)

                val ei = ExifInterface(currentPhotoPath!!)
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)


                var rotatedBitmap: Bitmap? = null
                when (orientation) {

                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 90)

                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 180)

                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 270)

                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                    else -> rotatedBitmap = bitmap
                }

                profilePhoto.imageTintMode = null
                Glide.with(context!!).applyDefaultRequestOptions(RequestOptions().circleCrop()).load(rotatedBitmap).into(profilePhoto)

                currentPhotoPath = Uri.fromFile(FileUtils.saveBitmap(context!!, rotatedBitmap!!)).toString()
            }
        }
        if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK){
            if(data?.data!=null){
                log("data!=null")
                log("currentPhotoPath: ${File(currentPhotoPath!!).exists()} ${File(ImageUtils.getPickImageResultUri(context!!, data).toString()).exists()} ${File(data.data.toString()).exists()}")
//                currentPhotoPath = ImageUtils.getPickImageResultUri(context!!, data).toString()
//                log("currentPhotoPath: $currentPhotoPath")
            }

            if(File(currentPhotoPath!!).exists()){
                log("file exist")
                val file = File(currentPhotoPath!!)
                val bmOptions =  BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions)

                val ei = ExifInterface(currentPhotoPath!!)
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)


                var rotatedBitmap: Bitmap? = null
                when (orientation) {

                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 90)

                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 180)

                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                        TransformationUtils.rotateImage(bitmap, 270)

                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                    else -> rotatedBitmap = bitmap
                }

                profilePhoto.imageTintMode = null
                Glide.with(context!!).applyDefaultRequestOptions(RequestOptions().circleCrop()).load(rotatedBitmap).into(profilePhoto)

                currentPhotoPath = Uri.fromFile(FileUtils.saveBitmap(context!!, rotatedBitmap!!)).toString()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentComponent.inject(this)
        presenter.onAttach(this)

        if(!editMode && !updatePasswordMode)
            setUp()
    }

    override fun setUp() {

        if((activity!! as MainActivity).getCurrentUser()==null){
            Timer("dealy").schedule(1000L){
                activity?.runOnUiThread {
                    setUp()
                }
            }
            return
        }

        user = (activity!! as MainActivity).getCurrentUser()!!
        user.profilePhoto?.let {url ->
            if(url.isNotBlank()){
                profilePhoto.imageTintMode = null
                Glide.with(context!!).applyDefaultRequestOptions(RequestOptions().circleCrop()).load(url).into(profilePhoto)
            }
        }
        txtUserName.text = user.name!!
        txtUserEmail.text = user.email!!

        btnSingOut.setOnClickListener {
            presenter.doSingOutUser()
        }

        val mobilePhoneMask = Mask.insert(Mask.MOBILE_PHONE_MASK, edtMobilePhone)
        edtMobilePhone.addTextChangedListener(mobilePhoneMask)

        btnEdit.setOnClickListener {
            val inputManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                activity!!.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )

            if(!loading){
                if(!updatePasswordMode){
                    user = (activity!! as MainActivity).getCurrentUser()!!
                    edtEmail.setText(user.email!!)
                    edtName.setText(user.name!!)
                    edtMobilePhone.setText(user.mobilePhone!!.substring(3))

                    if(editMode){
                        btnUpdatePassword.showAlpha()
                        containerUpdateProfile.hideItems(200)
                        btnEdit.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_edit))
                        editClickProfilePhoto.hideAlpha()
                        user.profilePhoto?.let {url ->
                            if(url.isNotBlank()){
                                profilePhoto.imageTintMode = null
                                Glide.with(context!!).applyDefaultRequestOptions(RequestOptions().circleCrop()).load(url).into(profilePhoto)
                            }
                        }
                    }else{
                        btnUpdatePassword.hideAlpha()
                        containerUpdateProfile.visibility = View.VISIBLE
                        containerUpdateProfile.showItems()
                        btnEdit.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_close))
                        editClickProfilePhoto.showAlpha()
                    }
                    editMode = !editMode
                }else{
                    "Finalize a edição da senha!".showSnack(container, backgroundColor = R.color.colorRed)
                }
            }else{
                "Espere terminar de carregar".showSnack(container, backgroundColor = R.color.colorRed)
            }
        }

        btnUpdatePassword.setOnClickListener {
            val inputManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                activity!!.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )

            if(!loading){
                if(!editMode){
                    if(updatePasswordMode){
                        btnUpdatePassword.text = "Alterar Senha"
                        btnUpdatePassword.setTextColor(ContextCompat.getColor(context!!, R.color.colorPurple))
                        btnUpdatePassword.setBackgroundResource(R.drawable.rectangle_cornes_white_borders_purple)
                        containerUpdatePassword.hideItems(200)
                    }else{
                        btnUpdatePassword.text = "Cancelar"
                        btnUpdatePassword.setTextColor(ContextCompat.getColor(context!!, R.color.colorRed))
                        btnUpdatePassword.setBackgroundResource(R.drawable.rectangle_cornes_white_borders_red)
                        containerUpdatePassword.visibility = View.VISIBLE
                        containerUpdatePassword.showItems(300)
                    }
                    updatePasswordMode = !updatePasswordMode
                }else{
                    "Finalize a edição do seu perfil!".showSnack(container, backgroundColor = R.color.colorRed)
                }
            }else{
                "Espere terminar de carregar".showSnack(container, backgroundColor = R.color.colorRed)
            }
        }

        btnNext.setOnClickListener {
            val inputManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                activity!!.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            showLoading()

            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val mobilePhone = edtMobilePhone.text.toString().unMaskOnlyNumbers()

            if(email.isNotBlank() && email.contains(".") && email.contains("@")){
                if(name.isNotBlank()){
                    if(mobilePhone.length == 11){
                        if(currentPhotoPath != null || user.profilePhoto!=null){
                            presenter.doUpdateProfile(
                                userId = user.id!!,
                                email = email,
                                name = name,
                                mobilePhone = String.format("+55%s",mobilePhone.unMaskOnlyNumbers()),
                                profilePhoto = currentPhotoPath?:user.profilePhoto!!,
                                updatePhoto = currentPhotoPath!=null
                            )
                        }else{
                            hideLoading()
                            "Selecione uma foto de perfil".showSnack(container, backgroundColor = R.color.colorRed)
                        }
                    }else{
                        hideLoading()
                        "Número de celular inválido".showSnack(container, backgroundColor = R.color.colorRed)
                    }
                }else{
                    hideLoading()
                    "Nome inválido".showSnack(container, backgroundColor = R.color.colorRed)
                }
            }else{
                hideLoading()
                "E-mail inválido".showSnack(container, backgroundColor = R.color.colorRed)
            }
        }

        btnNextPassword.setOnClickListener {
            val inputManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                activity!!.currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            showLoading()

            val password = edtPassword.text.toString()
            val confirmPassword = edtPasswordConfirm.text.toString()

            if(password.length > 5){
                if(password == confirmPassword){
                    presenter.doUpdatePassword(
                        userId = user.id!!,
                        password = password
                    )
                }else{
                    hideLoading()
                    "As senhas devem ser iguais!".showSnack(container, backgroundColor = R.color.colorRed)
                }
            }else{
                hideLoading()
                "Senha muito fraca!".showSnack(container, backgroundColor = R.color.colorRed)
            }
        }


        editClickProfilePhoto.setOnClickListener {
            RxPermissions(this)
                .request(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
                .subscribe { granted ->
                    if(granted){
//                        val url = ImageUtils.getCaptureImageOutputUri(context!!)
//                        currentPhotoPath = url.toString()
//                        startActivityForResult(ImageUtils.getPickImageChooserIntent(context!!, url), REQUEST_PICK_IMAGE)

//                        val singleSelectionPicker = BSImagePicker
//                            .Builder("br.com.motoflash.client.fileprovider")
//                            .setTag("TakeProfile")
//                            .build()
//                        singleSelectionPicker.show(childFragmentManager, "picker")

                        ImagePicker.create(this)
                            .returnMode(ReturnMode.ALL) // set whether pick and / or camera action should return immediate result or not.
                            .folderMode(true) // folder mode (false by default)
                            .toolbarFolderTitle("Arquivos") // folder selection title
                            .toolbarImageTitle("Clique para selecionar") // image selection title
                            .toolbarArrowColor(Color.WHITE) // Toolbar 'up' arrow color
                            .includeVideo(false) // Show video on image picker
                            .single() // single mode
                            .showCamera(true) // show camera or not (true by default)
                            .imageDirectory("Camera") // directory name for captured image  ("Camera" folder by default)
                            .start() // start image picker activity with request code
                    }else{
                        AlertUtil.showAlertNeutral(
                            context = activity!!,
                            title = "Permissões",
                            message = "É necessário aceitar as permissões!"
                        )
                    }
                }
        }
    }

    override fun onSingOutUser() {
        activity!!.startActivity(Intent(activity!!, SplashActivity::class.java))
        activity!!.finish()
    }

    override fun onSingOutFail() {

    }

    override fun onUpdatePassword() {
        btnUpdatePassword.text = "Alterar Senha"
        btnUpdatePassword.setTextColor(ContextCompat.getColor(context!!, R.color.colorPurple))
        btnUpdatePassword.setBackgroundResource(R.drawable.rectangle_cornes_white_borders_purple)
        containerUpdatePassword.hideItems(200)

//        "Senha Atualizada".showSnack(container, backgroundColor = R.color.colorBlue)
        showMessage("Realize o login com sua nova senha")
        activity!!.startActivity(Intent(activity!!, SplashActivity::class.java))
        activity!!.finish()
    }

    override fun onUpdatePasswordFail() {
        "Erro ao tentar alterar sua senha, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onUpdateProfile() {
        hideLoading()
        editMode = false
        containerUpdateProfile.hideItems(200)
        btnEdit.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_edit))
        editClickProfilePhoto.hideAlpha()
        txtUserName.text = edtName.text.toString()
        txtUserEmail.text = edtEmail.text.toString()
        btnUpdatePassword.showAlpha()

        "Dados atualizados!".showSnack(container, backgroundColor = R.color.colorPurple)
    }

    override fun onUpdateProfileError(errorCode: ErrorCode) {
        if(errorCode.field == "mobilePhone"){
            "Número de celular já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }

        if(errorCode.field == "email"){
            "E-mail já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }
        hideLoading()
    }

    override fun onUpdateProfileFail() {
        "Erro ao tentar alterar seu perfil, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    private fun hideLoading(){
        progress.visibility = View.GONE
        progressPassword.visibility = View.GONE
        loading = false
    }

    private fun showLoading(){
        progress.visibility = View.VISIBLE
        progressPassword.visibility = View.VISIBLE
        loading = true
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent(requestCode: Int) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    log(ex.message?:"Error: null")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        context!!,
                        "br.com.motoflash.client.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    companion object {
        const val REQUEST_PICK_IMAGE = 2224
    }

}
