package com.example.medicalstore.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalstore.Order;
import com.example.medicalstore.OrderDetails;
import com.example.medicalstore.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderHolder>{

    private Context context;
    public ArrayList<Order> orderList;



    public OrderAdapter(Context context, ArrayList<Order> orderList){
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderHolder(view);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull OrderHolder holder, int position) {
        // get data
        Order orderItem = orderList.get(position);

        String id = orderItem.getId();
        String status = orderItem.getStatus();
        String date = orderItem.getDate();
        String price = orderItem.getPrice();


        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aaa  dd/MM/yyyy");
        String formattedDate = formatter.format(new Date(Long.parseLong(date)));

        // set data

        holder.oId.setText(""+ id);
        holder.oStatus.setText(""+ status);
        holder.oDate.setText("" + formattedDate);
        holder.oPrice.setText("₹" + price);

        switch (status) {
            case "Delivered":
                DrawableCompat.setTint(holder.oStatus.getBackground(), ContextCompat.getColor(context, R.color.green));
                break;
            case "Cancelled":
                DrawableCompat.setTint(holder.oStatus.getBackground(), ContextCompat.getColor(context, R.color.red));
                break;
            case "Dispatched":
                DrawableCompat.setTint(holder.oStatus.getBackground(), ContextCompat.getColor(context, R.color.orange));
                break;
            case "In Progress":
                DrawableCompat.setTint(holder.oStatus.getBackground(), ContextCompat.getColor(context, R.color.main));
                break;
        }

        // set colour of view according to status

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OrderDetails.class);
                intent.putExtra("id", id);
                context.startActivity(intent);
            }
        });


    }




    @Override
    public int getItemCount() {
        return orderList.size();
    }



    class OrderHolder extends RecyclerView.ViewHolder{


        private TextView oId, oStatus, oDate, oPrice;

        public OrderHolder(@NonNull View itemView) {
            super(itemView);

            oId = itemView.findViewById(R.id.oId);
            oStatus = itemView.findViewById(R.id.oStatus);
            oDate = itemView.findViewById(R.id.oDate);
            oPrice = itemView.findViewById(R.id.oPrice);

        }
    }
}

