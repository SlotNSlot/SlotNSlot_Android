package com.slotnslot.slotnslot.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.models.Account;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.item_sign_in_button)
    Button signInButton;

    public AccountViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindingAccount(Account account) {
        signInButton.setText(account.getAddressHex());
    }

    public Button getSignInButton() {
        return signInButton;
    }
}
