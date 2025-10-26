package cn.iot.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    // 授权服务器安全配置
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        return http

            .formLogin(Customizer.withDefaults())

// 添加额外的安全配置

            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))

            .exceptionHandling(exceptions -> exceptions

                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))

            )

            .build();
    }

    // 默认安全配置
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                                       authorize
                                           .requestMatchers("/actuator/health", "/public/**").permitAll()

                                           .requestMatchers("/api/public/**").permitAll() // 匹配 /api/public/ 开头的所有请求
                                           .requestMatchers("/auth/login", "/auth/register").permitAll() // 匹配特定登录注册接口
                                           .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // 允许Swagger文
                                           .requestMatchers("/swagger-resources/**").permitAll()
                                           .requestMatchers("/swagger-ui.html").permitAll()
                                           .requestMatchers("/v2/api-docs").permitAll()
                                           .requestMatchers("/api/jetlinks-auth/**").permitAll()
                                           .anyRequest().authenticated()
            )
            .formLogin(form ->
                           form
                               .loginPage("/login")
                               .permitAll()
            )
            .logout(logout ->
                        logout
                            .logoutSuccessUrl("/login?logout")
                            .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/oauth2/**")); // 根据需求调整

        return http.build();
    }

    // 客户端配置 - 只保留这一个
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                                            .clientId("iot-client")
                                                            .clientSecret("{noop}iot-secret")
                                                            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                                            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                                                            .authorizationGrantType(AuthorizationGrantType.PASSWORD) // 如果需要密码模式
                                                            .redirectUri("http://127.0.0.1:8080/login/oauth2/code/iot-client")
                                                            .redirectUri("http://localhost:8080/authorized")
                                                            .redirectUri("https://oauth.pstmn.io/v1/callback")
                                                            .scope(OidcScopes.OPENID)
                                                            .scope(OidcScopes.PROFILE)
                                                            .scope("read")
                                                            .scope("write")

                                                            .clientSettings(ClientSettings.builder()
                                                                                          .requireAuthorizationConsent(false)
                                                                                          .requireProofKey(false) // PKCE 配置
                                                                                          .build())
                                                            .tokenSettings(TokenSettings.builder()
                                                                                        .accessTokenTimeToLive(Duration.ofHours(1))
                                                                                        .refreshTokenTimeToLive(Duration.ofDays(7))
                                                                                        .reuseRefreshTokens(false)
                                                                                        .build())
                                                            .build();



        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    // JWT 密钥配置
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    // JWT 解码器
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // 授权服务器设置
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .build();
    }

    // 用户详情服务
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();

        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    // RSA 密钥生成
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}