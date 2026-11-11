package com.toptoche.searchablespinnerlibrary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.creatrix.unicorn.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Vendored from com.github.miteshpithadiya:SearchableSpinner; the search now matches anywhere in the text, not only word starts. */
public class SearchableListDialog extends DialogFragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private static final String ITEMS = "items";
    private List<Object> _allItems;
    private ArrayAdapter<Object> listAdapter;
    private SearchableItem _searchableItem;
    private OnSearchTextChanged _onSearchTextChanged;
    private SearchView _searchView;
    private String _strTitle;
    private String _strPositiveButtonText;
    private DialogInterface.OnClickListener _onClickListener;

    public static SearchableListDialog newInstance(List items) {
        SearchableListDialog dialog = new SearchableListDialog();
        Bundle args = new Bundle();
        args.putSerializable(ITEMS, (Serializable) items);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) _searchableItem = (SearchableItem) savedInstanceState.getSerializable("item");
        View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.searchable_list_dialog, null);
        setData(rootView);
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(rootView)
                .setPositiveButton(_strPositiveButtonText == null ? "CLOSE" : _strPositiveButtonText, _onClickListener)
                .setTitle(_strTitle == null ? "Select Item" : _strTitle)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("item", _searchableItem);
        super.onSaveInstanceState(outState);
    }

    public void setTitle(String strTitle) {
        _strTitle = strTitle;
    }

    public void setPositiveButton(String strPositiveButtonText) {
        _strPositiveButtonText = strPositiveButtonText;
    }

    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
        _strPositiveButtonText = strPositiveButtonText;
        _onClickListener = onClickListener;
    }

    public void setOnSearchableItemClickListener(SearchableItem searchableItem) {
        _searchableItem = searchableItem;
    }

    public void setOnSearchTextChangedListener(OnSearchTextChanged onSearchTextChanged) {
        _onSearchTextChanged = onSearchTextChanged;
    }

    private void setData(View rootView) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        _searchView = rootView.findViewById(R.id.search);
        _searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        _searchView.setIconifiedByDefault(false);
        _searchView.setOnQueryTextListener(this);
        _searchView.setOnCloseListener(this);
        _searchView.clearFocus();
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(_searchView.getWindowToken(), 0);

        _allItems = (List<Object>) getArguments().getSerializable(ITEMS);
        // a copy: filtering clears/refills the adapter, and _allItems is the spinner's own list
        listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<>(_allItems));
        ListView listView = rootView.findViewById(R.id.listItems);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            _searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
            getDialog().dismiss();
        });
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        _searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        String q = s == null ? "" : s.trim().toLowerCase();
        List<Object> hits = new ArrayList<>();
        for (Object o : _allItems) {
            if (String.valueOf(o).toLowerCase().contains(q)) hits.add(o);
        }
        listAdapter.setNotifyOnChange(false);
        listAdapter.clear();
        listAdapter.addAll(hits);
        listAdapter.notifyDataSetChanged();
        if (_onSearchTextChanged != null) _onSearchTextChanged.onSearchTextChanged(s);
        return true;
    }

    public interface OnSearchTextChanged {
        void onSearchTextChanged(String s);
    }

    public interface SearchableItem extends Serializable {
        void onSearchableItemClicked(Object item, int position);
    }
}
