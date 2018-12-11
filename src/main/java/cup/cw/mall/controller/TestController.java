package cup.cw.mall.controller;

import com.alibaba.fastjson.JSONObject;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.UserMapper;
import cup.cw.mall.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

/**
 * created by cuiwei on 2018/10/13
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private UserMapper userMapper;

    private static final Set<String> typeSet = new HashSet<String>(){
        {
            add("user");
        }
    };
    @RequestMapping("/test.do")
    public JSONObject test(){
        JSONObject ret = new JSONObject();
        ret.put("message","test!");
        return ret;
    }

    @RequestMapping(value = "/insert/{type}/{param:.+}.do",method = RequestMethod.GET)
    public JSONObject insert(@PathVariable("type") String type,@PathVariable("param") String param){
        JSONObject ret = new JSONObject();
        if (!typeSet.contains(type.trim())) {
            ret.put("message","没有该type：" + type);
            return ret;
        }
        if (StringUtils.isEmpty(param)){
            ret.put("message","param为空！");
            return ret;
        }
        String[] params = param.split("-");
        switch (type){
            case "user":
                User user = new User();
                user.setUsername(params[0]);
                user.setPassword(params[1]);
                user.setRole(Byte.valueOf(params[2]));
                user.setEmail(params[3]);
                user.setPhone(params[4]);
                user.setAnswer(params[5]);
                user.setQuestion(params[6]);
                userMapper.insert(user);
                ret.put("message","插入成功！");
                ret.put("data",user.toString());
                break;
        }
        return ret;
    }

    @RequestMapping(value = "/query/{type}/{id}.do",method = RequestMethod.GET)
    public ServerResponse<User> query(@PathVariable("type") String type, @PathVariable("id") String param){
        if (!typeSet.contains(type.trim())) {
            return ServerResponse.createByError("没有该type: " + type);
        }
        if (StringUtils.isEmpty(param)){
            return ServerResponse.createByError("param 为空！");
        }
        User user = null;
        switch (type){
            case "user":
                user = userMapper.selectByPrimaryKey(Integer.valueOf(param));
                break;
        }
        return ServerResponse.createBySuccess("查询成功！",user);
    }

    @RequestMapping(value = "/update/{type}/{param}.do",method = RequestMethod.GET)
    public JSONObject update(@PathVariable("type") String type,@PathVariable("param") String param){
        JSONObject ret = new JSONObject();
        if (!typeSet.contains(type.trim())) {
            ret.put("message","没有该type：" + type);
            return ret;
        }
        if (StringUtils.isEmpty(param)){
            ret.put("message","param为空！");
            return ret;
        }
        String[] params = param.split("-");
        switch (type){
            case "user":
                User userSel = new User();
                userSel.setId(Integer.valueOf(params[0]));
                userSel.setUsername(params[1]);
                userMapper.updateByUserNameSelective(userSel);
                ret.put("message","更新成功！");
                ret.put("data",userSel.toString());
                break;
        }
        return ret;
    }

    @RequestMapping(value = "/delete/{type}/{id}.do",method = RequestMethod.GET)
    public JSONObject delete(@PathVariable("type") String type,@PathVariable("id") String param){
        JSONObject ret = new JSONObject();
        if (!typeSet.contains(type.trim())) {
            ret.put("message","没有该type：" + type);
            return ret;
        }
        if (StringUtils.isEmpty(param)){
            ret.put("message","param为空！");
            return ret;
        }
        switch (type){
            case "user":
                userMapper.deleteByPrimaryKey(Integer.valueOf(param));
                ret.put("message","删除成功！");
                break;
        }
        return ret;
    }

}
