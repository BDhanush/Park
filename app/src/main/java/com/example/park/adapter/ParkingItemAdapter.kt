package com.example.park.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.park.MainActivity
import com.example.park.ParkingActivity
import com.example.park.R
import com.example.park.model.Parking
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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

        viewHolder.parkingCard.setOnLongClickListener {
            val alertDialog = MaterialAlertDialogBuilder(viewHolder.itemView.context)
                .setTitle("Delete Parking: ${dataSet[position].title}")
                .setMessage("Parking Space will be deleted")
                .setPositiveButton("Delete") { dialog, which ->
                    deleteParking(dataSet[position],viewHolder.itemView.context)
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
            alertDialog.show()
            return@setOnLongClickListener true
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

fun deleteParking(parking: Parking,context: Context)
{
    MainActivity.database.parkingDao().delete(parking)
    Toast.makeText(context,"Parking Space Deleted", Toast.LENGTH_LONG).show()
}