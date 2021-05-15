package neilsayok.github.vaccitrack.RecyclerViewHelpers

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import neilsayok.github.vaccitrack.R
import neilsayok.github.vaccitracker.ViewModels.PinRVViewModel

class PinRVAdapter(val model:PinRVViewModel, var pinList: ArrayList<String>,val context: Context): RecyclerView.Adapter<PinRVViewHolder>() {

    lateinit var parentVG: ViewGroup
    lateinit var sp:SharedPreferences

    init {
        sp = context.getSharedPreferences("appDataPref", AppCompatActivity.MODE_PRIVATE)

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinRVViewHolder {
        parentVG = parent
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_rv_pincode,parent,false)
        return PinRVViewHolder(v)
    }

    override fun onBindViewHolder(holder: PinRVViewHolder, position: Int) {
        holder.pinText.text = pinList[position]
        holder.delPinBtn.setOnClickListener{
            //Log.d("Position",position.toString())
            model.removePin(pinList[position])
            notifyDataSetChanged()
        }

    }

    public fun updateList(updateList:ArrayList<String>){
        pinList = updateList
        //sp.edit().putStringSet("PIN_LIST", HashSet<String>(pinList)).apply()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return pinList.size
    }



}