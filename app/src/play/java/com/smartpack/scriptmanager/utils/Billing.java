/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of Script Manager, an app to create, import, edit
 * and easily execute any properly formatted shell scripts.
 *
 */

package com.smartpack.scriptmanager.utils;

import android.app.Activity;
import android.content.Intent;

import com.smartpack.scriptmanager.activities.BillingActivity;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 17, 2021
 */

public class Billing {

    public static void showDonateOption(Activity activity) {
        Intent donations = new Intent(activity, BillingActivity.class);
        activity.startActivity(donations);
    }

}