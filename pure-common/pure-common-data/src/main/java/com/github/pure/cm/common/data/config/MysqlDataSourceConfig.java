package com.github.pure.cm.common.data.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * 事务管理配置<br>
 * 使用两种方法对 DaoImpl 进行事务管理，注解式事务与方法名匹配两种模式<br>
 * 注解式事务优先方法名称匹配模式
 *
 * @author bairitan
 * @since 2018-01-10 21:59
 **/
@Component
@Slf4j
@EnableTransactionManagement
public class MysqlDataSourceConfig {

  // 配置事务管理器开始======================================================

  /**
   * 注册事务拦截器,设置 事务注解匹配方式与方法名称匹配方式进行拦截 <br>
   * 两个匹配器顺序是：注解式事务 > 方法名<br>
   * 事务匹配照旧<br>
   * 方法名称匹配规则如下:<br>
   * save、update、delete是read_write权限,其它方法全是readOnly权限<br>
   *
   * @param transactionManager 事务管理器
   * @param trInterceptor 事务拦截
   * @param annotationMatchSource 事务拦截规则匹配器
   */
  @Bean(name = "defaultTransactionInterceptor")
  public TransactionInterceptor transactionInterceptor(
      @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
      @Qualifier("transactionInterceptor") TransactionInterceptor trInterceptor,
      @Qualifier("transactionAttributeSource") AnnotationTransactionAttributeSource annotationMatchSource) {
    trInterceptor.setTransactionManager(transactionManager);

    // 配置方法名称事务规则匹配器
    Map<String, TransactionAttribute> trMatchConfig = new HashMap<>();

    // 回滚
    RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
    requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
    requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    trMatchConfig.put("save*", requiredTx);
    trMatchConfig.put("create*", requiredTx);
    trMatchConfig.put("insert*", requiredTx);
    trMatchConfig.put("update*", requiredTx);
    trMatchConfig.put("remove*", requiredTx);
    trMatchConfig.put("delete*", requiredTx);
    trMatchConfig.put("*Batch*", requiredTx);

    // 只读
    RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
    readOnlyTx.setReadOnly(true);
    trMatchConfig.put("select*", readOnlyTx);
    trMatchConfig.put("get*", readOnlyTx);
    trMatchConfig.put("list*", readOnlyTx);
    trMatchConfig.put("find*", readOnlyTx);
    // 其他方法名称默认只读，需要声明事务注解或者按照方法名称来达到修改数据
    trMatchConfig.put("*", readOnlyTx);

    NameMatchTransactionAttributeSource nameMatchSource = new NameMatchTransactionAttributeSource();
    nameMatchSource.setNameMap(trMatchConfig);

    // 设置事务管理匹配器,先匹配注解式事务,然后在匹配方法名称
    trInterceptor.setTransactionAttributeSources(annotationMatchSource, nameMatchSource);
    return trInterceptor;
  }

  /**
   * 创建 mysql 事务拦截代理对象
   * 使用 defaultTransactionInterceptor 事务拦截器拦截以 DaoImpl 结尾的类
   */
  @Bean("defaultBeanNameAutoProxyCreator")
  public BeanNameAutoProxyCreator transactionAutoProxy() {
    BeanNameAutoProxyCreator transactionAutoProxy = new BeanNameAutoProxyCreator();
    // 这个属性为true时，表示被代理的是目标类本身而不是目标类的接口
    transactionAutoProxy.setProxyTargetClass(true);
    transactionAutoProxy.setBeanNames("*ServiceImpl");
    transactionAutoProxy.setInterceptorNames("defaultTransactionInterceptor");
    return transactionAutoProxy;
  }
}