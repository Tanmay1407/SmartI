package com.herokuapp.tanmaypatel.smarti

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SearchRVAdapter(var context : Context,var list : ArrayList<SearchRVModel>) : RecyclerView.Adapter<SearchRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRVAdapter.ViewHolder {
       var view = LayoutInflater.from(context).inflate(R.layout.search_result_rv_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchRVAdapter.ViewHolder, position: Int) {
        val searchRVModel : SearchRVModel = list[position]
        holder.title.text = searchRVModel.title
        holder.snippet.text = searchRVModel.link
        holder.des.text = searchRVModel.snippet

        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var intent = Intent(Intent.ACTION_VIEW)
                intent.data = (Uri.parse(searchRVModel.link))
                context.startActivity(intent)
            }
        })

    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var title = itemView.findViewById<TextView>(R.id.tvTitle)
        var snippet = itemView.findViewById<TextView>(R.id.tvSnippet)
        var des = itemView.findViewById<TextView>(R.id.tvDescription)


    }

    override fun getItemCount(): Int {
        return list.size
    }
}