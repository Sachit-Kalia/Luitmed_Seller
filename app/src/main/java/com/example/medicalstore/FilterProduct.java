package com.example.medicalstore;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterProduct extends Filter {

    private SellerProductAdapter adapter;
    private ArrayList<SellerProduct> filterList;

    public FilterProduct(SellerProductAdapter adapter, ArrayList<SellerProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // validate data for search query

        if(constraint != null && constraint.length() > 0){

            // Search field not empty

            constraint = constraint.toString().toUpperCase();
            ArrayList<SellerProduct> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }else{
            // Search field empty

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productList = (ArrayList<SellerProduct>) results.values;
        // refresh the adapter
        adapter.notifyDataSetChanged();;

    }
}

