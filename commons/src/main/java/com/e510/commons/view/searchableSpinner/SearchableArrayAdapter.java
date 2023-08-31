package com.e510.commons.view.searchableSpinner;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class SearchableArrayAdapter extends ArrayAdapter<SearchableItem> implements Filterable {
    private List<SearchableItem> mOrigionalValues;
    private List<SearchableItem> mObjects;
    private Filter mFilter;

    public SearchableArrayAdapter(Context context, int textViewResourceId, List<SearchableItem> items) {
        super(context, textViewResourceId);
        mOrigionalValues = new ArrayList<SearchableItem>();
        mObjects = new ArrayList<SearchableItem>();

        if (items != null)
        {
            mOrigionalValues.addAll(items);
            mObjects.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void add(SearchableItem object) {
        mOrigionalValues.add(object);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public SearchableItem getItem(int position) {
        return mObjects.get(position);
    }

    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CustomFilter();
        }
        return mFilter;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0) {
                ArrayList<SearchableItem> list = new ArrayList<SearchableItem>(mOrigionalValues);
                results.values = list;
                results.count = list.size();
            } else {
                ArrayList<SearchableItem> newValues = new ArrayList<SearchableItem>();
                for(int i = 0; i < mOrigionalValues.size(); i++) {
                    SearchableItem item = mOrigionalValues.get(i);
                    if(item.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newValues.add(item);
                    }
                }
                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mObjects = (List<SearchableItem>) results.values;
            //Log.d("CustomArrayAdapter", String.valueOf(results.values));
            //Log.d("CustomArrayAdapter", String.valueOf(results.count));
            notifyDataSetChanged();
        }

    }

}
