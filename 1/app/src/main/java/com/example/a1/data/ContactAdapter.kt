package com.example.a1.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.a1.R
import androidx.recyclerview.widget.RecyclerView


class ContactAdapter (
    private val onContactClick:(Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private var items: List<Contact> = emptyList()

    fun submitList(newItems:List<Contact>){
        items = newItems
        notifyDataSetChanged()
    }

    class ContactViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val initialText: TextView = itemView.findViewById(R.id.tvInitial)
        val nameText: TextView = itemView.findViewById(R.id.tvName)
        val phoneText: TextView = itemView.findViewById(R.id.tvPhone)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact,parent,false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder:ContactViewHolder, position: Int) {
        val contact = items[position]
        holder.initialText.text = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?:"?"
        holder.nameText.text = contact.name
        holder.phoneText.text = contact.phone
        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }
    override fun getItemCount(): Int = items.size
}