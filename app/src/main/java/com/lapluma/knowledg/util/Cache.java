package com.lapluma.knowledg.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.lapluma.knowledg.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Cache {

    public static class BitmapCacheHelper {
        private static DiskLruCache dlc = null;
        protected static Activity owner;
        public BitmapCacheHelper(Activity owner) {
            this.owner = owner;
            if (dlc == null) {
                try {
                    dlc = DiskLruCache.open(Tool.getDiskCacheDir(owner.getApplicationContext(), "bitmap"), Tool.getAppVersion(owner.getApplicationContext()), 1, 1024 * 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                    Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_init_fail));
                }
            }
        }
        public boolean saveBitmap(String key, Bitmap image) {
            try {
                DiskLruCache.Editor editor = dlc.edit(Tool.hashKeyForDisk(key));
                OutputStream os = editor.newOutputStream(0);
                image.compress(Bitmap.CompressFormat.JPEG, 100, os);
                editor.commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_save_fail));
                return false;
            }
        }
        public boolean loadBitmap(String key, ImageView view) {
            try {
                DiskLruCache.Snapshot snapshot = dlc.get(Tool.hashKeyForDisk(key));
                if (snapshot == null) {
                    return false;
                }
                InputStream in = snapshot.getInputStream(0);
                Bitmap image = BitmapFactory.decodeStream(in);
                in.close();
                owner.runOnUiThread(()->view.setImageBitmap(image));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_load_fail));
                return false;
            }
        }
    }

    public static class ObjectCacheHelper<T> {
        private static DiskLruCache dlc = null;
        protected static Activity owner;

        public ObjectCacheHelper(Activity owner, String objectType) {
            this.owner = owner;
            if (dlc == null) {
                try {
                    dlc = DiskLruCache.open(Tool.getDiskCacheDir(owner.getApplicationContext(), objectType), Tool.getAppVersion(owner.getApplicationContext()), 1, 1024 * 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                    Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_init_fail));
                }
            }
        }

        public boolean saveObject(String key, T obj) {
            try {
                DiskLruCache.Editor editor = dlc.edit(Tool.hashKeyForDisk(key));
                OutputStream os = editor.newOutputStream(0);
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(obj);
                editor.commit();
//                Tool.makeSnackBar(owner, "successfully saved object into cache."); // ONLY FOR DEBUGGING
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_save_fail));
                return false;
            }
        }

        public T loadObj(String key) {
            try {
                DiskLruCache.Snapshot snapshot = dlc.get(Tool.hashKeyForDisk(key));
                if (snapshot == null) {
                    return null;
                }
                InputStream in = snapshot.getInputStream(0);
                ObjectInputStream ois = new ObjectInputStream(in);
                T obj = (T) ois.readObject();
//                Tool.makeSnackBar(owner, "successfully loaded object from cache."); // ONLY FOR DEBUGGING
                return obj;
            } catch (Exception e) {
                e.printStackTrace();
                Tool.makeSnackBar(owner, owner.getString(R.string.error_cache_load_fail));
                return null;
            }
        }
    }

}
