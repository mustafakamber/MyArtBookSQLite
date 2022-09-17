package com.mustafakamber.myartbooksql.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.mustafakamber.myartbooksql.databinding.RecyclerRowBinding
import com.mustafakamber.myartbooksql.model.Art
import com.mustafakamber.myartbooksql.view.ArtActivity

class ArtAdapter(val artList : ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {
    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        //recycler_row'u ArtAdapter'a baglama islemleri
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        //Art ismini recyclerView listesinde gosterme
        holder.binding.recyclerViewTextView.text = artList.get(position).name
        //Art ismine basilinca yapilacaklar
        holder.itemView.setOnClickListener{
            val intentToArtAct = Intent(holder.itemView.context, ArtActivity::class.java)
            //Kullanici Art'in ismine tikladiginda kayitli bir veriyi gorecek
            intentToArtAct.putExtra("info","old")
            intentToArtAct.putExtra("id",artList.get(position).id)
            holder.itemView.context.startActivity(intentToArtAct)
        }
    }

    override fun getItemCount(): Int {
     return artList.size
    }
}