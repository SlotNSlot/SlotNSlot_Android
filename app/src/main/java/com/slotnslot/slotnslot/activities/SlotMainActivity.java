package com.slotnslot.slotnslot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.slotnslot.slotnslot.MyPageType;
import com.slotnslot.slotnslot.R;
import com.slotnslot.slotnslot.SlotType;
import com.slotnslot.slotnslot.adapters.TabPagerAdapter;
import com.slotnslot.slotnslot.geth.Utils;
import com.slotnslot.slotnslot.models.AccountViewModel;
import com.slotnslot.slotnslot.provider.AccountProvider;
import com.slotnslot.slotnslot.provider.RxSlotRoom;
import com.slotnslot.slotnslot.provider.RxSlotRooms;
import com.slotnslot.slotnslot.utils.Constants;
import com.slotnslot.slotnslot.utils.Convert;
import com.slotnslot.slotnslot.utils.SlotUtil;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotMainActivity extends SlotRootActivity {
    public static final String TAG = SlotMainActivity.class.getSimpleName();

    @BindView(R.id.slot_tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.slot_viewpager)
    ViewPager viewPager;
    @BindView(R.id.nav_address_textview)
    TextView addressTextView;
    @BindView(R.id.nav_amount_textview)
    TextView amountTextView;
    @BindView(R.id.global_loading_container)
    RelativeLayout loadingView;

    private AccountViewModel accountViewModel;
    private boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_main);
        ButterKnife.bind(this);

        setLoadingView(loadingView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tabLayout.addTab(tabLayout.newTab().setText("PLAY"));
        tabLayout.addTab(tabLayout.newTab().setText("MAKE"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            int space = SlotUtil.convertDpToPixel(10.5f, this);
            p.setMargins(space, 0, space, 0);
            tab.requestLayout();
        }

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setAccountModel();
        RxSlotRooms.init();
        continuePlaying();
    }

    private void continuePlaying() {
        RxSlotRooms
                .rxSlotRoomMapSubject
                .debounce(2, TimeUnit.SECONDS)
                .take(1)
                .subscribe(slotMap -> {
                    for (RxSlotRoom rxSlotRoom : slotMap.values()) {
                        if (AccountProvider.identical(rxSlotRoom.getSlotRoom().getPlayerAddress())) {
                            Intent intent = new Intent(getApplicationContext(), SlotGameActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.ACTIVITY_EXTRA_KEY_SLOT_TYPE, SlotType.PLAYER);
                            bundle.putSerializable(Constants.BUNDLE_KEY_SLOT_ROOM, rxSlotRoom.getSlotAddress());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                        }
                    }
                }, Throwable::printStackTrace);
    }

    private void setAccountModel() {
        accountViewModel = new AccountViewModel(AccountProvider.accountSubject);

        accountViewModel.balance
                .subscribe(bigInteger -> amountTextView.setText(Convert.fromWei(bigInteger, Convert.Unit.ETHER) + " ETH"));
        accountViewModel.addressHex
                .subscribe(hex -> addressTextView.setText(hex));

        AccountProvider.getBalance();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Utils.showToast("press back again to exit");
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
        }
    }

    @OnClick(R.id.nav_wallet_layout)
    public void showWallet() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
        intent.putExtra(com.slotnslot.slotnslot.utils.Constants.ACTIVITY_EXTRA_KEY_MY_PAGE_TYPE, MyPageType.WALLET);
        startActivity(intent);
    }

    @OnClick(R.id.nav_address_layout)
    public void showAddess() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        Intent intent = new Intent(getApplicationContext(), MyPageActivity.class);
        intent.putExtra(com.slotnslot.slotnslot.utils.Constants.ACTIVITY_EXTRA_KEY_MY_PAGE_TYPE, MyPageType.WITHDRAW);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxSlotRooms.destroy();
    }
}
