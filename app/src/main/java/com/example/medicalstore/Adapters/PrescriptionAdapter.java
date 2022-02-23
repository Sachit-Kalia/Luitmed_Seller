package com.example.medicalstore.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicalstore.Models.Prescription;
import com.example.medicalstore.Order;
import com.example.medicalstore.PrescriptionDetails;
import com.example.medicalstore.R;

import java.util.ArrayList;
import java.util.Calendar;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.OrderHolder>{

    private Context context;
    public ArrayList<Prescription> orderList;



    public PrescriptionAdapter(Context context, ArrayList<Prescription> orderList){
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.prescription_item, parent, false);
        return new OrderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderHolder holder, int position) {
        // get data
        Prescription orderItem = orderList.get(position);

        String id = orderItem.getId();
        String date = orderItem.getDate();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(date));
        String formattedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        // set data

        holder.oId.setText(""+ id);
        holder.oDate.setText("" + formattedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PrescriptionDetails.class);
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


        private TextView oId, oDate;

        public OrderHolder(@NonNull View itemView) {
            super(itemView);

            oId = itemView.findViewById(R.id.prId);
            oDate = itemView.findViewById(R.id.prDate);

        }
    }
}
