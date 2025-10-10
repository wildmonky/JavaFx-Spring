package com.zhuofan.validator.javafx.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;
import org.springframework.util.comparator.Comparators;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class BuildService extends BaseProgressBarService<List<String>> {

    private static final String NUMBER_GET_PATTERN = "(\\d+)";

    public BuildService() {
        super(springBean("threadPoolTaskExecutor", ThreadPoolTaskExecutor.class),
                springBean("threadPoolTaskScheduler", ThreadPoolTaskScheduler.class));
    }

    public static List<String> replaceNumberAfterFAnd7(List<String> templateData, int from, int to) {
        if (CollectionUtils.isEmpty(templateData)) {
            return Collections.emptyList();
        }

        Pattern fPattern = Pattern.compile("F" + NUMBER_GET_PATTERN);
        Pattern sevenPattern = Pattern.compile("7" + NUMBER_GET_PATTERN);

        List<String> replacements = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            replacements.add(i + "");
        }

        Map<Pattern, List<String>> patternStringMap = new HashMap<>();
        patternStringMap.put(fPattern, replacements);
        patternStringMap.put(sevenPattern, replacements.stream().map(s -> String.format("%02d", Integer.parseInt(s))).toList());

        List<List<String>> all = new ArrayList<>();
        templateData.forEach(template -> all.add(replaceByPattern(template, patternStringMap)));

        List<String> result = new ArrayList<>();
        int max = all.stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);
        for (int i = 0; i < max; i++) {
            for (List<String> list : all) {
                result.add(list.size() > i ? list.get(i) : "");
            }
        }

        return result;

    }

    public static List<String> replaceByPattern(String templateStr,
                                          Map<Pattern, List<String>> replaceDataMap
    ) {
        if (StringUtils.isBlank(templateStr)) {
            return Collections.emptyList();
        }

        ReplaceInfo replaceInfo = parseToReplaceInfo(templateStr, replaceDataMap);

        int max = replaceDataMap.values().stream().map(List::size).max(Comparator.naturalOrder()).orElse(0);

        List<String> result = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            String str = replaceInfo.getStr();

            List<MatchInfo> matchInfos = replaceInfo.getMatchInfos();
            if (CollectionUtils.isEmpty(matchInfos)) {
                continue;
            }

            List<MatchInfo> sortedMatchInfos = matchInfos.stream().sorted((e1, e2) -> Comparators.nullsHigh().compare(e1.getFrom(), e2.getFrom())).toList();

            List<String> splitStrList = new ArrayList<>();
            int offset = 0;
            String suffix = "";
            for (MatchInfo sortedMatchInfo : sortedMatchInfos) {
                int start = sortedMatchInfo.getFrom() - offset;
                int end = sortedMatchInfo.getTo() - offset;

                Pattern pattern = sortedMatchInfo.getPattern();
                if (pattern == null) {
                    throw new RuntimeException("未获取到对应的正则");
                }

                List<String> replaceList = replaceDataMap.get(pattern);
                if (CollectionUtils.isEmpty(replaceList)) {
                    continue;
                }

                splitStrList.add(str.substring(0, start));
                splitStrList.add(replaceList.size() <= i ? "" : replaceList.get(i));
                suffix = str = str.substring(end);
                offset = end;
            }
            result.add(String.join("", splitStrList) + suffix);
        }

        return result;
    }

    public static ReplaceInfo parseToReplaceInfo(String templateStr, Map<Pattern, List<String>> replaceDataMap) {
        ReplaceInfo replaceInfo = new ReplaceInfo();
        replaceInfo.setStr(templateStr);
        for (Pattern pattern : replaceDataMap.keySet()) {
            Matcher matcher = pattern.matcher(templateStr);
            while (matcher.find()) {
                MatchInfo matchInfo = new MatchInfo();
                matchInfo.setFrom(matcher.start(1));
                matchInfo.setTo(matcher.end(1));
                matchInfo.setPattern(pattern);
                replaceInfo.addMatchInfo(matchInfo);
            }
            replaceInfo.addReplaceData(pattern, replaceDataMap.get(pattern));
        }
        return replaceInfo;
    }


    @Getter
    @Setter
    public static class ReplaceInfo{

        private String str;

        private final List<MatchInfo> matchInfos = new ArrayList<>();

        private final Map<Pattern, List<String>> replaceDataMap = new HashMap<>();

        public void addMatchInfo(MatchInfo matchInfo) {
            matchInfos.add(matchInfo);
        }

        public void addReplaceData(Pattern pattern, List<String> replacements) {
            replaceDataMap.put(pattern, replacements);
        }

    }

    @Getter
    @Setter
    public static class MatchInfo {

        private int from;

        private int to;

        private Pattern pattern;

    }

}
