package com.example.wifianalyzer.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wifianalyser.R
import com.example.wifianalyser.roomdb.AccessPointsModel
import com.example.wifianalyser.databinding.WifilistitemBinding

class WifiListAdapter :
    ListAdapter<AccessPointsModel, WifiListAdapter.ViewHolder>(WifiInfoDiffCallback) {

    inner class ViewHolder(private var binding: WifilistitemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: AccessPointsModel) {
            binding.apply {
                if (data.accessPointName.isBlank())
                    tvNameWifi.text = itemView.context.getString(R.string.hidden)
                else tvNameWifi.text = data.accessPointName
                tvChannel.text = itemView.context.getString(R.string.ch) + data.channel.toString()
                tvDbm.text = data.dbm.toString() + " " + itemView.context.getString(R.string.dbm)
                tvMac.text = "MAC:" + data.MacAddress
                tvWifiFrequency.text =
                    "Freq:" + data.frequency.toString() + itemView.context.getString(R.string.mhz)
                tvSecurity.text = data.security
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = WifilistitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }
}

object WifiInfoDiffCallback : DiffUtil.ItemCallback<AccessPointsModel>() {
    override fun areItemsTheSame(oldItem: AccessPointsModel, newItem: AccessPointsModel): Boolean {
        return oldItem.MacAddress == newItem.MacAddress
    }

    override fun areContentsTheSame(
        oldItem: AccessPointsModel,
        newItem: AccessPointsModel
    ): Boolean {
        return oldItem.accessPointName == newItem.accessPointName
                && oldItem.dbm == newItem.dbm
                && oldItem.channel == newItem.channel
    }
}