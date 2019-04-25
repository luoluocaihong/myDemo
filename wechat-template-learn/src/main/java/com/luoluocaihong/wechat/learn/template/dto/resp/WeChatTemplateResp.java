package com.luoluocaihong.wechat.learn.template.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xh on 2019/4/25.
 */
@Data
public class WeChatTemplateResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "微信模板ID")
    private String templateId;
    @ApiModelProperty(value = "微信模板标题")
    private String title;
    @ApiModelProperty(value = "主行业")
    private String primaryIndustry;
    @ApiModelProperty(value = "副行业")
    private String deputyIndustry;
    @ApiModelProperty(value = "微信模板内容")
    private String content;
    @ApiModelProperty(value = "微信模板样例")
    private String example;
}
