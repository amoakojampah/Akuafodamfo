package com.example.akuafodamfo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.akuafodamfo.R;
import com.example.akuafodamfo.models.MarketPrice;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MarketPriceAdapter extends RecyclerView.Adapter<MarketPriceAdapter.MarketPriceViewHolder> {
    private List<MarketPrice> marketPriceList;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public MarketPriceAdapter(List<MarketPrice> marketPriceList) {
        this.marketPriceList = marketPriceList;
    }

    @NonNull
    @Override
    public MarketPriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_market_price, parent, false);
        return new MarketPriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarketPriceViewHolder holder, int position) {
        MarketPrice marketPrice = marketPriceList.get(position);
        holder.bind(marketPrice);
    }

    @Override
    public int getItemCount() {
        return marketPriceList.size();
    }

    public void updateList(List<MarketPrice> newList) {
        this.marketPriceList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class MarketPriceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCropName;
        private TextView tvMarketName;
        private TextView tvPrice;
        private TextView tvUpdatedAt;

        public MarketPriceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            tvMarketName = itemView.findViewById(R.id.tvMarketName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
        }

        public void bind(MarketPrice marketPrice) {
            tvCropName.setText(marketPrice.getCropType());
            tvMarketName.setText(marketPrice.getMarketLocation());
            tvPrice.setText(String.format("%s %.2f", marketPrice.getCurrency(), marketPrice.getPrice()));
            tvUpdatedAt.setText(dateFormat.format(marketPrice.getUpdatedAt()));
        }
    }
}