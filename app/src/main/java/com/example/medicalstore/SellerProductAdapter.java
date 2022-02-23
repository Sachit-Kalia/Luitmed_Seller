package com.example.medicalstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.SellerProductHolder> implements Filterable {

    private Context context;
    public ArrayList<SellerProduct> productList, filterList;
    private FilterProduct filter;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    public SellerProductAdapter(Context context, ArrayList<SellerProduct> productList){
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public SellerProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.seller_product_item, parent, false);
        return new SellerProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerProductHolder holder, int position) {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // get data
        SellerProduct sellerProduct = productList.get(position);
        String id = sellerProduct.getProductID();
//        String uid = sellerProduct.getUid();
        String title = sellerProduct.getTitle();
        Integer titleSize = title.length();
        if(titleSize > 70){
            title = title.substring(0,70);
            title += "...";
        }

//        String desc = sellerProduct.getDescription();
        String price = sellerProduct.getPrice();
        String discPrice = sellerProduct.getDiscountedPrice();
//        String category = sellerProduct.getCategory();
//        String timestamp = sellerProduct.getTimestamp();
        String image = sellerProduct.getProductIcon();
        String quantity = sellerProduct.getQuantity();

        // set data

        holder.spTitle.setText(title);
        holder.spQuantity.setText(quantity);
        holder.spPrice.setText("₹" + price);
        holder.spDiscountedPrice.setText("₹" + discPrice);
        Double discountt = (((Double.parseDouble(price) - Double.parseDouble(discPrice))*100)/Double.parseDouble(price));
        int discount = (int)Math.floor(discountt);
        holder.spDiscount.setText("-" + String.valueOf(discount) + "%");
        if(price == discPrice){
            holder.spDiscount.setVisibility(View.GONE);
            holder.spPrice.setVisibility(View.GONE);
        }else{
            holder.spDiscount.setVisibility(View.VISIBLE);
            holder.spPrice.setVisibility(View.VISIBLE);
            holder.spPrice.setPaintFlags(holder.spPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // strike-through the MRP
        }

        try{
            Picasso.get().load(image).placeholder(R.color.gray).into(holder.productIV);
        }catch (Exception e){
            holder.productIV.setImageResource(R.drawable.splash_image);
        }

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isDisc = (price != discPrice);
                Intent intent = new Intent(context, EditProduct.class);
                intent.putExtra("title", sellerProduct.getTitle());
                intent.putExtra("description", sellerProduct.getDescription());
                intent.putExtra("category", sellerProduct.getCategory());
                intent.putExtra("quantity", quantity);
                intent.putExtra("subcategory", sellerProduct.getSubcategory());
                intent.putExtra("stock", sellerProduct.getStock());
                intent.putExtra("price", price);
                intent.putExtra("discPrice", discPrice);
                intent.putExtra("image", image);
                intent.putExtra("isDisc", isDisc);
                intent.putExtra("id", sellerProduct.getProductID());
                context.startActivity(intent);
            }
        });


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((SellerProducts)context).progressDialog.setMessage("Deleting product..");
                ((SellerProducts)context).progressDialog.show();

                db.collection("products").document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        productList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, productList.size());
                        ((SellerProducts)context).progressDialog.dismiss();
                        Toast.makeText(context, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ((SellerProducts)context).progressDialog.dismiss();
                        Toast.makeText(context, "Product couldn't be deleted.", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterProduct(this, filterList);
        }
        return filter;
    }

    class SellerProductHolder extends RecyclerView.ViewHolder{

        private ImageView productIV;
        private TextView spTitle, spQuantity, spDiscountedPrice, spPrice, spDiscount;
        private ImageButton edit, delete;

        public SellerProductHolder(@NonNull View itemView) {
            super(itemView);

            productIV = itemView.findViewById(R.id.productIV);
            spTitle = itemView.findViewById(R.id.spTitle);
            spDiscountedPrice = itemView.findViewById(R.id.spDiscountedPrice);
            spPrice = itemView.findViewById(R.id.spPrice);
            spQuantity = itemView.findViewById(R.id.spQuantity);
            spDiscount = itemView.findViewById(R.id.discount);
            edit = itemView.findViewById(R.id.editProduct);
            delete = itemView.findViewById(R.id.deleteProduct);
        }
    }
}

