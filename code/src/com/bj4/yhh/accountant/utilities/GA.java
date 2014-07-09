
package com.bj4.yhh.accountant.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import android.content.Context;
import android.os.Build;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class GA {
    private static Tracker mTracker;

    public static class CATEGORY {
        public static final String CATEGORY_CREATE_PLAN_FRAGMENT = "create_plan_fragment";

        public static final String CATEGORY_MAIN_ENRTY_FRAGMENT = "main_entry_fragment";

        public static final String CATEGORY_OVER_VIEW_FRAGMENT = "over_view_fragment";

        public static final String CATEGORY_TEST_FRAGMENT = "test_fragment";
        
        public static final String CATEGORY_PARSE_DATA_TIME = "parse data time";
    }

    public static class ACTIONS {
        public static final String ACTIONS_ADD_NEW_PLAN = "add_new_plan";

        public static final String ACTIONS_DELETE_PLAN = "delete_plan";

        public static final String ACTIONS_DELETE_PLAN_FROM_LIST = "delete_plan_from_list";

        public static final String ACTIONS_SHOW_LAW_PARAGRAPH = "show_law_paragraph";

        public static final String ACTIONS_TEST_REVIEW_PLAN = "test_review_plan";

        public static final String ACTIONS_TEST_REVIEW_OVERALL = "test_review_overall";

        public static final String ACTIONS_TEST_REVIEW_RANDOM = "review_random";

        public static final String ACTIONS_OVERVIEW_SORT = "overview_sorting";

        public static final String ACTIONS_SHOW_LARGE_CONTENT = "show_large_content";
    }

    public static class LABELS {
        public static final String LABELS_COMPANY_LAW = "company_law";

        public static final String LABELS_LAND_LAW = "land_law";

        public static final String LABELS_TAX_COLLECTIONS = "tax_collections_law";

        public static final String LABELS_VALUE_BUSINESS_LAW = "value_business_law";

        public static final String LABELS_ESTATE_GIFT_TAX_LAW = "estate_gift_tax_law";

        public static final String LABELS_BUSINESS_ENTITY_ACCOUNTING_LAW = "business_entity_accounting_law";

        public static final String LABELS_SECURITY_EXCHANGE_LAW = "security_exchange_law";

        public static final String LABELS_INCOME_TAX_LAW = "income_tax_law";
    }

    private synchronized static Tracker getInstanceTracker(Context context) {
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).getTracker("UA-45176399-4");
        }
        return mTracker;
    }

    public static void sendEvents(Context context, String category, String action, String label,
            Long value) {
        try {
            getInstanceTracker(context).send(
                    MapBuilder.createEvent(category, action, label, value).build());
        } catch (Exception e) {
        }
    }

    public static void sendView(Context context, String appScreen, HashMap<Integer, String> dimenMap) {
        try {
            Tracker tracker = getInstanceTracker(context);
            tracker.set(Fields.SCREEN_NAME, appScreen);
            MapBuilder mb = MapBuilder.createAppView();
            setUserScopDimension(mb, tracker.get(Fields.CLIENT_ID));
            StringBuilder sb = new StringBuilder();
            setDimension(mb, dimenMap, sb);
            tracker.send(mb.build());
        } catch (Exception e) {
        }
    }

    public static void sendTiming(Context context, String category, long intervalInMilliseconds,
            String name, String label) {
        try {
            Tracker tracker = getInstanceTracker(context);
            tracker.send(MapBuilder.createTiming(category, intervalInMilliseconds, name, label)
                    .build());
        } catch (Exception e) {
        }
    }

    public static class DefaultDimension {
        public static final int BUILD_MODEL = 1;

        public static final int BUILD_FINGERPRINT = 2;

        public static final int BUILD_TYPE = 3;

        public static final int BUILD_DEVICE = 3;

    }

    private static void setDimension(MapBuilder mb, HashMap<Integer, String> dimenMap,
            StringBuilder sb) {
        if (mb == null || dimenMap == null || dimenMap.isEmpty()) {
            return;
        }
        Set<Entry<Integer, String>> set = dimenMap.entrySet();
        for (Entry<Integer, String> entry : set) {
            int dimen = entry.getKey();
            String dimVal = entry.getValue();
            mb.set(Fields.customDimension(dimen), dimVal);
            if (sb != null) {
                sb.append("dimension ").append(dimen).append(" = ").append(dimVal).append('\n');
            }
        }
    }

    private static void setUserScopDimension(MapBuilder mb, String cid) {
        mb.set(Fields.customDimension(DefaultDimension.BUILD_MODEL), Build.MODEL);
        mb.set(Fields.customDimension(DefaultDimension.BUILD_FINGERPRINT), Build.FINGERPRINT);
        mb.set(Fields.customDimension(DefaultDimension.BUILD_TYPE), Build.TYPE);
        mb.set(Fields.customDimension(DefaultDimension.BUILD_DEVICE), Build.DEVICE);
    }
}
