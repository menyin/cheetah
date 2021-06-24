package com.caisheng.cheetah.common.condition;

import com.caisheng.cheetah.api.common.Condition;

import java.util.Map;
import java.util.Set;

public class TagsCondition implements Condition {
    private Set<String> tagsList;
    public TagsCondition(Set<String> tags) {
        this.tagsList=tags;
    }

    public boolean test(Map<String,Object> env) {
        String tags = (String) env.get("tags");
        return tags != null && this.tagsList.stream().anyMatch(tags::contains);
    }
}
