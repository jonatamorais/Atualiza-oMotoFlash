package br.com.motoflash.client.ui.history


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import br.com.motoflash.client.R
import br.com.motoflash.client.ui.base.BaseFragment
import br.com.motoflash.client.ui.detail.WorkOrderDetailActivity
import br.com.motoflash.client.ui.main.MainActivity
import br.com.motoflash.core.data.network.model.WorkOrder
import br.com.motoflash.core.ui.adapter.WorkOrderAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_history.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class HistoryFragment : BaseFragment(), HistoryMvpView {

    private val list: MutableList<WorkOrder> = ArrayList()

    private var containerFrame: View? = null

    private val adapter = WorkOrderAdapter(
        list = list,
        callback = object: WorkOrderAdapter.OnWorkOrderCallback{
            override fun onWorkOrderClick(workOrder: WorkOrder) {
                startActivity(WorkOrderDetailActivity.start(context!!,workOrderId = workOrder.id!!))
            }

            override fun onWorkOrderLongClick(workOrder: WorkOrder) {

            }
        }
    )

    @Inject
    lateinit var presenter: HistoryMvpPresenter<HistoryMvpView>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        containerFrame = view.findViewById<View>(R.id.container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentComponent.inject(this)

        setUp()
    }

    override fun setUp() {
        recyclerView.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        presenter.onAttach(this)
        presenter.doGetWorkOrders(FirebaseAuth.getInstance().uid!!)
    }

    override fun onGetWorkOrders(workOrders: List<WorkOrder>) {
        progressBar.visibility = View.GONE
        list.clear()
        list.addAll(workOrders)

        if(list.isEmpty()){
            txtEmpty.visibility = View.VISIBLE
        }else{
            txtEmpty.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        presenter.onDetach()
    }
}
