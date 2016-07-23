package net.machina.sdmmistrzejowice.common;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import net.machina.sdmmistrzejowice.R;

public class MapData {
    public static final int POINT_ALL_CATEGORIES = -1;
    public static final int POINT_CHURCH = 0;
    public static final int POINT_SHOP = 1;
    public static final int POINT_PHARMACY = 2;
    public static final int POINT_BANK = 3;
    public static final int POINT_EXCHANGE = 4;
    public static final int POINT_CLINIC = 5;
    public static final int POINT_SCHOOL = 6;

    public static String getLocalizedName(Context context, int category) {
        switch(category) {
            case POINT_CHURCH:
                return context.getString(R.string.category_church);
            case POINT_SHOP:
                return context.getString(R.string.category_shop);
            case POINT_PHARMACY:
                return context.getString(R.string.category_pharmacy);
            case POINT_BANK:
                return context.getString(R.string.category_bank);
            case POINT_EXCHANGE:
                return context.getString(R.string.category_exchange);
            case POINT_CLINIC:
                return context.getString(R.string.category_clinic);
            case POINT_SCHOOL:
                return context.getString(R.string.category_school);
            default:
                return "Unknown";
        }
    }

    public static BitmapDescriptor getPointIcon(int category) {
        switch(category) {
            case POINT_CHURCH:
                return BitmapDescriptorFactory.fromResource(R.drawable.church);
            case POINT_SHOP:
                return BitmapDescriptorFactory.fromResource(R.drawable.shop32);
            case POINT_PHARMACY:
                return BitmapDescriptorFactory.fromResource(R.drawable.pharmacy32);
            case POINT_BANK:
                return BitmapDescriptorFactory.fromResource(R.drawable.bank32);
            case POINT_EXCHANGE:
                return BitmapDescriptorFactory.fromResource(R.drawable.exchange32);
            case POINT_CLINIC:
                return BitmapDescriptorFactory.fromResource(R.drawable.first_aid32);
            case POINT_SCHOOL:
                return BitmapDescriptorFactory.fromResource(R.drawable.accomodation32);
            default:
                return BitmapDescriptorFactory.defaultMarker();
        }
    }
}
