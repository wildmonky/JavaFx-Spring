package com.zhuofan.validator.spring.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

    public static <T> List<List<T>> split(List<T> list, int batchSize) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        batchSize = batchSize == 0 ? 100 : batchSize;
        List<List<T>> reList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int index = i / batchSize;

            if (index > reList.size() - 1) {
                reList.add(new ArrayList<>());
            }

            List<T> ts = reList.get(index);
            ts.add(list.get(i));
        }

        return reList;
    }


}
