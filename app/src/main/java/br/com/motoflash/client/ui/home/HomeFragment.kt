package br.com.motoflash.client.ui.home


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Parcel
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseFragment
import br.com.motoflash.client.ui.main.MainActivity
import br.com.motoflash.core.data.network.model.Address
import br.com.motoflash.core.data.network.model.Address.Companion.CITY
import br.com.motoflash.core.data.network.model.Address.Companion.NEIGHBORHOOD
import br.com.motoflash.core.data.network.model.Address.Companion.NUMBER
import br.com.motoflash.core.data.network.model.Address.Companion.POSTAL_CODE
import br.com.motoflash.core.data.network.model.Address.Companion.STATE
import br.com.motoflash.core.data.network.model.Address.Companion.STREET
import br.com.motoflash.core.data.network.model.Quotation
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.data.network.model.WorkOrderPoint
import br.com.motoflash.core.ui.adapter.WorkOrderPointsAdapter
import br.com.motoflash.core.ui.dialog.types.InputTextDialog
import br.com.motoflash.core.ui.util.AlertUtil
import br.com.motoflash.core.ui.util.CommonsUtil
import br.com.motoflash.core.ui.util.showSnack
import br.com.motoflash.core.ui.util.toBold
import com.fonfon.kgeohash.GeoHash
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.api.Quota
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.ui.IconGenerator
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : BaseFragment(), HomeMvpView {

    lateinit var mMapView: MapView
    private lateinit var googleMap: GoogleMap
    private var mRouteMarkerList: MutableList<Marker> = ArrayList()

    private var client: FusedLocationProviderClient? = null
    private var callback: LocationCallback? = null
    private var mLastLocation: Location? = null

    private var currentQuotation: Quotation? = null

    private var workOrderId: String? = null

    val placesClient by lazy{
        Places.createClient(context!!)
    }

    private var currentWorkOrderPointId: String? = null

    private val list = mutableListOf(WorkOrderPoint(
        id = UUID.randomUUID().toString(),
        sequence = 1L
    ),
        WorkOrderPoint(
            id = UUID.randomUUID().toString(),
            sequence = 2L
        ))

    private val adapter = WorkOrderPointsAdapter(
        callback = object : WorkOrderPointsAdapter.OnWorkOrderPointCallback{
            override fun onWorkOrderPointClick(workOrderPoint: WorkOrderPoint) {
                val fields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS_COMPONENTS
                )

                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields
                )
                    .build(context!!)

                currentWorkOrderPointId = workOrderPoint.id

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
            }

            override fun onWorkOrderPointLongClick(workOrderPoint: WorkOrderPoint) {
                verifyRemovePoint(workOrderPoint)
            }
        },
        list = list
    )

    @Inject
    lateinit var presenter: HomeMvpPresenter<HomeMvpView>

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null){
            log("autocomplete")

            var hasNumber = false

            for(item in Autocomplete.getPlaceFromIntent(data).addressComponents?.asList()!!){
                log(String.format("name:%s shortName:%s type:%s", item.name, item.shortName, item.types[0]))
                if(item.types[0] == NUMBER) {
                    hasNumber = true
                }
            }

            log("before !hasNumber")

            if(!hasNumber && false){
                log("!hasNumber")
                val dialog = InputTextDialog(
                    title = "Endereço",
                    label = "Informe um número para esse endereço: ",
                    callback = object : InputTextDialog.DialogListener{
                        override fun onButtonClick(text: String, dialogFragment: DialogFragment) {
                            if(text.isEmpty()){
                                "É necessário informar o número!".showSnack(container, backgroundColor = R.color.colorRed)
                            }else {
                                dialogFragment.dismiss()
                                val place = Autocomplete.getPlaceFromIntent(data)
                                place.addressComponents?.asList()?.add(object : AddressComponent(){
                                    override fun describeContents(): Int {
                                        return 0
                                    }

                                    override fun getName(): String {
                                        return text
                                    }

                                    override fun getShortName(): String? {
                                        return text
                                    }

                                    override fun getTypes(): MutableList<String> {
                                        return listOf(NUMBER).toMutableList()
                                    }

                                    override fun writeToParcel(dest: Parcel?, flags: Int) {

                                    }
                                })
                                updateWorkOrderPoint(place)
                            }
                        }
                    },
                    dark = false)

                dialog.show(fragmentManager!!, "Endereço")
            }else{
                log("hasNumber")
                updateWorkOrderPoint(Autocomplete.getPlaceFromIntent(data))
            }
        }
    }

    private fun updateWorkOrderPoint(place:Place){
        log("updateWorkOrderPoint")
        val address = Address().apply {
            //                                    name = text
            for (item in place.addressComponents?.asList()!!) {
                when (item.types[0]) {
                    STREET -> {
                        address1 = item.name
                    }
                    NUMBER -> {
                        number = item.name
                    }
                    NEIGHBORHOOD -> {
                        neighborhood = item.name
                    }
                    CITY -> {
                        city = item.name
                    }
                    STATE -> {
                        state = item.shortName!!
                    }
                    POSTAL_CODE -> {
                        zipCode = item.name
                    }
                }
            }
        }

        address.address2 = "Casa"
        address.location = br.com.motoflash.core.data.network.model.Location(
            geopoint = GeoPoint(
                place.latLng!!.latitude,
                place.latLng!!.longitude
            ),
            geohash = GeoHash(place.latLng!!.latitude,
                place.latLng!!.longitude).toString()
        )

        list.forEach {
            log("item before:${it.address?.address1?:"address"}, ${it.id}, ${it.sequence}")
        }

        if(currentWorkOrderPointId!=null){
            val index = list.indexOfFirst { it.id == currentWorkOrderPointId }
            list[index] = WorkOrderPoint(
                address = address,
                sequence = index + 1L,
                id = currentWorkOrderPointId
            )
            currentWorkOrderPointId = null
        }else{
            list.add(
                WorkOrderPoint(
                    address = address,
                    id = UUID.randomUUID().toString(),
                    sequence = (list[list.size - 1].sequence?:0) + 1
            ))
        }
        list.forEach {
            log("item:${it.address?.address1?:"address"}, ${it.id}, ${it.sequence}")
        }

        adapter.notifyDataSetChanged()
        currentQuotation = null
        txtPrice.visibility = View.GONE
        btnCreate.visibility = View.GONE
        if(list.filter { it.address!=null }.size >= 2){
            presenter.doGetQuotation(list, (activity!! as MainActivity).getCurrentUser()?.companyId?:"")
        }else{
            txtPrice.visibility = View.GONE
        }
        boundMap()
    }

    private fun verifyRemovePoint(workOrderPoint: WorkOrderPoint){
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle("Remover")
        dialog.setMessage("Deseja remover esse ponto?")
        dialog.setPositiveButton("Remover") { _, _ ->
            list.removeAt(list.indexOfFirst { it.id == workOrderPoint.id})
            adapter.notifyDataSetChanged()
            currentQuotation = null
            txtPrice.visibility = View.GONE
            btnCreate.visibility = View.GONE

            if(list.filter { it.address!=null }.size >= 2){
                presenter.doGetQuotation(list, (activity!! as MainActivity).getCurrentUser()?.companyId?:"")
            }else{
                txtPrice.visibility = View.GONE
            }
            boundMap()
        }
        dialog.setNegativeButton("Cancelar",null)
        dialog.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Inflate the layout for this fragment
        mMapView = view.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume() // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
            log("MapError: ${e.message}")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentComponent.inject(this)
        presenter.onAttach(this)
        setUp()
    }

    override fun setUp() {
        mMapView.isFocusable = false


        mMapView.getMapAsync {
            googleMap = it

            log("getMapAsync")

            googleMap.setOnMarkerClickListener {
                return@setOnMarkerClickListener false
            }

            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_silver))

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
                            loadLocation()
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

        fabCenter.setOnClickListener {
            boundMap()
        }

        btnNewPoint.setOnClickListener {
            val fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            )
                .build(context!!)

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }

        recyclerview.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        recyclerview.adapter = adapter
        adapter.notifyDataSetChanged()

        btnCreate.setOnClickListener {
            if(currentQuotation!=null){
                showLoading()
                presenter.doCreateWorkOrder(
                    userId = (activity!! as MainActivity).getCurrentUser()?.id?:"",
                    points = list,
                    quotation = currentQuotation!!,
                    motorcycle = btnMotorcycle.isChecked
                )
            }else{
                "Você precisa ter feito um orçamento com pelo menos 2 pontos".showSnack(container, backgroundColor = R.color.colorRed)
            }
        }

        btnMotorcycle.setOnClickListener {
            if(btnMotorcycle.isChecked){
                btnMotorcycle.text = "Motoboy"
                btnMotorcycle.setTextColor(ContextCompat.getColor(context!!, R.color.colorPurple))
            }else{
                btnMotorcycle.text = "Bike"
                btnMotorcycle.setTextColor(ContextCompat.getColor(context!!, R.color.colorGray))
            }

        }

        btnCancell.setOnClickListener {
            if(workOrderId!=null){
                presenter.doCancellWorkOrder(workOrderId!!)
            }
        }
    }

    private fun loadLocation() {
        val request = LocationRequest()

        request.interval = 1000
        request.fastestInterval = 1000 / 2
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        client = LocationServices.getFusedLocationProviderClient(activity!!)

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

                    boundMap()
                }
            }
        }

        val permission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED && client != null) {
            // Request location updates and when an update is
            client!!.requestLocationUpdates(request, callback, null)
        }
    }

    private fun boundMap(){
        val iconFactory = IconGenerator(context!!)
        iconFactory.setColor(ContextCompat.getColor(context!!, R.color.colorWhite))
        iconFactory.setTextAppearance(R.style.PurpleText)

        clearMarkers()

        if(list.filter { it.address != null }.isEmpty() && mLastLocation!=null){
            val marker = MarkerOptions()
            marker.position(LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude))
            val cameraPosition = CameraPosition.Builder().target(marker.position).zoom(16f).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            return
        }else if(list.filter { it.address != null }.size >= 2){
            var builder = LatLngBounds.Builder()

            for (point in list) {

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

            val bounds = builder.build()
            val padding = CommonsUtil.getPx(context!!, 100f) // offset from edges of the map in pixels
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding.toInt())
                googleMap.animateCamera(cameraUpdate, 2000, null)

        }
    }

    fun clearMarkers() {
        for (marker in mRouteMarkerList) {
            marker.remove()
        }
        mRouteMarkerList.clear()
    }


    override fun onGetQuotation(quotation: Quotation) {
        currentQuotation = quotation
        txtPrice.text = String.format("Total: R$ %.2f", quotation.price?.toFloat())
        txtPrice.visibility = View.VISIBLE
        btnCreate.visibility = View.VISIBLE
        hideLoading()
    }

    override fun onGetQuotationFail() {
        "Falha ao calcular preço da entrega".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    override fun onCreateWorkOrder() {
        hideLoading()
        "Seu pedido foi criado".showSnack(container, backgroundColor = R.color.colorPurple)
            (activity!! as MainActivity).doOpenFragment(1)
    }

    override fun onSearchCourier(workOrderId: String) {
        this.workOrderId = workOrderId
        "Procurando um entregador...".showSnack(container, backgroundColor = R.color.colorPurple)
        btnCancell.visibility = View.VISIBLE
    }

    override fun onCancellWorkOrder() {
        (activity!! as MainActivity).doOpenFragment(1)
        "Seu pedido foi cancelado".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onCancellWorkOrderFail() {
        "Não foi possível cancelar o pedido".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onNotFoundCourier() {
        hideLoading()
        (activity!! as MainActivity).doOpenFragment(1)
        "Não foi possível encontrar um entregador para seu pedido. Pode tentar novamente selecionando ele".showSnack(container, backgroundColor = R.color.colorRed)
    }

    override fun onCreateWorkOrderFail() {
        "Falha ao criar uma entrega".showSnack(container, backgroundColor = R.color.colorRed)
        hideLoading()
    }

    fun hideLoading(){
        btnNewPoint.visibility = View.VISIBLE
        btnCreate.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    fun showLoading(){
        btnNewPoint.visibility = View.GONE
        btnCreate.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDetach()
    }

    companion object{
        const val AUTOCOMPLETE_REQUEST_CODE = 426
    }
}
