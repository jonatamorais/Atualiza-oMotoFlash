package br.com.motoflash.courier.ui.workorder.alert

import android.animation.ValueAnimator
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.util.*
import br.com.motoflash.courier.R
import br.com.motoflash.courier.ui.base.BaseActivity
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.google.maps.android.ui.IconGenerator
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_alert.*
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class AlertActivity : BaseActivity(), AlertMvpView {

    @Inject
    lateinit var presenter: AlertMvpPresenter<AlertMvpView>

    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap

    private lateinit var valueAnimator: ValueAnimator

    private var currentValueAnimatior = 0f

    private var paused = false

    val timeToAccept by lazy {
        intent.getStringExtra(PARAM_ACCEPTTIME).toLong() / 1000
    }

    val distance by lazy {
        intent.getStringExtra(PARAM_DISTANCE).toDouble()
    }

    private var accept = false
    private var denie = false

    val workOrderId by lazy {
        intent.getStringExtra(PARAM_WORKORDER_ID)
    }

    private var pressedAccept = false
    private var pressedDismiss = false
    private val animationDuration = 1000L

    private var timeoutAlertInSeconds = 30L
    private lateinit var mMediaPlayer: MediaPlayer

    private var workOrder: WorkOrder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)
        activityComponent.inject(this)
        presenter.onAttach(this)

        mMediaPlayer = MediaPlayer.create(this, R.raw.alert)
        mMediaPlayer.isLooping = true

        mMapView = findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            log("MapError: ${e.message}")
        }

        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        Prefs.putString(
            DEVICE_ID,
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        )

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            log("TOKEN CERTO DO DEVICE: ${it.id} ${it.token}")
            Prefs.putString(DEVICE_MESSAGE_ID, it.token)
        }

        setUp()
    }

    override fun setUp() {
        mMapView.isFocusable = false
        mMapView.getMapAsync {
            googleMap = it

            val padding = CommonsUtil.getPx(this, 20f).toInt()
            val positions = intArrayOf(0, 0)
            topCardView.getLocationOnScreen(positions)
            googleMap.setPadding(padding, padding, padding, CommonsUtil.getPx(this, 255f).toInt())

            log("getMapAsync")

            googleMap.setOnMarkerClickListener {
                return@setOnMarkerClickListener false
            }

            googleMap.setOnMapLoadedCallback {
                log("setOnMapLoadedCallback")
//                loaderMaps?.visibility = View.GONE
                Timer("delay").schedule(1000L) {
                    runOnUiThread {
                        if (workOrder != null)
                            boundMap(workOrder!!)
                    }
                }
            }

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_silver))
        }

        valueAnimator = ValueAnimator.ofFloat(*floatArrayOf(0f, timeToAccept.toFloat()))
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 1000L * timeToAccept
        valueAnimator.addUpdateListener {
            currentValueAnimatior = valueAnimator?.animatedFraction!!
            val progress = 100 - (currentValueAnimatior * 100).toInt()
            progressBar.progress = progress
            txtTimeCount.text = String.format("%ds", (progress * timeToAccept / 100).toInt())

            if (valueAnimator.animatedFraction >= 1) {
                containerProgress.visibility = View.GONE
                if (!accept && !denie) {
//                    startLoadAccept()
//                    showToast("Os Ignorada")
//                    presenter.doDenieWorkOrder(
//                        courierId = Prefs.getString(COURIER_ID, ""),
//                        workOrderId = workOrderId
//                    )
                }
                finish()
            }
        }
        txtAccept.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {
                pressedAccept = true
                startAnimationProgress(progressBarAccept, true)
            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                pressedAccept = false
                clearAnimationProgress(progressBarAccept)
                valueAnimator.resume()
            }

            return@setOnTouchListener true
        }

        txtDismiss.setOnTouchListener { v, event ->

            if (event.action == MotionEvent.ACTION_DOWN) {

                pressedDismiss = true
                startAnimationProgress(progressBarDismiss, false)

            } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                pressedDismiss = false
                clearAnimationProgress(progressBarDismiss)
            }

            return@setOnTouchListener true
        }

        presenter.doGetWorkOrder(workOrderId)
    }

    override fun onAssignWorkOrder() {
        accept = true
        Prefs.putString(CURRENT_WORK_ORDER, workOrderId)
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onAssignWorkOrderFail() {
        if (!accept) {
            paused = false
            valueAnimator.resume()
            showToast("Falha ao aceitar a OS")
            stopLoad()
        }
    }

    override fun onDeniedWorkOrder() {
        denie = true
        showToast("OS recusada")
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onDeniedWorkOrderFail() {
        valueAnimator.resume()
        showToast("Falha ao recusar a OS")
        stopLoad()
    }

    override fun onGetWorkOrder(workOrder: WorkOrder) {
        this.workOrder = workOrder
        loadWorkOrder(workOrder)
    }

    private fun loadWorkOrder(workOrder: WorkOrder) {
        if (workOrder.status != WorkOrder.Status.PENDING.name) {
            mMediaPlayer.stop()
            if (workOrder.courierId != Prefs.getString(COURIER_ID, "")) {
                showToast("OS não está mais disponível")
                finish()
            } else {
                Prefs.putString(CURRENT_WORK_ORDER, workOrder.id!!)
                accept = true
//                showToast("OS aceita!")
//                startActivity(Intent(this, SplashActivity::class.java))
//                finish()
            }
        }

        if (!mMediaPlayer.isPlaying) {
            mMediaPlayer.start()
        }
        
        progressBar.max = 100

        if (paused) {
            paused = true
            valueAnimator.resume()
        } else {
            valueAnimator.start()
        }

        txtPrice.visibility = View.GONE
        if (workOrder.quotation?.price != null) {
            txtPrice.visibility = View.GONE // updated from VISIBLE to GONE date 21/2020
            txtPrice.text =
                NumberFormat.getCurrencyInstance().format(workOrder.quotation?.price ?: 0.0)
        }
        txtBonus.visibility = View.GONE
        if (workOrder.bonus != null) {
            txtBonus.text = NumberFormat.getCurrencyInstance().format(workOrder.bonus ?: 0.0)
        }
        val points = (workOrder.points!!)
        log("${Gson().toJson(points[0].address!!)} ${Gson().toJson(points[points.size - 1].address!!)}")
        txtAddress.text = String.format(
            "%s > %s",
            points[0].address!!.neighborhood,
            points[points.size - 1].address!!.neighborhood
        )
        if (points[0].address!!.neighborhood != null)
            txtAddress.visibility = View.GONE // updated from VISIBLE to GONE date 21/2020
        else
            txtAddress.visibility = View.GONE

        txtDuration.visibility = View.GONE
        if (workOrder.quotation?.duration != null) {
            txtDuration.visibility = View.VISIBLE
            var time = (workOrder.quotation?.duration!! / 60).toInt()
            if (time < 0)
                time = 1
            txtDuration.text = "${time}min"
        }

        txtDistance.visibility = View.GONE
        if (workOrder.quotation?.distance != null) {
            txtDistance.visibility = View.VISIBLE
            txtDistance.text =
                String.format("%.2fkm", (workOrder.quotation?.distance!! / 1000).toFloat())
        }

        txtDistanceToFirstPoint.text =
            String.format("Distância até o ponto: %.2fkm", distance.toFloat())
//        boundMap(workOrder)
    }

    override fun onGetWorkOrderFail() {
        showMessage("Falha ao recuperar informações desse pedido")
        finish()
    }

    private fun startLoadRefuse() {
        progressBarDismiss.progress = 15
        progressBarDismiss.startAnimation(
            RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 1000
                fillAfter = true
                repeatCount = Animation.INFINITE
                repeatMode = Animation.REVERSE
            })
        txtAccept.isEnabled = false
        txtDismiss.isEnabled = false
//        txtAccept.setBackgroundResource(R.drawable.circle_gray)
//        txtDismiss.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorGray)
    }

    private fun stopLoad() {
        txtAccept.isEnabled = true
        txtDismiss.isEnabled = true
        progressBarAccept.progress = 0
        progressBarAccept.startAnimation(RotateAnimation(0f, 0f))
        progressBarDismiss.progress = 0
        progressBarDismiss.startAnimation(RotateAnimation(0f, 0f))
//        txtAccept.setBackgroundResource(R.drawable.circle_gray)
//        txtDismiss.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorRed)

    }

    private fun clickAccept() {
        runOnUiThread {

            mMediaPlayer.stop()
            presenter.doAssignWorkOrder(
                courierId = FirebaseAuth.getInstance().uid!!,
                workOrderId = workOrderId
            )
            valueAnimator.pause()
            paused = true
            startLoadAccept()
        }
    }

    private fun startAnimationProgress(progressBar: ProgressBar, accept: Boolean) {
        progressBar.startAnimation(
            ProgressBarAnimation(
                progressBar,
                progressBar.progress.toFloat(),
                100f
            ).apply {
                duration = animationDuration
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        if (accept)
                            clickAccept()
                        else
                            clickDismiss()
                    }

                    override fun onAnimationStart(animation: Animation?) {

                    }

                })
            })
    }

    private fun clearAnimationProgress(progressBar: ProgressBar) {
        progressBar.startAnimation(
            ProgressBarAnimation(
                progressBar,
                progressBar.progress.toFloat(),
                0f
            ).apply {
                duration = progressBar.progress * animationDuration / 100.toLong()
                fillAfter = true
            })
//        presenter.doDenieWorkOrder(
//            courierId = FirebaseAuth.getInstance().currentUser!!.uid!!,
//            workOrderId = workOrderId
//        )
        if (paused) {
            paused = true
            valueAnimator.resume()
        }
//        paused = true
//        startLoadRefuse()
    }

    private fun clickDismiss() {
        runOnUiThread {
            if (mMediaPlayer.isPlaying) {
                mMediaPlayer.stop()
            }
            startLoadRefuse()
            presenter.doDenieWorkOrder(
                courierId = FirebaseAuth.getInstance().currentUser!!.uid!!,
                workOrderId = workOrderId
            )
        }
    }

    private fun startLoadAccept() {
        progressBarAccept.progress = 15
        progressBarAccept.startAnimation(
            RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 1000
                fillAfter = true
                repeatCount = Animation.INFINITE
            })
        txtAccept.isEnabled = false
        txtDismiss.isEnabled = false
