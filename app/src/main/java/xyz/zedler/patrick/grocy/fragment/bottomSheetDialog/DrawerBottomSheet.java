package xyz.zedler.patrick.grocy.fragment.bottomSheetDialog;

/*
    This file is part of Grocy Android.

    Grocy Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Grocy Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Grocy Android.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020 by Patrick Zedler & Dominic Zedler
*/

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import xyz.zedler.patrick.grocy.NavGraphDirections;
import xyz.zedler.patrick.grocy.R;
import xyz.zedler.patrick.grocy.activity.MainActivity;
import xyz.zedler.patrick.grocy.activity.ShoppingActivity;
import xyz.zedler.patrick.grocy.barcode.CameraXLivePreviewActivity;
import xyz.zedler.patrick.grocy.fragment.ConsumeFragment;
import xyz.zedler.patrick.grocy.fragment.MasterLocationsFragment;
import xyz.zedler.patrick.grocy.fragment.MasterProductGroupsFragment;
import xyz.zedler.patrick.grocy.fragment.MasterProductsFragment;
import xyz.zedler.patrick.grocy.fragment.MasterQuantityUnitsFragment;
import xyz.zedler.patrick.grocy.fragment.MasterStoresFragment;
import xyz.zedler.patrick.grocy.fragment.PurchaseFragment;
import xyz.zedler.patrick.grocy.fragment.ShoppingListFragment;
import xyz.zedler.patrick.grocy.fragment.StockFragment;
import xyz.zedler.patrick.grocy.util.ClickUtil;
import xyz.zedler.patrick.grocy.util.Constants;
import xyz.zedler.patrick.grocy.util.IconUtil;
import xyz.zedler.patrick.grocy.util.NetUtil;

public class DrawerBottomSheet extends CustomBottomSheet implements View.OnClickListener {

    private final static String TAG = DrawerBottomSheet.class.getSimpleName();

