package com.github.canglan.cm.common.core.callback;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.WebFluxCallbackManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author bairitan
 * @date 2020/1/8
 */
@Configuration
@Slf4j
public class SentinelConfig {

  public SentinelConfig() {
    log.debug("config CustomBlockRequestHandler ====================");
    WebFluxCallbackManager.setBlockHandler(new CustomBlockRequestHandler());
  }
}
