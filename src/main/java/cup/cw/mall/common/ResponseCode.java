package cup.cw.mall.common;

/**
 * created by cuiwei on 2018/10/14
 * 响应状态码
 */
public enum ResponseCode {
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return this.code;
    }

    public String getDesc(){
        return this.desc;
    }

    public static String getDescByCode(int code){
        for (ResponseCode responseCode : ResponseCode.values()){
            if (responseCode.getCode() == code){
                return responseCode.getDesc();
            }
        }
        return null;
    }
}
