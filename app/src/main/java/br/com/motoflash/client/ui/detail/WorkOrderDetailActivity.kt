package br.com.motoflash.client.ui.detail

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseActivity
import br.com.motoflash.core.data.network.model.Courier
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.adapter.WorkOrderPointsAdapter
import br.com.motoflash.core.ui.util.*
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.maps.android.ui.IconGenerator
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_work_order_detail.*
import java.util.*
import javax.inject.Inject

class WorkOrderDetailActivity : BaseActivity(), WorkOrderDetailMvpView {

    @Inject
    lateinit var presenter: WorkOrderDetailMvpPresenter<WorkOrderDetailMvpView>

    private val workOrderId by lazy {
        intent.getStringExtra(PARAM_WORKORDER_ID)
    }

    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    private var mRouteMarkerList: MutableList<Marker> = ArrayList()

    private var client: FusedLocationProviderClient? = null
    private var callback: LocationCallback? = null
    private var mLastLocation: Location? = null

    private val list:MutableList<WorkOrderPoint> = ArrayList()

    private var courier: Courier? = null

    private val adapter = WorkOrderPointsAdapter(
        callback = object : WorkOrderPointsAdapter.OnWorkOrderPointCallback{
            override fun onWorkOrderPointClick(workOrderPoint: WorkOrderPoint) {}
            override fun onWorkOrderPointLongClick(workOrderPoint: WorkOrderPoint) {}
        },
        list = list
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_order_detail)
        activityComponent.inject(this)

