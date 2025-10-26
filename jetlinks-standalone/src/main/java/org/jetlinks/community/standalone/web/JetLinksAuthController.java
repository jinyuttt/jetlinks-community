package org.jetlinks.community.standalone.web;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.crud.query.QueryHelper;
import org.hswebframework.web.system.authorization.api.PasswordEncoder;
import org.jetlinks.community.auth.entity.UserDetail;
import org.jetlinks.community.auth.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 内部注册
 */
@RestController
@RequestMapping("/api/jetlinks-auth")
public class JetLinksAuthController {
   @Autowired
    UserTokenManager userTokenManager;

   @Autowired
    UserDetailService  userDetailService;

@Autowired
    QueryHelper queryHelper;

@Autowired
    TimestampTokenService timestampTokenService;
    /**
     * Token接口
     */
    @PostMapping("/signin")
    public  Mono<UserToken> refreshToken(@RequestHeader("userid") String userid,@RequestHeader("X-Refresh-Token") String refreshToken) {

        Mono<UserToken> tokenMono = userTokenManager.signIn(refreshToken, "default", userid, 60 * 60 * 60 * 1000);
        return tokenMono;
    }
    @GetMapping("/token")
    public  Flux<UserToken> queryToken(@RequestHeader("userid") String userid) {
             return userTokenManager.getByUserId(userid);
    }

    @GetMapping("/queryUser")
    public  Mono<PagerResult<UserDetail>> queryUser(@RequestHeader("username") String username) {
        QueryParamEntity query = QueryParamEntity.newQuery()
                                                 .and("username", username).getParam();
      return userDetailService.queryUserDetail(query);
    }

    @GetMapping("/login")
    public  Mono<String> queryUser(@RequestHeader("username") String username,@RequestHeader("password") String password) {
        if (password == null || password.equals("")) {
            return Mono.empty();
        }

        QueryParamEntity query = QueryParamEntity.newQuery()
                                                 .and("username", username).getParam();

        Flux<UserDetails> detailsFlux = queryHelper
            .select("select id,username,password,salt from s_user", UserDetails::new)
            .where(query)
            .fetch();
       Mono<String> ret= detailsFlux.last().flatMap(p -> {
                                       String hexHash = convert(password, p.getSalt());
           String token ="";
                                       if (p.getPassword().equals(hexHash)) {
                                            token = timestampTokenService.generateTimestampToken();
                                           this.refreshToken(p.getId(), token);
                                       }
                                       return Mono.just(hexHash);
                                   }
        );
       return  ret;
    }




    private  String convert(String passwords,String salts) {
        PasswordEncoder passwordEncoder = (password, salt) -> DigestUtils.md5Hex(String.format("hsweb.%s.framework.%s", password, salt));

        return passwordEncoder.encode(passwords, salts);

    }
}

