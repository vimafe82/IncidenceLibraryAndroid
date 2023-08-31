package com.e510.commons.view.searchableSpinner;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;

import com.e510.commons.R;
import com.e510.commons.utils.FontUtils;
import com.e510.commons.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SearchableSpinner extends AppCompatSpinner implements View.OnTouchListener,
        SearchableListDialog.SearchItem {

    public static final int NO_ITEM_SELECTED = -1;
    private Context _context;
    private List _items;
    private SearchableListDialog _searchableListDialog;

    private boolean _isDirty;
    private ArrayAdapter _arrayAdapter;
    private String _strHintText;
    private boolean _isFromInit;

    private boolean visibleSearch = true;
    private OnSearchItemClicked _onSearchItemClicked;

    public SearchableSpinner(Context context, boolean withSearchView) {
        super(context);
        this._context = context;
        this.visibleSearch = withSearchView;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SearchableSpinner_hintText) {
                _strHintText = a.getString(attr);
            }
        }
        a.recycle();
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _items = new ArrayList();
        _searchableListDialog = SearchableListDialog.newInstance
                (_items);
        _searchableListDialog.showSearchView(visibleSearch);
        _searchableListDialog.setOnSearchItemClickListener(this);
        setOnTouchListener(this);

        _arrayAdapter = (ArrayAdapter) getAdapter();
        if (!TextUtils.isEmpty(_strHintText)) {
            ArrayList<SearchableItem> list = new ArrayList<>();
            SearchableItem it = new SearchableItem();
            it.id = 0;
            it.name = _strHintText;
            list.add(it);
            ArrayAdapter arrayAdapter = createAdapter(_context, list);

            _isFromInit = true;
            setAdapter(arrayAdapter);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (_searchableListDialog.isAdded()) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {

            showDialog();
        }
        return true;
    }

    public void showDialog()
    {
        try {
            if (null != _arrayAdapter) {

                // Refresh content #6
                // Change Start
                // Description: The items were only set initially, not reloading the data in the
                // spinner every time it is loaded with items in the adapter.
                _items.clear();
                for (int i = 0; i < _arrayAdapter.getCount(); i++) {
                    _items.add(_arrayAdapter.getItem(i));
                }
                // Change end.

                _searchableListDialog.show(scanForActivity(_context).getFragmentManager(), "TAG");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {

        if (!_isFromInit) {
            _arrayAdapter = (ArrayAdapter) adapter;
            if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {

                ArrayList<SearchableItem> list = new ArrayList<>();
                SearchableItem it = new SearchableItem();
                it.id = 0;
                it.name = _strHintText;
                list.add(it);
                ArrayAdapter arrayAdapter = createAdapter(_context, list);

                super.setAdapter(arrayAdapter);
            } else {
                super.setAdapter(adapter);
            }

        } else {
            _isFromInit = false;
            super.setAdapter(adapter);
        }
    }

    @Override
    public void onSearchableItemClicked(Object item, int position) {
        setSelection(_items.indexOf(item));

        if (!_isDirty) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setSelection(_items.indexOf(item));
            if (_onSearchItemClicked != null) {
                _onSearchItemClicked.onSearchItemClicked((SearchableItem)item, position);
            }
            _isDirty = false;
        }
    }

    public void setTitle(String strTitle) {
        _searchableListDialog.setTitle(strTitle);
    }

    public void setPositiveButton(String strPositiveButtonText) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText);
    }

    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText, onClickListener);
    }

    public void setOnSearchTextChangedListener(SearchableListDialog.OnSearchTextChanged onSearchTextChanged) {
        _searchableListDialog.setOnSearchTextChangedListener(onSearchTextChanged);
    }

    public void setOnSearchItemClickedListener(OnSearchItemClicked onSearchItemClicked) {
        _onSearchItemClicked = onSearchItemClicked;
    }

    private Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    @Override
    public int getSelectedItemPosition() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return NO_ITEM_SELECTED;
        } else {
            return super.getSelectedItemPosition();
        }
    }

    @Override
    public Object getSelectedItem() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return super.getSelectedItem();
        }
    }

    public ArrayAdapter<SearchableItem> createAdapter(Context ctx, ArrayList<SearchableItem> items)
    {
        SearchableArrayAdapter adapter = new SearchableArrayAdapter(ctx, R.layout.searchable_spinner_textview, items) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                FontUtils.setTypeValueText(v, ctx);

                boolean isHint = false;
                if (!TextUtils.isEmpty(_strHintText)) {

                    if (items != null && items.size() == 1)
                    {
                        SearchableItem str = items.get(0);
                        if (str.name.equals(_strHintText))
                        {
                            isHint = true;
                        }
                    }
                }

                if (isHint)
                {
                    ((TextView) v).setTextColor(Utils.getColor(ctx, R.color.hintGray));
                }
                else
                {
                    ((TextView) v).setTextColor(Utils.getColor(ctx, R.color.black));
                }

                return v;
            }

            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                FontUtils.setTypeValueText(v, ctx);

                return v;
            }
        };

        return adapter;
    }

    public interface OnSearchItemClicked {
        void onSearchItemClicked(SearchableItem item, int position);
    }
}
