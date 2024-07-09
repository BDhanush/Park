package com.example.park.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.park.ParkingActivity
import com.example.park.R
import com.example.park.model.Parking
import com.google.android.material.card.MaterialCardView

class ParkingItemAdapter(private var dataSet: List<Parking>) :
    RecyclerView.Adapter<ParkingItemAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.title)
        val parkingCard:MaterialCardView = view.findViewById(R.id.parkingCard)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.parking_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.titleTextView.text = dataSet[position].title
        viewHolder.parkingCard.setOnClickListener {
            val intent = Intent(viewHolder.itemView.context, ParkingActivity::class.java)
            intent.putExtra("title", dataSet[position].title)
            intent.putExtra("latitude", dataSet[position].latitude)
            intent.putExtra("longitude", dataSet[position].longitude)
            intent.putExtra("altitude", dataSet[position].altitude)
            intent.putExtra("note", dataSet[position].note)
            intent.putExtra("id", dataSet[position].id)
            viewHolder.itemView.context.startActivity(intent)
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun updateDataset(dataSet:List<Parking>)
    {
        this.dataSet=dataSet
        notifyDataSetChanged()
    }

}
