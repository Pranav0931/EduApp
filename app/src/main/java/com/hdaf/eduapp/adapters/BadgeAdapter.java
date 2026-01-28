package com.hdaf.eduapp.adapters;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.gamification.Badge;
import com.hdaf.eduapp.gamification.GamificationManager;

import java.util.List;

/**
 * Adapter for displaying badges in a grid layout.
 * Shows earned badges in color and unearned badges grayed out.
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final Context context;
    private List<GamificationManager.BadgeStatus> badges;
    private OnBadgeClickListener listener;

    public interface OnBadgeClickListener {
        void onBadgeClick(Badge badge, boolean isEarned);
    }

    public BadgeAdapter(Context context, List<GamificationManager.BadgeStatus> badges) {
        this.context = context;
        this.badges = badges;
    }

    public void setOnBadgeClickListener(OnBadgeClickListener listener) {
        this.listener = listener;
    }

    public void updateBadges(List<GamificationManager.BadgeStatus> newBadges) {
        this.badges = newBadges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        GamificationManager.BadgeStatus badgeStatus = badges.get(position);
        Badge badge = badgeStatus.badge;
        boolean isEarned = badgeStatus.earned;

        // Set badge icon
        holder.imgBadgeIcon.setImageResource(badge.getIconResId());
        
        // Set badge name
        holder.txtBadgeName.setText(badge.getName());
        
        // Set XP reward
        holder.txtBadgeXP.setText("+" + badge.getXpReward() + " XP");

        // Apply visual state based on earned status
        if (isEarned) {
            // Full color for earned badges
            holder.imgBadgeIcon.setColorFilter(null);
            holder.imgLockOverlay.setVisibility(View.GONE);
            holder.txtBadgeName.setAlpha(1.0f);
            holder.txtBadgeXP.setAlpha(1.0f);
        } else {
            // Grayscale for unearned badges
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            holder.imgBadgeIcon.setColorFilter(new ColorMatrixColorFilter(matrix));
            holder.imgLockOverlay.setVisibility(View.VISIBLE);
            holder.txtBadgeName.setAlpha(0.6f);
            holder.txtBadgeXP.setAlpha(0.6f);
        }

        // Set accessibility description
        String accessibilityDesc = badge.getAccessibilityAnnouncement(isEarned);
        holder.itemView.setContentDescription(accessibilityDesc);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBadgeClick(badge, isEarned);
            }
        });
    }

    @Override
    public int getItemCount() {
        return badges != null ? badges.size() : 0;
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgBadgeIcon;
        ImageView imgLockOverlay;
        TextView txtBadgeName;
        TextView txtBadgeXP;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBadgeIcon = itemView.findViewById(R.id.img_badge_icon);
            imgLockOverlay = itemView.findViewById(R.id.img_lock_overlay);
            txtBadgeName = itemView.findViewById(R.id.txt_badge_name);
            txtBadgeXP = itemView.findViewById(R.id.txt_badge_xp);
        }
    }
}
