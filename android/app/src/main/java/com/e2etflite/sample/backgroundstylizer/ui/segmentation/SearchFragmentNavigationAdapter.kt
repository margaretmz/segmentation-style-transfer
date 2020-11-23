package com.e2etflite.sample.backgroundstylizer.ui.segmentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.e2etflite.sample.backgroundstylizer.R
import com.e2etflite.sample.backgroundstylizer.databinding.SearchFragmentAdapterBinding
import java.util.*


class SearchFragmentNavigationAdapter(
        val mContext: Context,
        private var hitsList: ArrayList<String>?,
        private val mSearchClickItemListener: SearchClickItemListener
) :
        RecyclerView.Adapter<SearchFragmentNavigationAdapter.NavigationAdapterViewHolder>() {

    var indexPosition = -1

    interface SearchClickItemListener {
        fun onListItemClick(
                itemIndex: Int,
                sharedImage: ImageView?,
                type: String
        )
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            i: Int
    ): NavigationAdapterViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val binding = SearchFragmentAdapterBinding.inflate(inflater, parent, false)

        return NavigationAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
            holder: NavigationAdapterViewHolder,
            position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return if (hitsList != null && hitsList!!.size > 0) {
            hitsList!!.size
        } else {
            0
        }
    }

    inner class NavigationAdapterViewHolder(var binding: SearchFragmentAdapterBinding) :
            RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        override fun onClick(view: View) {
            val clickedPosition = adapterPosition
            mSearchClickItemListener.onListItemClick(
                    clickedPosition,
                    binding.imageFragmentAdapter,
                    hitsList!![clickedPosition]
            )
        }

        init {
            itemView.setOnClickListener {
                onClick(binding.imageFragmentAdapter)

                // On click set position
                indexPosition = adapterPosition

                // when clicked change dimensions
                val anim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in)
                itemView.startAnimation(anim)
                anim.fillAfter = true

                // clear all other animations from previous buttons
                notifyDataSetChanged()
            }

        }

        fun bind(
                position: Int
        ) {

            val imagePath = hitsList!![position]

            // if position is the same as the clicked button ...show it animated
            // this is used when recyclerview scrolls and recycles items
            if (position == indexPosition){
                val anim: Animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_in)
                itemView.startAnimation(anim)
                anim.fillAfter = true
            }


            /*Glide.with(mContext)
                .load(Uri.parse("file:///android_asset/thumbnails/$imagePath"))
                .centerInside()
                .into(binding.imageFragmentAdapter)*/

            binding.imageFragmentAdapter.setImageBitmap(getBitmapFromAsset(mContext, "thumbnails/$imagePath"))

        }
    }

    private fun getBitmapFromAsset(context: Context, path: String): Bitmap =
            context.assets.open(path).use { BitmapFactory.decodeStream(it) }

    companion object {

    }

}