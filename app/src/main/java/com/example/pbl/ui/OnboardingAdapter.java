package com.example.pbl.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pbl.R;
import com.example.pbl.model.OnboardingPage;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingPage> pages;

    public OnboardingAdapter(List<OnboardingPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);
        holder.tvTitle.setText(page.getTitle());
        holder.tvSubtitle.setText(page.getSubtitle());
        holder.tvBody.setText(page.getBody());
        holder.ivIcon.setImageResource(page.getIconRes());
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvBody;
        ImageView ivIcon;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_onboarding_title);
            tvSubtitle = itemView.findViewById(R.id.tv_onboarding_subtitle);
            tvBody = itemView.findViewById(R.id.tv_onboarding_body);
            ivIcon = itemView.findViewById(R.id.iv_onboarding_icon);
        }
    }
}
