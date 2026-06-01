package com.example.akuafodamfo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.akuafodamfo.R;
import com.example.akuafodamfo.models.Pest;
import java.util.List;

public class PestAdapter extends RecyclerView.Adapter<PestAdapter.PestViewHolder> {
    private List<Pest> pestList;

    public PestAdapter(List<Pest> pestList) {
        this.pestList = pestList;
    }

    @NonNull
    @Override
    public PestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pest, parent, false);
        return new PestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PestViewHolder holder, int position) {
        Pest pest = pestList.get(position);
        holder.bind(pest);
    }

    @Override
    public int getItemCount() {
        return pestList.size();
    }

    static class PestViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPestName;
        private TextView tvScientificName;
        private TextView tvConfidence;
        private TextView tvTreatment;

        public PestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPestName = itemView.findViewById(R.id.tvPestName);
            tvScientificName = itemView.findViewById(R.id.tvScientificName);
            tvConfidence = itemView.findViewById(R.id.tvConfidence);
            tvTreatment = itemView.findViewById(R.id.tvTreatment);
        }

        public void bind(Pest pest) {
            tvPestName.setText(pest.getName());
            tvScientificName.setText(pest.getScientificName());
            tvConfidence.setText(String.format("Confidence: %.1f%%", pest.getConfidence() * 100));
            tvTreatment.setText(pest.getTreatment());
        }
    }
}