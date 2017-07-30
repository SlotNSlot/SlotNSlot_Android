package com.slotnslot.slotnslot.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.fragments.SignInFragment;
import com.slotnslot.slotnslot.models.Account;
import com.slotnslot.slotnslot.views.AccountViewHolder;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class SignInAdapter extends RecyclerView.Adapter {

    private ArrayList<Account> items = new ArrayList<>();
    private PublishSubject<Fragment> observable;
    private Disposable signinButtonDisposable;

    public SignInAdapter(ArrayList<Account> items, PublishSubject<Fragment> observable) {
        this.items = items;
        this.observable = observable;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_sign_in, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (signinButtonDisposable != null) {
//            signinButtonDisposable.dispose();
//        }

        AccountViewHolder viewHolder = (AccountViewHolder)holder;
        viewHolder.bindingAccount(items.get(position));
        signinButtonDisposable = RxView.clicks(viewHolder.getSignInButton())
                .subscribe(o -> {
                    Fragment frag = new SignInFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("account", items.get(position));
                    frag.setArguments(bundle);
                    observable.onNext(frag);
                });
        viewHolder.getSignInButton();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
