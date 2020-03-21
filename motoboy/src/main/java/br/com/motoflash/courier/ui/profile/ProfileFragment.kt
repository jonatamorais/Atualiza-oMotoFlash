package br.com.motoflash.courier.ui.profile


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.ui.dialog.adapter.SimpleTextAdapter
import br.com.motoflash.core.ui.util.*
import br.com.motoflash.courier.BuildConfig

import br.com.motoflash.courier.R
import br.com.motoflash.courier.service.location.LocationService
import br.com.motoflash.courier.ui.base.BaseFragment
import br.com.motoflash.courier.ui.main.MainActivity
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.firebase.ui.auth.AuthUI
import com.google.gson.Gson
import com.pixplicity.easyprefs.library.Prefs
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : BaseFragment(), ProfileMvpView {

    @Inject
    lateinit var presenter: ProfilePresenter<ProfileMvpView>

    private lateinit var courier: Courier

    private var currentPhotoPath: String? = null

    private var logout = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK){
            if(data?.data!=null){
                currentPhotoPath = data.data.toString()
            }

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

                log("loadBitmap")
                Glide.with(context!!).asBitmap().load(rotatedBitmap).addListener(object: RequestListener<Bitmap>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        log("onLoadFailed: ${e?.message?:null} ${Gson().toJson(e)}")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        log("onResourceReady")
                        imgProfile.setImageBitmap(resource!!)
                        return true
                    }
                }).into(imgProfile)
                imgProfile.visibility = View.VISIBLE

                showLoading()

                presenter.doUpdateProfilePhoto(
                    courierId = courier.id!!,
                    courierPhoto = Uri.fromFile(FileUtils.saveBitmap(context!!, rotatedBitmap!!)).toString()
                )
            }else{
                currentPhotoPath = null
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp()
    }

    override fun onResume() {
        super.onResume()
        fragmentComponent.inject(this)
        presenter.onAttach(this)
        OPEN = true
        refreshCourier()
    }

    @SuppressLint("CheckResult")
    override fun setUp() {
        (activity!! as MainActivity).setCallback(object : MainActivity.OnCourierCallback{
            override fun onGetCourier(courier: Courier) {
                this@ProfileFragment.courier  = courier
                loadCourier()
            }
        })

        switchOnline?.setOnClickListener {
            showLoading()
            if(Prefs.getString(CURRENT_WORK_ORDER, "").isEmpty()) {

                if (switchOnline?.isChecked == true) {
                    presenter.doUpdateOnline(courierId = courier!!.id!!, online = true)
                } else
                    presenter.doUpdateOnline(
                        courierId = courier!!.id!!,
                        online = false
                    )
            }else{
                hideLoading()
                switchOnline?.isChecked = true
                "Não é possível desativar no momento, finalize sua OS atual".showSnack(container,backgroundColor =  R.color.colorRed)
            }
        }


        labelVersion.text = "Versão: ${BuildConfig.VERSION_NAME}"
        labelSystem.text = "Android: ${CommonsUtil.getAndroidVersion(Build.VERSION.SDK_INT)}"

        btnLogout.setOnClickListener {
            showLoading()
            logout = true
            presenter.doUpdateOnline(courierId = courier.id!!, online = false)
        }

        imgProfile.setOnClickListener {
            dispatchTakePictureIntent(REQUEST_PICK_IMAGE)
        }
    }

    private fun loadCourier(){
        if(OPEN){
            log("loadCourier")
            switchOnline?.isChecked = courier.online
            if(switchOnline?.isChecked == true){
                online()
            }
            else{
                offline()
            }

            imgProfile?.let{
                Glide.with(this).load(courier.profilePhoto).circleCrop().into(it)
            }
            txtLabelLocation.text = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(Prefs.getLong(LAST_LOCATION_TIME, 0L)))
            txtName.text = courier.name

            if(courier.cnh){
                labelVec.text = "${courier.currentEquipment!!.plate} - ${courier.currentEquipment!!.model} (${courier.currentEquipment!!.year})"
            }else{
                labelVec.text = "Bike"
            }
        }
    }

    private fun online(){
        if(context!=null){
            txtLabelOnline.text = "Online"
            imgLocation.setImageResource(R.drawable.ic_location_on)
            imgLocation.setColorFilter(ContextCompat.getColor(context!!, R.color.colorBlue), PorterDuff.Mode.SRC_IN)
            imgLocation.startAnimation(AlphaAnimation(1f,0f).apply {
                duration = 500
                fillAfter = true
                interpolator = DecelerateInterpolator()
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            })
            Prefs.putBoolean(COURIER_ONLINE, true)
            LocationService.startLocationServices(applicationContext = context!!)
        }
    }
    private fun offline(){
        if(context!=null && switchOnline!=null){
            txtLabelOnline.text = "Offline"
            imgLocation.setImageResource(R.drawable.ic_location_off)
            imgLocation.setColorFilter(ContextCompat.getColor(context!!, R.color.colorGray), PorterDuff.Mode.SRC_IN)
            imgLocation.clearAnimation()
            Prefs.putBoolean(COURIER_ONLINE, false)
            LocationService.stopLocationServices(applicationContext = context!!)
        }
    }

    override fun onUpdateOnline() {
        hideLoading()
        if(logout){
            LocationService.stopLocationServices(applicationContext = context!!)
            AuthUI.getInstance().signOut(context!!).addOnCompleteListener {
                if (it.isSuccessful) {
                    Prefs.clear()
                    startActivity(Intent(context!!, SplashActivity::class.java))
                    activity!!.finish()
                } else {
                    hideLoading()
                    "Falha ao sair".showSnack(container, backgroundColor = R.color.colorRed)
                }
            }
        }
        if(switchOnline.isChecked){
            "Você está online".showSnack(container, backgroundColor = R.color.colorBlue)
            online()
        }else{
            "Você está offline".showSnack(container, backgroundColor = R.color.colorRed)
            offline()
        }
    }

    override fun onUpdateFail() {
        hideLoading()
        switchOnline.isChecked = !switchOnline.isChecked

        "Falha ao atualizar".showSnack(container, backgroundColor = R.color.colorRed)
//        if(switchOnline.isChecked){
//            offline()
//        }else{
//            online()
//        }
    }

    override fun onUpdateInvalidCourier() {
        hideLoading()
        "Não é possível mudar seu status durante uma corrida".showSnack(container, backgroundColor = R.color.colorRed)
        switchOnline.isChecked = !switchOnline.isChecked

//        if(switchOnline.isChecked){
//            offline()
//        }else{
//            online()
//        }
    }

    override fun onUpdateProfilePhoto() {
        hideLoading()
        "Foto atualizada".showSnack(container, backgroundColor = R.color.colorBlue)
//        refreshCourier()
    }

    override fun onUpdateProfilePhotoFail() {
        hideLoading()
        if(courier.profilePhoto == null){
            imgProfile.setImageDrawable(null)
        }else{
            imgProfile?.let{
                Glide.with(this).load(courier.profilePhoto).circleCrop().into(it)
            }
        }
        "Falha ao atualizar".showSnack(container, backgroundColor = R.color.colorRed)
    }

    private fun showLoading(){
        ringRelevance.animateIndeterminate()
    }

    private fun hideLoading(){
        ringRelevance.stopAnimateIndeterminate()
    }

    private fun refreshCourier(){
        log("refreshCourier")
        if((activity!! as MainActivity).doGetCourier()!=null){
            courier = (activity!! as MainActivity).doGetCourier()!!
            loadCourier()
        }
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
                        "br.com.motoflash.courier.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, requestCode)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        OPEN = false
        presenter.onDetach()
    }

    companion object{
        const val REQUEST_PICK_IMAGE = 295
    }
}
