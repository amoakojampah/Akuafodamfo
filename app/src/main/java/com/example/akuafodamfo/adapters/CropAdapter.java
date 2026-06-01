package com.example.akuafodamfo.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.akuafodamfo.R;
import com.example.akuafodamfo.models.Crop;
import com.bumptech.glide.Glide;
import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {
    private List<Crop> cropList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Crop crop);
    }

    public CropAdapter(List<Crop> cropList, OnItemClickListener listener) {
        this.cropList = cropList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);
        holder.bind(crop, listener);
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCropImage;
        private TextView tvCropName;
        private TextView tvCropType;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCropImage = itemView.findViewById(R.id.ivCropImage);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            tvCropType = itemView.findViewById(R.id.tvCropType);
        }

        public void bind(final Crop crop, final OnItemClickListener listener) {
            tvCropName.setText(crop.getName());
            tvCropType.setText(crop.getType());

            Glide.with(itemView.getContext())
                    .load(crop.getImageUrl())
                    .placeholder(R.drawable.ic_crop_placeholder)
                    .into(ivCropImage);

            itemView.setOnClickListener(v -> listener.onItemClick(crop));
        }
    }
}
