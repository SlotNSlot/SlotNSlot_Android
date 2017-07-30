package com.slotnslot.slotnslot.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.activities.SignInUpActivity;
import com.slotnslot.slotnslot.adapters.SignInAdapter;
import com.slotnslot.slotnslot.geth.CredentialManager;
import com.slotnslot.slotnslot.models.Account;
import com.slotnslot.slotnslot.utils.SlotUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.subjects.PublishSubject;

public class SignInListFragment extends SlotRootFragment {
    private static final String TAG = SignInListFragment.class.getSimpleName();

    @BindView(R.id.signin_list_recyclerview)
    RecyclerView recyclerView;
    private SignInAdapter adapter;

    private PublishSubject<Fragment> clickItem = PublishSubject.create();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SignInUpActivity) getActivity()).setTitle("Sign in");

        clickItem.subscribe(fragment -> {
            FragmentManager fmanager = getActivity().getSupportFragmentManager();
            FragmentTransaction ftrans = fmanager.beginTransaction();
            ftrans.replace(R.id.fragment_framelayout, fragment);
            ftrans.addToBackStack(null);
            ftrans.commit();
        });
    }

    private ArrayList<Account> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signin_list, container, false);
        ButterKnife.bind(this, view);

        adapter = new SignInAdapter(items, clickItem);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SpacesItemDecoration(SlotUtil.convertDpToPixel(10f, getContext())));
        recyclerView.setAdapter(adapter);

        //TODO Add Account & adapter.notifyDataSetChanged;
        List<org.ethereum.geth.Account> list = CredentialManager.getAccounts();
        for (org.ethereum.geth.Account account : list) {
            Log.d("Address", account.getAddress().getHex());
            items.add(new Account(account));
        }

        adapter.notifyDataSetChanged();
        return view;
    }
}
