package org.jetlinks.community.standalone.web;

import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jetlinks-auth")
public class JetLinksAuthController {
   @Autowired
    UserTokenManager userTokenManager;

   @Autowired
    UserDetailService  userDetailService;


    /**
     * Token接口
     */
    @PostMapping("/signin")
    public  Mono<UserToken> refreshToken(@RequestHeader("userid") String userid,@RequestHeader("X-Refresh-Token") String refreshToken) {

      Mono<UserToken> tokenMono=   userTokenManager.signIn(refreshToken, "default", userid, 60*60*60*1000);
     return  tokenMono;
    }
    @GetMapping("/token")
    public  Flux<UserToken> queryToken(@RequestHeader("userid") String userid) {
             return userTokenManager.getByUserId(userid);
    }

    @GetMapping("/queryUser")
    public  Mono<PagerResult<UserDetail>> queryUser(@RequestHeader("username") String username) {
        QueryParamEntity queryParamEntity = new QueryParamEntity();
        queryParamEntity.setWhere("username="+username);
      return userDetailService.queryUserDetail(queryParamEntity);
    }


}

