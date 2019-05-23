package com.codeforces.commons.rate;

import com.codeforces.commons.pair.SimplePair;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused")
public class RateUtil {
    private static final Logger logger = Logger.getLogger(RateUtil.class);

    private static final ConcurrentMap<String, SimplePair<Long, Integer>> maxRatePerMinuteByScope = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ConcurrentMap<String, Data>> datas = new ConcurrentHashMap<>();
    private static AtomicLong dataCount = new AtomicLong();
    private static AtomicLong counter = new AtomicLong();

    private RateUtil() {
        // No operations.
    }

    public static void setRestriction(String scope, long intervalMillis, int maxRatePerIntervalMillis) {
        maxRatePerMinuteByScope.put(scope, new SimplePair<>(intervalMillis, maxRatePerIntervalMillis));
    }

    public static boolean addEvent(String scope, String session) {
        clear();

        SimplePair<Long, Integer> restriction = maxRatePerMinuteByScope.get(scope);
        if (restriction == null) {
            throw new IllegalStateException("No restriction for the scope '" + scope + "', use #setRestriction(scope, maxRatePerMinute).");
        }

        ConcurrentMap<String, Data> scopeDatas = datas.compute(scope, (s, stringQueueConcurrentMap) -> {
            if (stringQueueConcurrentMap == null) {
                return new ConcurrentHashMap<>();
            } else {
                return stringQueueConcurrentMap;
            }
        });

        @SuppressWarnings("ConstantConditions") final Data data = scopeDatas.compute(session, (k, v) -> v == null
                ? new Data(4, restriction.getFirst(), restriction.getSecond()) : v);

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (data) {
            boolean add = data.add();
            if (!add) {
                logger.info("Too many events, RateUtil#addEvent returns false [scope=" + scope + ", session=" + session + "].");
            }
            return add;
        }
    }

    private static void clear() {
        if (2 * dataCount.get() < counter.incrementAndGet()) {
            logger.warn("Clearing RateUtil [" + dataCount.get() + ", " + counter.get() + "].");
            counter.set(0);

            long startTimeMillis = System.currentTimeMillis();
            int previousSize = 0;
            int removeSize = 0;

            synchronized (datas) {
                for (Map.Entry<String, ConcurrentMap<String, Data>> e : datas.entrySet()) {
                    previousSize += e.getValue().size();
                    List<String> emptySessions = new ArrayList<>();
                    for (Map.Entry<String, Data> sessionAndData : e.getValue().entrySet()) {
                        synchronized (sessionAndData.getValue()) {
                            sessionAndData.getValue().adjust(startTimeMillis);
                            if (sessionAndData.getValue().isEmpty()) {
                                emptySessions.add(sessionAndData.getKey());
                            }
                        }
                    }
                    for (String emptySession : emptySessions) {
                        logger.warn("Cleared RateUtil session [scope=" + e.getKey() + ", session=" + emptySession + "].");
                        e.getValue().remove(emptySession);
                        removeSize++;
                    }
                }
            }

            logger.warn("RateUtil#clear done [size: " + previousSize + " -> " + (previousSize - removeSize) + " " + (-removeSize) + "].");
        }
    }

    private static final class Data {
        private final int depth;
        private final long intervalMills;
        private final long maxRatePerMinute;
        private final Queue<Long>[] queues;

        private Data(int depth, long intervalMills, int maxRatePerIntervalMillis) {
            long count;
            if ((count = RateUtil.dataCount.incrementAndGet()) % 1000 == 0) {
                logger.info("Created instance of RateUtil#Data [count=" + count + "].");
            }

            this.depth = depth;
            this.intervalMills = intervalMills;
            this.maxRatePerMinute = maxRatePerIntervalMillis;
            //noinspection unchecked
            queues = new Queue[depth];
            for (int i = 0; i < depth; i++) {
                queues[i] = new LinkedList<>();
            }
        }

        private boolean add() {
            long currentTimeMillis = System.currentTimeMillis();
            adjust(currentTimeMillis);

            if (isProper()) {
                queues[0].add(currentTimeMillis);
                return true;
            } else {
                return false;
            }
        }

        private void adjust(long currentTimeMillis) {
            for (int i = depth - 1; i >= 0; i--) {
                Queue<Long> queue = queues[i];
                while (!queue.isEmpty() && queue.peek() < currentTimeMillis - (1L << i) * intervalMills) {
                    long elem = queue.poll();
                    for (int j = i + 1; j < depth; j++) {
                        if (elem >= currentTimeMillis - (1L << j) * intervalMills) {
                            queues[j].add(elem);
                            break;
                        }
                    }
                }
            }
        }

        private boolean isEmpty() {
            for (Queue<Long> queue : queues) {
                if (!queue.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        private boolean isProper() {
            long sum = 0;
            long lim = maxRatePerMinute << (depth - 1);
            for (int i = 0; i < depth; i++) {
                sum += queues[i].size();
                if (sum >= lim) {
                    return false;
                }
            }
            return true;
        }
    }
}