        // Inflate the layout for this fragment
        mMapView = findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume() // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            log("MapError: ${e.message}")
        }

        setUp()
    }

    override fun setUp() {
        fabCenter.hide()

        btnRunQueue.setOnClickListener {
            presenter.doRunQueue(workOrderId)
            btnRunQueue.visibility = View.GONE
            showLoading()
        }

        recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerview.adapter = adapter

        mMapView.getMapAsync {
            googleMap = it

            log("getMapAsync")

            googleMap.setOnMarkerClickListener {
                return@setOnMarkerClickListener false
            }

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_silver))

            googleMap.setOnMapLoadedCallback {
                log("setOnMapLoadedCallback")
//                loaderMaps?.visibility = View.GONE
                RxPermissions(this)
                    .request(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    .subscribe { granted ->
                        if(granted){
                            presenter.doGetWorkOrder(workOrderId!!)
                            loadLocation()
                        }else{
                            AlertUtil.showAlertNeutral(
                                context = this,
                                title = "Permissões",
                                message = "É necessário aceitar as permissões!"
                            )
                        }
                    }
            }
        }

        fabCenter.setOnClickListener {
            boundMap(true)
        }
    }

    private fun loadLocation() {
        val request = LocationRequest()

        request.interval = 1000
        request.fastestInterval = 1000 / 2
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        client = LocationServices.getFusedLocationProviderClient(this)

        if (callback != null) {
            client!!.removeLocationUpdates(callback)
        }

        callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                log("onResult")

                val location = locationResult!!.lastLocation
                if (location != null) {

                    if (mLastLocation == null)
                        mLastLocation = location

                    client!!.removeLocationUpdates(callback)
                    fabCenter.show()
                    boundMap(true)
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED && client != null) {
            // Request location updates and when an update is
            client!!.requestLocationUpdates(request, callback, null)
        }
    }

    private fun boundMap(move: Boolean){
        val iconFactory = IconGenerator(this)
        iconFactory.setColor(ContextCompat.getColor(this, R.color.colorWhite))
        iconFactory.setTextAppearance(R.style.PurpleText)

        clearMarkers()

        if(list.filter { it.address != null }.isEmpty()){
            val marker = MarkerOptions()
            marker.position(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            val cameraPosition = CameraPosition.Builder().target(marker.position).zoom(16f).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            return
        }else if(list.filter { it.address != null }.size >= 2){
            var builder = LatLngBounds.Builder()

            for (point in list) {

                when(point.status){
                    WorkOrderPoint.Status.PENDING.name -> {
                        iconFactory.setColor(ContextCompat.getColor(this, R.color.colorWhite))
                        iconFactory.setTextAppearance(R.style.PurpleText)
                    }
                    WorkOrderPoint.Status.STARTED.name -> {
                        iconFactory.setColor(ContextCompat.getColor(this, R.color.colorPurple))
                        iconFactory.setTextAppearance(R.style.WhiteText)
                    }
                    WorkOrderPoint.Status.CHECKED_OUT.name -> {
                        iconFactory.setColor(ContextCompat.getColor(this, R.color.colorGray))
                        iconFactory.setTextAppearance(R.style.WhiteText)
                    }
                }

                val marker = MarkerOptions()
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            iconFactory.makeIcon(
                                "${point.sequence!!}".toBold()
                            )
                        )
                    )
                    .anchor(iconFactory.anchorU, iconFactory.anchorV)
                    .position(LatLng(point.address?.location?.geopoint?.latitude ?: 0.0, point.address?.location?.geopoint?.longitude ?: 0.0))

                mRouteMarkerList.add(googleMap.addMarker(marker))
                builder.include(marker.position)
            }

            if(courier!=null){
                val marker = MarkerOptions()
                marker.position(
                    LatLng(
                        courier!!.location!!.geopoint!!.latitude,
                        courier!!.location!!.geopoint!!.longitude
                    )
                )
                marker.title(courier!!.name)
                marker.icon(
                    bitmapDescriptorFromVector(
                        context = this,
                        vectorDrawableResourceId = R.drawable.helmet_2_24dp
                    )
                )
                mRouteMarkerList.add(googleMap.addMarker(marker))
                builder.include(marker.position)
            }

            if(move){
                val bounds = builder.build()
                val padding = CommonsUtil.getPx(this, 100f) // offset from edges of the map in pixels
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding.toInt())
                googleMap.animateCamera(cameraUpdate, 2000, null)
            }
        }
    }

    private fun clearMarkers() {
        for (marker in mRouteMarkerList) {
            marker.remove()
        }
        mRouteMarkerList.clear()
    }

    override fun onStart() {
        super.onStart()
        presenter.onAttach(this)
//        presenter.doGetWorkOrder(workOrderId)
    }

    override fun onStop() {
        super.onStop()
        presenter.onDetach()
    }

    override fun onGetWorkOrder(workOrder: WorkOrder) {
        courier = workOrder.courier

        if(list.isEmpty()){
            list.clear()
            list.addAll(workOrder.points!!)
            boundMap(true)
        }else{
            boundMap(false)
        }

        list.clear()
        list.addAll(workOrder.points!!)

        adapter.notifyDataSetChanged()

        txtPrice.text = String.format("Total: R$ %.2f", workOrder.quotation?.price?.toFloat())

        if(workOrder.status == WorkOrder.Status.PENDING.name){
            btnRunQueue.visibility = View.VISIBLE
        }else{
            btnRunQueue.visibility = View.GONE
        }

        val status = WorkOrder.Status.valueOf(workOrder.status!!)

        txtStatus.backgroundTintList = ContextCompat.getColorStateList(this, status.getColor(this))
        txtStatus.text = status.toLabel()

        if(workOrder.courier!=null){
            containerCourier.visibility = View.VISIBLE
            Glide.with(this).load(workOrder.courier!!.profilePhoto).into(imgProfile)
            txtName.text = workOrder.courier!!.name
            txtMobilePhone.text = workOrder.courier!!.mobilePhone
        }else{
            containerCourier.visibility = View.GONE
        }
    }

    override fun onNotFoundCourier() {
        "Não foi encontrado um entregador para seu pedido!".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
        btnRunQueue.visibility = View.VISIBLE
    }

    override fun onRunQueueFail() {
        hideLoading()
        btnRunQueue.visibility = View.VISIBLE
    }

    override fun onRunQueue() {
        hideLoading()
    }


    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun bitmapDescriptorFromVector( context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, R.drawable.pin_white)!!
        background.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)!!
        vectorDrawable.setBounds(CommonsUtil.getPx(context, 12f).toInt(), CommonsUtil.getPx(context, 6f).toInt(), vectorDrawable.intrinsicWidth + CommonsUtil.getPx(context, 10f).toInt(), vectorDrawable.intrinsicHeight + CommonsUtil.getPx(context,  6f).toInt())
//    vectorDrawable.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
        val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas =  Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object{
        const val PARAM_WORKORDER_ID = "PARAM_WORKORDER_ID"
        fun start(context: Context, workOrderId: String) : Intent{
            val intent = Intent(context, WorkOrderDetailActivity::class.java)
            intent.putExtra(PARAM_WORKORDER_ID, workOrderId)
            return intent
        }
    }
}
