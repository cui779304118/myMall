package cup.cw.mall.common;

import java.io.FileWriter;
import java.io.IOException;

/**
 * created by cuiwei on 2018/11/29
 */
public class AlipayConfig {
    //↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public static String app_id = "2016092000554262";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC/RdecKECH3MBQuEiV3dOv8hdEmoSIxtmV6QmWoKg3+Bvfl6QRx5EgEOw9bMEoKqwxV9drnfnfsbx1AYSYNNr+Jo6dfZtuvY4OEPxyUk484153qrJMgBJa0wI3cbC+EKTtgldBm6TwNlXjMP/gC6UfUHV7JnNrqlDaikqX1OGsksTeYeUIuHJjxcIkU/xOtdD5SJA0wTNlQvA+DdaNZ1h2PtasRRoNYw+9uxcOtYD2GtdUwe+OQqP9TFXvo8lwNXvEXB6JNTXLhDibTNp2UnTut/ywN7yam+e4WTy8rLQVFY75qcH4jxgnd00T9CJDazWz+GDexpd/mGC+FAoZcbntAgMBAAECggEAL113QAdDu6ExyotoBhyxgB9+HZH6AfjkBth3dGKCZaG+HUdq7LAzwvIN3kQhLPYsP+fVw6OSLg/QcFUY5awpEsivhlaFeA3esKMKMLErCK7xRMbzcjhA01bvdJVlxBtRDDO7sdZ+KLH74M/QfUXE9RUyknV6HY5GiWe3gxPHcrlT7v5B9PlJhnlZORjUeToQrYVBKroYuMuvB2RKMSQGwvaniFPHlKwX+9O8jPPtq2/nEbfQVuOdLR0l/nRb9gtULrA2/Vao7Abn0ZSXUutEO5CW5eneOfQTxC82f1uEziXlG2fTPiaYpuzabHhiP+xPOPj43MxBbPwdnXHiE/NawQKBgQDm/3lYsEqNnxIYQfXjEVieffO3CgAmBYU0LZPtgdebS+RZ/06TGGYMCsDswaqWIcwV08TYgsbJ4fwidrQcKNJle1cvX8Z2n7TQ0dOTaN/4NGl0/Z9qO5cJBphri6DOFSdgzpIv/RxZ58Ege10JB3D22LG6jGjP4fRevutkEhvVkQKBgQDT+aiRmDdmzJM8j9H3k5vqw0tni6arr5V7R0VXZRey1V8Bow1Gat/SX9XtBeCl+BMW+YFvVoG8F7YuBVJCXtrDN8WTAAPXN0vp8sOjh19p7XMaa+4dl+lxY/yPJtMTvAbtFECGk+yqUVriGuNvOwWyAQVIrHuO1y0v0+nsBYPAnQKBgQDUSCT5BxGMconLdxqDsNYy2ObW6D7YsiwGlngiRTehxK22szQ/Y6WsHVnMefHNhIOr73Av1Ibg0mfGODYuxqkLj7zAkZCuRPiDCsEX78Ibs/bSB52vn9Gd/syl0KxXlwMYKN4Zcc7TJHLL6fLGsGr0vpEeMZjEqg7L30ItNaMUsQKBgEBmnl81lsdlqeoBa/JC52Do2pTn6chJfSnKGngV36BWkRnHNvZI7B1XPoBXg8wI1YvqW/e+mYXS0mei+D3v4Zjj7ePeYkxVoXu/ZM7fBw8AtDK3zc0x5RutHANF8WUSgCwxNfVtJb8NHoaJUsXtcaqGAtv0g0l/eFwKjwxFFKVFAoGAWt+Mye4aWqFl89xF7koM0/igP1u+ygxOZMrL8e4AwJnBwbVuWK7HkKHugUVLzUnK4itvxP5tP6hk1FM6SdVzOOG2Wf6+uVntNP/7K3qYmRMmlRS/dIG9RGhq0fRloOhwDVgUWUBqAL/UN2MuQB67P+vDh0hSvrhp1+Pq7Wp6Qv8=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2Vp/dkVYyFyoDlAZs8xQvK6WdbFQXKJncRl0zE7TaIXmXHlDo86xQolAoQvGvSL4GPe933f+gBxFP6/rF3GiWGsmq7E+tMfaSYDr7XI0+cWkn2viXspJPKGkTd6Qoyrd6VsZh2LWveZMszTPUxZoM9fP0UIvtc10FBIQdmAkHHAKxmBMJPz0AbIwvP3Q3AiucwMfbtrNItF8Bedys+y8F/KOaTRECxFwQQvPXTN3U/Lnc5LOOr6UQzFQCaa0a5kwhOH1xRZvamx4uetO/JJY5wtQT7wx/KKTtfJ67P31T/F+a9/HpTv3jnkd1dMWcTQF5JSmMdRh/S9tdIwKFR8V/QIDAQAB";

    // 服务器异步通知页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://localhost:8080/notify_url.jsp";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    public static String return_url = "http://localhost/return_url.jsp";

    // 签名方式
    public static String sign_type = "RSA2";

    // 字符编码格式
    public static String charset = "utf-8";

    // 支付宝网关
    public static String gatewayUrl = "https://openapi.alipay.com/gateway.do";

    // 支付宝网关
    public static String log_path = "C:\\";


//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    /**
     * 写日志，方便测试（看网站需求，也可以改成把记录存入数据库）
     * @param sWord 要写入日志里的文本内容
     */
    public static void logResult(String sWord) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(log_path + "alipay_log_" + System.currentTimeMillis()+".txt");
            writer.write(sWord);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
