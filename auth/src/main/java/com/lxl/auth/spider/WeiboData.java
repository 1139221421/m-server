package com.lxl.auth.spider;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "weibo_data")
public class WeiboData {

    private Long id;
    private Integer textLength;
    private String repostsCount;
    private Integer commentsCount;
    private Integer attitudesCount;
    private Long expireTime;
    private String rewardScheme;
    private String text;
    private String source;
    private String recommendSource;
    private String rawText;
    private WeiboUser user;
}

@Data
class WeiboUser {
    private Long id;
    private String screenName;
    private String profileImageUrl;
    private String profileUrl;
    private String verifiedReason;
    private String coverImagePhone;
    private String avatarHd;
}
