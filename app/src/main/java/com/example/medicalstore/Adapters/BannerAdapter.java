package com.example.medicalstore.Adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalstore.Banners;
import com.example.medicalstore.Models.Banner;
import com.example.medicalstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerHolder> {

    private Context context;
    public ArrayList<Banner> bannerList;

    public BannerAdapter(Context context, ArrayList<Banner> bannerList){
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerAdapter.BannerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.banner_item, parent, false);
        return new BannerAdapter.BannerHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BannerAdapter.BannerHolder holder, int position) {
        // get data
        Banner bannerItem = bannerList.get(position);

        String id = bannerItem.getId();
        String url = bannerItem.getUrl();

        try{
            Picasso.get().load(url).placeholder(R.color.gray).into(holder.bannerImage);
        }catch (Exception e){
            holder.bannerImage.setImageResource(R.color.gray);
        }

        holder.bannerEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Banners)context).action = 3;
                ((Banners)context).bannerId = id;
                ((Banners)context).showImagePickDialog();
            }
        });

        holder.bannerDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Banners)context).bannerId = id;
                ((Banners)context).deleteBanner();
            }
        });

    }




    @Override
    public int getItemCount() {
        return bannerList.size();
    }



    class BannerHolder extends RecyclerView.ViewHolder{


        private ImageView bannerImage;
        private ImageButton bannerEdit, bannerDelete;

        public BannerHolder(@NonNull View itemView) {
            super(itemView);

            bannerImage = itemView.findViewById(R.id.bannerIV);
            bannerEdit = itemView.findViewById(R.id.editBanner);
            bannerDelete = itemView.findViewById(R.id.deleteBanner);

        }
    }

}
