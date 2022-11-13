package com.lingyi.RootGet.controller;

import com.lingyi.RootGet.entry.Collection;
import com.lingyi.RootGet.tools.Constant;
import com.lingyi.RootGet.tools.RedisTools;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collection")
public class CollectionController {
    private final RedisTools redisTools;
    public CollectionController(RedisTools redisTools){
        this.redisTools = redisTools;
    }
    @GetMapping("/getCollectionByUid/{uid}")
    public List<Collection> getCollection(@PathVariable("uid")long uid){
        return redisTools.getCollection(uid);
    }
    @PostMapping("/addToCollection")
    public String addToCollection(@RequestBody Map<String,String> body,HttpServletRequest request){
        long id = Long.parseLong(body.get("id"));
        String name = body.get("name");
        long videoId = Long.parseLong(body.get("videoId"));
        String uidInCookie = Constant.getUidInCookie(request);
        if(uidInCookie==null||id!=Long.parseLong(uidInCookie)) return Constant.NoPermission;
        redisTools.addToCollection(id,videoId,name);
        return Constant.SUCCESS;
    }
    @PostMapping("/removeFromCollection")
    public String removeFromCollection(@RequestBody Map<String,String> body, HttpServletRequest request){
        long id = Long.parseLong(body.get("id"));
        String name = body.get("name");
        long videoId = Long.parseLong(body.get("videoId"));
        String uidInCookie = Constant.getUidInCookie(request);
        if(uidInCookie==null||id!=Long.parseLong(uidInCookie)) return Constant.NoPermission;
        redisTools.removeFromCollection(id,videoId,name);
        return Constant.SUCCESS;
    }
    @PostMapping("/renameCollection")
    public String rename(@RequestBody Map<String,String> body, HttpServletRequest request){
        long id = Long.parseLong(body.get("id"));
        String name = body.get("name");
        String newName = body.get("newName");
        if(name.equals(newName)) return Constant.KeyExists;
                String uidInCookie = Constant.getUidInCookie(request);
        if(uidInCookie==null||id!=Long.parseLong(uidInCookie)) return Constant.NoPermission;
        Boolean hasKey = redisTools.getRedisTemplate().hasKey("VideoStation:Collection:"+id+ ":"+ newName);
        if(hasKey) return Constant.KeyExists;
        redisTools.renameKey("VideoStation:Collection:"+id+":"+name,"VideoStation:Collection:"+id+":"+newName);
        return Constant.SUCCESS;
    }
}