    private MainActivity activity;
    private View view;
    private SharedPreferences sharedPrefs;
    private ClickUtil clickUtil = new ClickUtil();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), R.style.Theme_Grocy_BottomSheetDialog);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        view = inflater.inflate(
                R.layout.fragment_bottomsheet_drawer, container, false
        );

        activity = (MainActivity) getActivity();
        assert activity != null;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

        view.findViewById(R.id.button_drawer_shopping_mode).setOnClickListener(v -> {
            dismiss();
            activity.startActivity(new Intent(activity, ShoppingActivity.class));
        });

        view.findViewById(R.id.button_drawer_batch_consume).setOnClickListener(v -> {
            navigate(DrawerBottomSheetDirections
                    .actionDrawerBottomSheetDialogFragmentToScanBatchFragment(
                            Constants.ACTION.CONSUME
                    ), true
            );
        });

        view.findViewById(R.id.button_drawer_batch_purchase).setOnClickListener(v -> {
            navigate(NavGraphDirections.actionGlobalScanBatchFragment(
                            Constants.ACTION.PURCHASE
                    ), true
            );
        });

        setOnClickListeners(
                R.id.linear_drawer_shopping_list,
                R.id.linear_drawer_consume,
                R.id.linear_drawer_purchase,
                R.id.linear_drawer_master_data,
                R.id.linear_settings,
                R.id.linear_feedback,
                R.id.linear_help
        );

        Fragment currentFragment = activity.getCurrentFragment();
        if(currentFragment instanceof ShoppingListFragment) {
            select(R.id.linear_drawer_shopping_list, R.id.text_drawer_shopping_list, false);
        } else if(currentFragment instanceof ConsumeFragment) {
            select(R.id.linear_drawer_consume, R.id.text_drawer_consume, false);
        } else if(currentFragment instanceof PurchaseFragment) {
            select(R.id.linear_drawer_purchase, R.id.text_drawer_purchase, false);
        } else if(currentFragment instanceof MasterProductsFragment
                || currentFragment instanceof MasterLocationsFragment
                || currentFragment instanceof MasterStoresFragment
                || currentFragment instanceof MasterQuantityUnitsFragment
                || currentFragment instanceof MasterProductGroupsFragment
        ) {
            select(R.id.linear_drawer_master_data, R.id.text_drawer_master_data, true);
        }

        hideDisabledFeatures();

        return view;
    }

    private void setOnClickListeners(@IdRes int... viewIds) {
        for (int viewId : viewIds) {
            view.findViewById(viewId).setOnClickListener(this);
        }
    }

    public void onClick(View v) {
        if(clickUtil.isDisabled()) return;

        switch(v.getId()) {
            case R.id.linear_drawer_shopping_list:
                navigate(DrawerBottomSheetDirections
                        .actionDrawerBottomSheetDialogFragmentToShoppingListFragment(), true);
                break;
            case R.id.linear_drawer_consume:
                navigate(DrawerBottomSheetDirections
                        .actionDrawerBottomSheetDialogFragmentToConsumeFragment(), true);
                break;
            case R.id.linear_drawer_purchase:
                navigate(DrawerBottomSheetDirections
                        .actionDrawerBottomSheetDialogFragmentToPurchaseFragment(), true);
                break;
            case R.id.linear_drawer_master_data:
                dismiss();
                activity.showBottomSheet(new MasterDataBottomSheet(), null);
                break;
            case R.id.linear_settings:
                Intent intent = new Intent(activity, CameraXLivePreviewActivity.class);
                startActivity(intent);

                /*IconUtil.start(view, R.id.image_settings);
                new Handler().postDelayed(() -> navigate(DrawerBottomSheetDirections
                        .actionDrawerBottomSheetDialogFragmentToSettingsActivity(), true), 300);*/
                break;
            case R.id.linear_feedback:
                navigate(DrawerBottomSheetDirections
                        .actionDrawerBottomSheetDialogFragmentToSettingsFragment(), true);
                break;
            case R.id.linear_help:
                IconUtil.start(view, R.id.image_help);
                new Handler().postDelayed(() -> {
                    dismiss();
                    boolean success = NetUtil.openURL(activity, Constants.URL.HELP);
                    if(!success) {
                        Snackbar.make(
                                activity.binding.frameMainContainer,
                                R.string.error_no_browser,
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
                }, 300);
                break;
        }
    }

    private void navigate(NavDirections directions, boolean popUp) {

        NavOptions.Builder builder = new NavOptions.Builder();
        builder.setEnterAnim(R.anim.slide_in_up).setPopExitAnim(R.anim.slide_out_down);
        if(popUp) builder.setPopUpTo(R.id.stockFragment, false);
        if(! (activity.getCurrentFragment() instanceof StockFragment)) {
            builder.setExitAnim(R.anim.slide_out_down);
        } else {
            builder.setExitAnim(R.anim.slide_no);
        }
        dismiss();
        NavHostFragment.findNavController(this).navigate(
                directions,
                builder.build()
        );
    }

    private void select(@IdRes int linearLayoutId, @IdRes int textViewId, boolean clickable) {
        LinearLayout linearLayout = view.findViewById(linearLayoutId);
        linearLayout.setBackgroundResource(R.drawable.bg_drawer_item_selected);
        linearLayout.setClickable(clickable);  // so selected entries can be disabled
        ((TextView) view.findViewById(textViewId)).setTextColor(
                ContextCompat.getColor(activity, R.color.retro_green_fg)
        );
    }

    private void hideDisabledFeatures() {
        if(!isFeatureEnabled(Constants.PREF.FEATURE_SHOPPING_LIST)) {
            view.findViewById(R.id.linear_drawer_shopping_list).setVisibility(View.GONE);
            view.findViewById(R.id.divider_drawer_shopping_list).setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isFeatureEnabled(String pref) {
        if(pref == null) return true;
        return sharedPrefs.getBoolean(pref, true);
    }

    @NonNull
    @Override
    public String toString() {
        return TAG;
    }
}
