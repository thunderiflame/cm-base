package com.github.canglan.cm.auth.server.config;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

import com.github.canglan.cm.auth.common.RsaUtil.RsaKey;
import com.github.canglan.cm.auth.server.handler.CustomAccessDeniedHandler;
import com.github.canglan.cm.auth.server.handler.CustomAuthPoint;
import com.github.canglan.cm.auth.server.model.dto.LoginUser;
import com.github.canglan.cm.auth.server.properties.ClientAuthProperties;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * 配置认证服务
 *
 * @author bairitan
 * @since 2019/11/14
 */
@Slf4j
@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(ClientAuthProperties.class)
@AllArgsConstructor
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

  private RsaConfig rsaConfig;
  private AuthenticationManager authenticationManager;
  private RedisConnectionFactory redisConnectionFactory;
  private CustomAuthPoint customAuthPoint;
  private CustomAccessDeniedHandler customAccessDeniedHandler;
  private HikariDataSource dataSource;
  private PasswordEncoder passwordEncode;
  private UserDetailsService userDetailsService;

  @Override
  public void configure(AuthorizationServerSecurityConfigurer security) {
    // security 配置令牌端点(Token Endpoint)的安全约束.
    // 开放/oauth/token_key
    security.tokenKeyAccess("permitAll()")
        // 开放/oauth/check_token
        // AuthenticationEntryPoint
        .checkTokenAccess("isAuthenticated()")
        .authenticationEntryPoint(customAuthPoint)
        .accessDeniedHandler(customAccessDeniedHandler)
        // 允许表单认证
        .allowFormAuthenticationForClients();
    log.debug(" ================ 配置 AuthorizationServerSecurityConfigurer ==============================");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    log.debug(" ================ 配置 ClientDetailsServiceConfigurer ==============================");
    clients.jdbc(this.dataSource).passwordEncoder(this.passwordEncode);
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    // endpoints 用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)
    endpoints
        .authenticationManager(this.authenticationManager)
        // 设置用户service
        .userDetailsService(this.userDetailsService)
        //  token 转换器
        .accessTokenConverter(accessTokenConverter())
        // 设置token存储
        .tokenStore(tokenStore());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    // RSA非对称加密方式
    RsaKey rsaKey = rsaConfig.getRsaKey();
    RSAPublicKey publicKey = (RSAPublicKey) rsaKey.getPublicKey();
    RSAPrivateKey privateKey = (RSAPrivateKey) rsaKey.getPrivateKey();

    CustomOauth2AccessToken auth2AccessToken = new CustomOauth2AccessToken();
    auth2AccessToken.setSigner(new RsaSigner(privateKey));
    auth2AccessToken.setVerifier(new RsaVerifier(publicKey));
    String verifierKey = String.format("%s%s%s",
        "-----BEGIN PUBLIC KEY-----\n", new String(Base64.encode(publicKey.getEncoded())), "\n-----END PUBLIC KEY-----");

    auth2AccessToken.setVerifierKey(verifierKey);
    return auth2AccessToken;
  }

  @Bean
  public TokenStore tokenStore() {
    return new RedisTokenStore(redisConnectionFactory);
  }

  /**
   * 自定义访问令牌，在访问令牌中添加一些自定义声明
   */
  static class CustomOauth2AccessToken extends JwtAccessTokenConverter {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
      DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);

      log.debug("oauth2token===>{}", authentication);
      log.debug("principal===>{}", authentication.getPrincipal());

      Map<String, Object> info = Maps.newLinkedHashMap(accessToken.getAdditionalInformation());
      String tokenId = result.getValue();
      if (!info.containsKey(TOKEN_ID)) {
        info.put(TOKEN_ID, tokenId);
      }
      info.put("organization", authentication.getName() + randomAlphabetic(4));
      LoginUser user = (LoginUser) authentication.getUserAuthentication().getPrincipal();
      List<String> authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
      info.put("oid", user.getOid());
      info.put("username", user.getUsername());
      info.put("roles", authorities);

      log.debug("info============>{}", info);
      result.setAdditionalInformation(info);
      result.setValue(encode(result, authentication));
      return result;
    }
  }
}
