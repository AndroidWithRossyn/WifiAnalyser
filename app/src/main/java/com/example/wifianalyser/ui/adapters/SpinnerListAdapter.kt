package com.example.wifianalyser.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wifianalyser.models.SpinnerModel
import com.example.wifianalyser.R
import com.example.wifianalyser.databinding.ItemSpinnerListBinding

class SpinnerListAdapter(private var onClick: (data: SpinnerModel, position: Int) -> Unit) :
    ListAdapter<SpinnerModel, SpinnerListAdapter.ViewHolder>(WifiInfoDiffCallback) {

    inner class ViewHolder(private var binding: ItemSpinnerListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SpinnerModel) {
            binding.apply {
                if (data.accessPointName.isBlank())
                    tvAccessPointName.text = itemView.context.getString(R.string.hidden)
                else tvAccessPointName.text = data.accessPointName

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            ItemSpinnerListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        val mPos = holder.adapterPosition

        holder.itemView.setOnClickListener {
      onClick(this.currentList[mPos],mPos)
        }
        holder.bind(data)
    }
}

object WifiInfoDiffCallback : DiffUtil.ItemCallback<SpinnerModel>() {
    override fun areItemsTheSame(oldItem: SpinnerModel, newItem: SpinnerModel): Boolean {
        return oldItem.accessPointSpeed == newItem.accessPointSpeed
    }

    override fun areContentsTheSame(
        oldItem: SpinnerModel,
        newItem: SpinnerModel
    ): Boolean {
        return oldItem.accessPointName == newItem.accessPointName
                && oldItem.accessPointSpeed == newItem.accessPointSpeed
    }
}