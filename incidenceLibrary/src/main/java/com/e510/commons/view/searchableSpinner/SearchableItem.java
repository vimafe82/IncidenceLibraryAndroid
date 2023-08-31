package com.e510.commons.view.searchableSpinner;

import androidx.annotation.NonNull;

public class SearchableItem {
    public int id;
    public String name;

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
