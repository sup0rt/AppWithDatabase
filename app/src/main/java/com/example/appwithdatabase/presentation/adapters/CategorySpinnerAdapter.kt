package com.example.appwithdatabase.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.appwithdatabase.data.entities.CategoryEntity

class CategorySpinnerAdapter(
    context: Context,
    private val categories: List<CategoryEntity>
) : ArrayAdapter<CategoryEntity>(context, android.R.layout.simple_spinner_item, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)

        val category = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)

        category?.let {
            textView.text = it.name
            // Можно добавить цвет категории слева
        }

        return view
    }
}