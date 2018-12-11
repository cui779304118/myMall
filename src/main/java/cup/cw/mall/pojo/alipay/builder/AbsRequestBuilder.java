package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.JSONObject;

/**
 * created by cuiwei on 2018/11/30
 *  抽象请求builder，包含公共请求参数
 */
public abstract class AbsRequestBuilder {

    public AbsRequestBuilder(){}

    public abstract boolean validate();

    public abstract Object getBizContent();

    public String toJsonString(){
        return JSONObject.toJSONString(this.getBizContent());
    }

}
