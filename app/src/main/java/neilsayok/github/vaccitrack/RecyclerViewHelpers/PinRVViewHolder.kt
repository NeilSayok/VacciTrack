package neilsayok.github.vaccitrack.RecyclerViewHelpers

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import neilsayok.github.vaccitrack.R

public class PinRVViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val pinText : TextView = itemView.findViewById(R.id.pinText)
    val delPinBtn : ImageButton = itemView.findViewById(R.id.deletePinIV)
}