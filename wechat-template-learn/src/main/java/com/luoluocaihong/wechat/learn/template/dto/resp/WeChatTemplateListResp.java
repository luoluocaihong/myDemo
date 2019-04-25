package com.luoluocaihong.wechat.learn.template.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xh on 2019/4/25.
 */
@Data
public class WeChatTemplateListResp implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<WeChatTemplateResp> templateList;
}
