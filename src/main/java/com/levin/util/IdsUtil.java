package com.levin.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IdsUtil {
    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * 随机分段
     */
    public static List<Integer> random(int n, int L) {
        List<Integer> res = new ArrayList<>();
        int nn = n;
        for (int i = 0; i < n - 1; i++) {
            if (L <= 0) {
                res.add(0);
            } else {
                int a = Math.max(1, random.nextInt(Math.max(L / nn + 1, 1)));
                int b = random.nextInt(a) + L / nn;
                L = L - b;
                if (L <= 0) {
                    res.add(L + b);
                } else {
                    res.add(b);
                }
            }
            nn--;
        }

        res.add(L < 0 ? 0 : L);
        Collections.shuffle(res);
        return res;
    }
}
