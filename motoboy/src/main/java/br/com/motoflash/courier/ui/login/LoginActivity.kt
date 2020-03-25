package br.com.motoflash.courier.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.Equipment
import br.com.motoflash.core.data.network.model.ErrorCode
import br.com.motoflash.core.ui.util.*
import br.com.motoflash.courier.R
import br.com.motoflash.courier.ui.base.BaseActivity
import br.com.motoflash.courier.ui.main.MainActivity
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LoginActivity : BaseActivity(), LoginMvpView {

    @Inject
    lateinit var presenter: LoginMvpPresenter<LoginMvpView>

    private var checkedEmail = false
    private var newCourier = false

    private var currentPhotoPath: String? = null
    private var currentChnPhoto: String? = null
    private var currentRgPhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activityComponent.inject(this)
        presenter.onAttach(this)

        setUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CNH_PHOTO){
            if(resultCode == Activity.RESULT_OK){
                if(File(currentPhotoPath!!).exists()){
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

                    currentPhotoPath = null
                    btnHasPhoto.isChecked = true
                    btnHasPhoto.setTextColor(ContextCompat.getColor(this,R.color.colorBlue))
                    if(btnMotorcycle.isChecked){
                        currentChnPhoto = Uri.fromFile(FileUtils.saveBitmap(this, rotatedBitmap!!)).toString()
                    }else{
                        currentRgPhoto = Uri.fromFile(FileUtils.saveBitmap(this, rotatedBitmap!!)).toString()
                    }
                }else{
                    currentPhotoPath = null
                }
            }else{
                currentPhotoPath = null
                currentChnPhoto = null
                currentRgPhoto = null
                btnHasPhoto.isChecked = false
                btnHasPhoto.setTextColor(ContextCompat.getColor(this,R.color.colorGray))
            }
        }
    }
    @SuppressLint("CheckResult")
    override fun setUp() {

        RxPermissions(this)
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .subscribe { granted ->
                if(granted){

                }else{
                    finish()
                }
            }

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0!=null){
                    val text = p0.toString()
                    if(text.contains('@') && text.contains('.')){
                        btnNext.visibility = View.VISIBLE
                    }else{
                        btnNext.visibility = View.GONE
                    }
                }else{
                    btnNext.visibility = View.GONE
                }
            }
        })

        val mobilePhoneMask = Mask.insert(Mask.MOBILE_PHONE_MASK, edtMobilePhone)
        edtMobilePhone.addTextChangedListener(mobilePhoneMask)

        val birthDateMask = Mask.insert(Mask.BIRTH_DATE, edtBirthDate)
        edtBirthDate.addTextChangedListener(birthDateMask)

        btnMotorcycle.setOnClickListener {
            if(btnMotorcycle.isChecked){
                btnMotorcycle.setText("Motoboy")
                btnHasPhoto.setText("Foto CNH")
                inputCnhNumber.showScale()
                inputBrand.showScale()
                inputModel.showScale()
                inputYear.showScale()
                inputPlate.showScale()
            }else{
                btnMotorcycle.setText("Bike")
                btnHasPhoto.setText("Foto Documento Pessoal")
                inputCnhNumber.hideScale()
                inputBrand.hideScale()
                inputModel.hideScale()
                inputYear.hideScale()
                inputPlate.hideScale()
            }
        }

        containerHasPhoto.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_CNH_PHOTO)
        }

        btnNext.setOnClickListener {
            val inputManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            showLoading()
            if(checkedEmail){
                val email = edtEmail.text.toString()
                val password = edtPassword.text.toString()

                if(newCourier){
                    val confirmPassword = edtPasswordConfirm.text.toString()
                    val name = edtName.text.toString()
                    val mobilePhone = edtMobilePhone.text.toString()
                    val birthdate = edtBirthDate.text.toString()
                    val cnhNumber = edtCNH.text.toString()
                    val brand = edtBrand.text.toString()
                    val model = edtModel.text.toString()
                    val plate = edtPlate.text.toString()
                    val year = edtYear.text.toString()

                    if(email.isNotBlank()){
                        if(password.length > 5){
                            if(password == confirmPassword){
                                if(name.isNotBlank()){
                                    if(mobilePhone.length == 15){
                                        if(birthdate.length == 10){
                                            if(btnMotorcycle.isChecked){
                                                if(currentChnPhoto == null){
                                                    hideLoading()
                                                    showSnackError("Tire uma foto da sua CNH")
                                                    return@setOnClickListener
                                                }
                                                if(cnhNumber.isBlank()){
                                                    hideLoading()
                                                    showSnackError("Informe o número da sua CNH")
                                                    return@setOnClickListener
                                                }
                                                if(brand.isBlank()){
                                                    hideLoading()
                                                    showSnackError("Informe a Marca da sua Moto")
                                                    return@setOnClickListener
                                                }
                                                if(model.isBlank()){
                                                    hideLoading()
                                                    showSnackError("Informe o Modelo da sua Moto")
                                                    return@setOnClickListener
                                                }
                                                if(year.length != 4){
                                                    hideLoading()
                                                    showSnackError("Ano Inválido")
                                                    return@setOnClickListener
                                                }
                                                if(plate.isBlank()){
                                                    hideLoading()
                                                    showSnackError("Informe a Placa da sua Moto")
                                                    return@setOnClickListener
                                                }
                                            }else{
                                                if(currentRgPhoto == null){
                                                    hideLoading()
                                                    showSnackError("Tire uma foto de seu documento de identidade")
                                                    return@setOnClickListener
                                                }
                                            }

                                            val cal = Calendar.getInstance()

                                            //0123456789
                                            //21-08-2000
                                            cal.set(birthdate.substring(6).toInt(),birthdate.substring(3,5).toInt(),birthdate.substring(0,2).toInt(),0,0,0)
                                            val courier = Courier(
                                                name = name,
                                                email = email,
                                                password = password,
                                                cnh = btnMotorcycle.isChecked,
                                                mobilePhone = String.format("+55%s",mobilePhone.unMaskOnlyNumbers()),
                                                birthdate = cal.timeInMillis / 1000
                                            )
                                            if(btnMotorcycle.isChecked){
                                                courier.cnhNumber = cnhNumber
                                                courier.cnhDoc = currentChnPhoto
                                                courier.currentEquipment = Equipment(
                                                    brand = brand,
                                                    model = model,
                                                    year = year.toLong(),
                                                    plate = plate
                                                )
                                            }else{
                                                courier.rgDoc = currentRgPhoto
                                            }
                                            presenter.doCreatedCourier(courier)
                                        }else{
                                            hideLoading()
                                            showSnackError("Data de nascimento inválida")
                                        }
                                    }else{
                                        hideLoading()
                                        showSnackError("Número de telefone inválido")
                                    }
                                }else{
                                    hideLoading()
                                    showSnackError("Preencha o nome")
                                }
                            }else{
                                hideLoading()
                                showSnackError("As senhas precisam ser iguais")
                            }
                        }else{
                            hideLoading()
                            showSnackError("A senha precisa ter no mínimo 6 caracteres")
                        }
                    }else{
                        hideLoading()
                        showSnackError("Preencha o e-mail")
                    }
                }else{
                    if(edtPassword.text.toString().isNotBlank()){
                        presenter.doLoginCourier(edtEmail.text.toString(), edtPassword.text.toString())
                    }else{
                        hideLoading()
                        showSnackError("Informe uma senha!")
                    }
                }
            }else{
                presenter.doVerifyCourier(edtEmail.text.toString())
            }
        }
    }

    override fun onVerfyCourier(exists: Boolean) {
        hideLoading()
        checkedEmail = true
        newCourier = !exists
        if(newCourier){
            "Vamos criar uma nova conta!".showSnack(container, backgroundColor = R.color.colorBlue)
            for (i in 0 until containerLoginFields.childCount) {
                val v = containerLoginFields.getChildAt(i)

                if(v.scaleX == 0f && v !is ProgressBar){
                    v.visibility = View.VISIBLE
                    val viewAnimator: ViewPropertyAnimatorCompat = ViewCompat.animate(v)
                        .scaleY(1f).scaleX(1f)
                        .setStartDelay(100L * i)
                        .setDuration(500)

                    viewAnimator.setInterpolator(DecelerateInterpolator()).start()
                }
            }
            imageView.hideAlpha()
            txtTitle.hideAlpha()
        }else{
            "Bem-vindo de volta, informe suas credenciais!".showSnack(container, backgroundColor = R.color.colorBlue)
            inputPassword.visibility = View.VISIBLE
            val viewAnimator: ViewPropertyAnimatorCompat = ViewCompat.animate(inputPassword)
                .scaleY(1f).scaleX(1f)
                .setDuration(500)
            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }
    }

    override fun onLoginCourier() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onLoginFailGeneric() {
        hideLoading()
        "Falha ao realizar login, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onLoginInvalidCredentials() {
        "Credenciais inválidos, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onCreatedCourier() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreatedCourierFail() {
        "Falha ao criar sua conta, tente novamente".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onCreatedCourierInvalidCredentials(errorCode: ErrorCode) {
        if(errorCode.field == "mobilePhone"){
            "Número de celular já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }

        if(errorCode.field == "email"){
            "E-mail já cadastrado!".showSnack(container, backgroundColor = R.color.colorRed)
        }
        hideLoading()
    }

    override fun hideLoading() {
        progress.hideAlpha()
        btnNext.showAlpha()
    }

    override fun showLoading() {
        progress.showAlpha()
        btnNext.hideAlpha()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
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
            takePictureIntent.resolveActivity(this.packageManager)?.also {
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
                        this,
                        "br.com.motoflash.courier.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    companion object{
        const val REQUEST_CNH_PHOTO = 297
    }
}
