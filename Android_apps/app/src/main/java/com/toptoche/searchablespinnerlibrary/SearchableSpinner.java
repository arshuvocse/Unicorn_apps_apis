package com.toptoche.searchablespinnerlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

/** Vendored from com.github.miteshpithadiya:SearchableSpinner (unused hintText support dropped); same package so layouts and callers are unchanged. */
public class SearchableSpinner extends Spinner implements View.OnTouchListener, SearchableListDialog.SearchableItem {
    public static final int NO_ITEM_SELECTED = -1;
    private final List<Object> _items = new ArrayList<>();
    private final SearchableListDialog _searchableListDialog = SearchableListDialog.newInstance(_items);

    public SearchableSpinner(Context context) {
        super(context);
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        _searchableListDialog.setOnSearchableItemClickListener(this);
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (_searchableListDialog.isAdded()) return true;
        SpinnerAdapter adapter = getAdapter();
        if (event.getAction() == MotionEvent.ACTION_UP && adapter != null) {
            _items.clear();
            for (int i = 0; i < adapter.getCount(); i++) _items.add(adapter.getItem(i));
            _searchableListDialog.show(scanForActivity(getContext()).getFragmentManager(), "TAG");
        }
        return true;
    }

    @Override
    public void onSearchableItemClicked(Object item, int position) {
        setSelection(_items.indexOf(item));
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

    private Activity scanForActivity(Context cont) {
        if (cont instanceof Activity) return (Activity) cont;
        if (cont instanceof ContextWrapper) return scanForActivity(((ContextWrapper) cont).getBaseContext());
        return null;
    }
}
