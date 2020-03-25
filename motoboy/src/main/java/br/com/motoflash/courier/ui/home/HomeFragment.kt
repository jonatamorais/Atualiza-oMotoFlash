package br.com.motoflash.courier.ui.home


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.adapter.WorkOrderAdapter
import br.com.motoflash.core.ui.util.*
import br.com.motoflash.courier.R
import br.com.motoflash.courier.ui.base.BaseFragment
import br.com.motoflash.courier.ui.splash.SplashActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.ui.IconGenerator
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_home.*
import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : BaseFragment(), HomeMvpView {

    @Inject
    lateinit var presenter: HomeMvpPresenter<HomeMvpView>

    private val list: MutableList<WorkOrder> = ArrayList()
    private var mRouteMarkerList: MutableList<Marker> = java.util.ArrayList()

    private val adapter = WorkOrderAdapter(
        list = list,
        callback = object : WorkOrderAdapter.OnWorkOrderCallback {
            override fun onWorkOrderClick(workOrder: WorkOrder) {

            }

            override fun onWorkOrderLongClick(workOrder: WorkOrder) {

            }
        },
        motoboy = true
    )

    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap

    private var load = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentComponent.inject(this)

        mMapView = view.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(context!!)
        } catch (e: Exception) {
            e.printStackTrace()
            log("MapError: ${e.message}")
        }
        setUp()
    }

    override fun setUp() {
        recyclerView.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        mMapView.isFocusable = false
        mMapView.getMapAsync {
            googleMap = it

            val padding = CommonsUtil.getPx(context!!, 20f).toInt()
            val positions = intArrayOf(0, 0)
            topCardView.getLocationOnScreen(positions)
            googleMap.setPadding(
                padding,
                CommonsUtil.getPx(context!!, 150f).toInt(),
                padding,
                padding
            )

            log("getMapAsync")

            googleMap.setOnMarkerClickListener {
                return@setOnMarkerClickListener false
            }

            googleMap.setOnMapLoadedCallback {
                log("setOnMapLoadedCallback")
//                loaderMaps?.visibility = View.GONE
                load = true
                Timer("delay").schedule(1000L) {
                    activity!!.runOnUiThread {
                        if (list.isNotEmpty())
                            boundMap(true, list[0])
                    }
                }
            }

            googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context!!,
                    R.raw.mapstyle_silver
                )
            )
        }
    }

    override fun onGetCurrentWorkOrder(workOrder: WorkOrder) {
        list.clear()
        list.add(workOrder)
        adapter.notifyDataSetChanged()

        txtEmpty.visibility = View.GONE
        containerWorkOrder.visibility = View.VISIBLE

        btnAction.visibility = View.VISIBLE
        if (workOrder.points!!.filter { it.status != WorkOrderPoint.Status.PENDING.name }.isEmpty() && workOrder.status == WorkOrder.Status.ASSIGNED.name) {
            btnAction.text = "Inicie o pedido!"
            btnAction.setOnClickListener {
                showLoading()
                presenter.doStartWorkOrder(workOrder.courierId!!, workOrder.id!!, workOrder)
            }
        } else {
            if (workOrder.points!!.filter { it.status == WorkOrderPoint.Status.STARTED.name }
                    .isEmpty()) {
                btnAction.text = "Inicie o ponto!"
                btnAction.setOnClickListener {
                    showLoading()
                    presenter.doStartPoint(
                        workOrder.courierId!!,
                        workOrder.id!!,
                        workOrder.points!!.first { it.status == WorkOrderPoint.Status.PENDING.name }.id!!,
                        workOrder
                    )
                }
            } else {
                btnAction.text = "Finalize o ponto!"
                btnAction.setOnClickListener {
                    showLoading()
                    presenter.doFinishPoint(
                        workOrder.courierId!!,
                        workOrder.id!!,
                        workOrder.points!!.first { it.status == WorkOrderPoint.Status.STARTED.name }.id!!,
                        workOrder
                    )
                }
            }
        }

        btnCancell.visibility = View.GONE
        btnCancell.setOnClickListener {
            if (StringUtils.isNotEmpty(workOrder.id!!)) {
                showLoading()
            }
        }

        if (load && context != null)
            boundMap(false, workOrder)
    }

    override fun onCancellWorkOrder() {
        hideLoading()
        "Pedido Cancelado!".showSnack(container, backgroundColor = R.color.colorBlue)
    }

    override fun onCancellWorkOrderFail() {
        hideLoading()
        "Falha ao cancelar pedido".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onStartWorkOrder() {
        hideLoading()
        "Pedido Iniciado!".showSnack(container, backgroundColor = R.color.colorBlue)
    }

    override fun onStartWorkOrderFail() {
        hideLoading()
        "Falha ao iniciar pedido".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onStartPoint() {
        hideLoading()
        "Ponto Iniciado!".showSnack(container, backgroundColor = R.color.colorBlue)
    }

    override fun onStartPointFail() {
        hideLoading()
        "Falha ao iniciar ponto".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onFinishPoint(lastPoint: Boolean) {
        hideLoading()
        if (!lastPoint)
            "Ponto Finalizado!".showSnack(container, backgroundColor = R.color.colorBlue)
        else {
            "Pedido Finalizado!".showSnack(container, backgroundColor = R.color.colorBlue)
            containerWorkOrder.visibility = View.GONE
            txtEmpty.visibility = View.VISIBLE
        }
    }

    override fun onFinishPointFail() {
        hideLoading()
        "Falha ao finalizar ponto".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onGetEmptyWorkOrder() {
        Prefs.putString(CURRENT_WORK_ORDER, "")
        presenter.doSetRunningFalse(courierId = Prefs.getString(COURIER_ID, ""))
        txtEmpty.visibility = View.VISIBLE
        containerWorkOrder.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        presenter.onAttach(this)
        presenter.doGetCurrentWorkOrder(Prefs.getString(COURIER_ID, ""))
    }

    override fun onStop() {
        super.onStop()
        presenter.onDetach()
    }

    private fun boundMap(move: Boolean, workOrder: WorkOrder) {
        val iconFactory = IconGenerator(context!!)
        iconFactory.setColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        iconFactory.setTextAppearance(R.style.BlueText)

        clearMarkers()

        if (workOrder.points!!.filter { it.address != null }.size >= 2) {
            var builder = LatLngBounds.Builder()

            for (point in workOrder.points!!) {

                when (point.status) {
                    WorkOrderPoint.Status.PENDING.name -> {
                        iconFactory.setColor(ContextCompat.getColor(context!!, R.color.colorWhite))
                        iconFactory.setTextAppearance(R.style.BlueText)
                    }
                    WorkOrderPoint.Status.STARTED.name -> {
                        iconFactory.setColor(ContextCompat.getColor(context!!, R.color.colorBlue))
                        iconFactory.setTextAppearance(R.style.WhiteText)
                    }
                    WorkOrderPoint.Status.CHECKED_OUT.name -> {
                        iconFactory.setColor(ContextCompat.getColor(context!!, R.color.colorGray))
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
                    .position(
                        LatLng(
                            point.address?.location?.geopoint?.latitude ?: 0.0,
                            point.address?.location?.geopoint?.longitude ?: 0.0
                        )
                    )

                mRouteMarkerList.add(googleMap.addMarker(marker))
                builder.include(marker.position)
            }


            val marker = MarkerOptions()
            marker.position(
                LatLng(
                    workOrder.courier!!.location!!.geopoint!!.latitude,
                    workOrder.courier!!.location!!.geopoint!!.longitude
                )
            )
            marker.title("Você")
            marker.icon(
                bitmapDescriptorFromVector(
                    context = context!!,
                    vectorDrawableResourceId = R.drawable.helmet_2_24dp
                )
            )
            mRouteMarkerList.add(googleMap.addMarker(marker))
            builder.include(marker.position)


            if (move) {
                val bounds = builder.build()
                val padding =
                    CommonsUtil.getPx(context!!, 100f) // offset from edges of the map in pixels
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

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, R.drawable.pin_white)!!
        background.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)!!
        vectorDrawable.setBounds(
            CommonsUtil.getPx(context, 12f).toInt(),
            CommonsUtil.getPx(context, 6f).toInt(),
            vectorDrawable.intrinsicWidth + CommonsUtil.getPx(context, 10f).toInt(),
            vectorDrawable.intrinsicHeight + CommonsUtil.getPx(context, 6f).toInt()
        )
//    vectorDrawable.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
        val bitmap = Bitmap.createBitmap(
            background.intrinsicWidth,
            background.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun hideLoading() {
        progress.visibility = View.GONE
        btnAction.visibility = View.VISIBLE
    }

    private fun showLoading() {
        progress.visibility = View.VISIBLE
        btnAction.visibility = View.GONE
    }
}
