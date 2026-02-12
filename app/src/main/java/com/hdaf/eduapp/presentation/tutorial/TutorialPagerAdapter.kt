package com.hdaf.eduapp.presentation.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.hdaf.eduapp.R

/**
 * Adapter for the navigation tutorial ViewPager.
 * Each page explains a different accessibility feature.
 */
class TutorialPagerAdapter(
    private val pages: List<TutorialPage>
) : RecyclerView.Adapter<TutorialPagerAdapter.TutorialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tutorial_page, parent, false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class TutorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgTutorialIcon)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTutorialTitle)
        private val txtDescription: TextView = itemView.findViewById(R.id.txtTutorialDescription)
        private val txtSubtitle: TextView = itemView.findViewById(R.id.txtTutorialSubtitle)

        fun bind(page: TutorialPage) {
            imgIcon.setImageResource(page.iconRes)
            txtTitle.setText(page.titleRes)
            txtDescription.setText(page.descriptionRes)
            txtSubtitle.setText(page.subtitleRes)
            
            // Set content description for accessibility
            val title = itemView.context.getString(page.titleRes)
            val description = itemView.context.getString(page.descriptionRes)
            imgIcon.contentDescription = "$title illustration"
            itemView.contentDescription = "$title. $description"
        }
    }
}

/**
 * Data class representing a single tutorial page
 */
data class TutorialPage(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val subtitleRes: Int,
    val ttsText: String // Text to speak for this page
)
