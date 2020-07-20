package com.example.toVisit_Aman_C0772344_android;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SelectedPlaceAdapter extends RecyclerView.Adapter<SelectedPlaceAdapter.ViewHolder> {
    private Context context;
    private ArrayList<SelectedPlaceData> favouriteList;
    private OnClickListener onClickListener;

    public SelectedPlaceAdapter(Context context, ArrayList<SelectedPlaceData> favouriteList, OnClickListener onClickListener) {
        this.context = context;
        this.favouriteList = favouriteList;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public SelectedPlaceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_favourite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedPlaceAdapter.ViewHolder holder, int position) {
        final SelectedPlaceData data = favouriteList.get(position);
        holder.tvTitle.setText(data.getTitle());
        String distance = String.format(Locale.ENGLISH, "%.2f", data.getDistance());
        String finalDistance = "Distance:" + distance + " KM";
        holder.tvDistance.setText(finalDistance);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("latitude", data.getLatitude());
                intent.putExtra("longitude", data.getLongitude());
                intent.putExtra("title", data.getTitle());
                intent.putExtra("isFromList", true);
                intent.putExtra("placeId", data.getPlace_id());
                context.startActivity(intent);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favouriteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDistance;
        private ImageView ivDelete;
        private Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDistance = itemView.findViewById(R.id.tv_distance);

            btnDelete = itemView.findViewById(R.id.deleteBtn);

        }
    }

    interface OnClickListener {
        void onClick(SelectedPlaceData selectedPlaceData);
    }
}
