package com.tools.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joye on 15-5-19.
 * ContentProvider to provider service
 */
public class ServiceProvider extends ContentProvider {

    public static final String EXTRA_BINDER = "binder";
    public static final String CMD_ADD = "add";
    public static final String CMD_GET = "get";

    public static Uri getContentUri(Context context) {
        return Uri.parse(String.format("content://%1$s.local_service", context.getPackageName()));
    }

    private Map<String, IBinder> registeredServices;

    @Override
    public boolean onCreate() {
        registeredServices = new HashMap<String, IBinder>();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, String arg, Bundle extras) {
        switch (method) {
            case CMD_ADD:
                return handleAdd(arg, extras);
            case CMD_GET:
                return handleGet(arg);
        }
        return null;
    }

    private Bundle handleGet(String serviceName) {
        IBinder service = doGet(serviceName);
        if (service != null) {
            Bundle result = new Bundle();
            result.putParcelable(EXTRA_BINDER, new IBinderWrapper(service));

            return result;
        } else {
            return null;
        }
    }

    private IBinder doGet(String serviceName) {
        return registeredServices.get(serviceName);
    }

    private Bundle handleAdd(String serviceName, Bundle extras) {
        if (getContext() == null) {
            return null;
        }
        extras.setClassLoader(getContext().getClassLoader());
        IBinderWrapper wrapper = extras.getParcelable(EXTRA_BINDER);
        if (wrapper != null) {
            registeredServices.put(serviceName, wrapper.getService());
        } else {
            // remove operation
            registeredServices.remove(serviceName);
        }
        return new Bundle();
    }
}