//        txtAccept.setBackgroundResource(R.drawable.circle_gray_light)
//        fab_dismiss.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorGray)
    }

    private fun boundMap(workOrder: WorkOrder) {
        var builder = LatLngBounds.Builder()

        val iconFactory = IconGenerator(this)
        iconFactory.setColor(ContextCompat.getColor(this, R.color.colorWhite))
        iconFactory.setTextAppearance(R.style.BlueText)

        if (workOrder != null)
            for (point in (workOrder.points as List<WorkOrderPoint>)) {

                val marker = MarkerOptions()
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            iconFactory.makeIcon(
                                "${point.sequence!!}".toBold()
                            )
                        )
                    )
                    .anchor(iconFactory.anchorU, iconFactory.anchorV)
                    .position(
                        LatLng(
                            point.address!!.location!!.geopoint!!.latitude ?: 0.0,
                            point.address!!.location!!.geopoint!!.longitude ?: 0.0
                        )
                    )


                googleMap.addMarker(marker)
                builder.include(marker.position)
            }

        val bounds = builder.build()
        val padding = CommonsUtil.getPx(this, 30f).toInt() // offset from edges of the map in pixels
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        googleMap.animateCamera(cameraUpdate, 1, null)
    }


    override fun hideLoading() {

    }

    override fun showLoading() {

    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.stop()
        presenter.onDetach()
    }

    companion object {
        const val PARAM_WORKORDER_ID = "PARAM_WORKORDER_ID"
        const val PARAM_ACCEPTTIME = "PARAM_ACCEPTTIME"
        const val PARAM_DISTANCE = "PARAM_DISTANCE"

        fun start(
            context: Context,
            workOrderId: String,
            acceptTime: String,
            distance: String
        ): Intent {
            val intent = Intent(context, AlertActivity::class.java)
            intent.putExtra(PARAM_WORKORDER_ID, workOrderId)
            intent.putExtra(PARAM_ACCEPTTIME, acceptTime)
            intent.putExtra(PARAM_DISTANCE, distance)
            return intent
        }
    }
}
