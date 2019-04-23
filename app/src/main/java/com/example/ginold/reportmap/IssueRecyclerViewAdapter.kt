package com.example.ginold.reportmap

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.util.ArrayList
import com.example.ginold.reportmap.models.Issue


class IssueRecyclerViewAdapter (private var mDataset: ArrayList<Issue>?, private val onClickListener: IssueItemOnClickListener?)
    : RecyclerView.Adapter<IssueRecyclerViewAdapter.CustomViewHolder>() {

    // Provide a reference to the views for each data item
    class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        var issueTitle: TextView = v.findViewById(R.id.issue_name_list) as TextView
        var image: ImageView = v.findViewById(R.id.issue_image_list) as ImageView
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueRecyclerViewAdapter.CustomViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.issue_list_item, null, false)
        return CustomViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        // - get element from your dataset at this position
        val issue = mDataset!![position]
        val ctx = holder.itemView.context

        holder.issueTitle.text = issue.name
        holder.image.setImageResource(ctx.resources.getIdentifier(issue.type, "drawable", ctx.packageName))
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onIssueItemClick(holder.adapterPosition, issue)
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset!!.size
    }
}