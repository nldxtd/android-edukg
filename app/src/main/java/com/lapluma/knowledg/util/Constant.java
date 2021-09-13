package com.lapluma.knowledg.util;

import com.lapluma.knowledg.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Constant {
    /** define result codes and their meanings.
     * used for passing-back information between activities.
     */
    public static class ResultCode {
        final public static int NAVIGATE_TO_HOME = 10;
        final public static int REFRESH_HOME = 11;
    }
    public static final Map<String, Integer> category2StringId;
    static {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("", R.string.category_null);
        map.put("all", R.string.category_all);
        map.put("chinese", R.string.category_chinese);
        map.put("math", R.string.category_math);
        map.put("english", R.string.category_english);
        map.put("physics", R.string.category_physics);
        map.put("chemistry", R.string.category_chemistry);
        map.put("biology", R.string.category_biology);
        map.put("geo", R.string.category_geo);
        map.put("history", R.string.category_history);
        map.put("politics", R.string.category_politics);

        category2StringId = Collections.unmodifiableMap(map);

    }
}
