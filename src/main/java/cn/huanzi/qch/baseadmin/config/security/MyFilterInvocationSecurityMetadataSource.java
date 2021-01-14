package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.sys.sysauthority.vo.SysAuthorityVo;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置认证数据源，实现动态权限加载（注意：不要手动new，把它交给spring管理，spring默认单例）
 */
@Component
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    //权限数据
    private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap;

    // 仅仅需要登录的url
    private List<RequestMatcher> onlyLoginList = new ArrayList<>();


    /**
     * 在我们初始化的权限数据中找到对应当前url的权限数据
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        HttpServletRequest request = fi.getRequest();

        // 是否是仅仅需要登录的url
        for (RequestMatcher requestMatcher : onlyLoginList) {
            if (requestMatcher.matches(request)) {
                return null;
            }
        }

        //遍历我们初始化的权限数据，找到对应的url对应的权限
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap
                .entrySet()) {
            if (entry.getKey().matches(request)) {
                return entry.getValue();
            }
        }
        ArrayList<ConfigAttribute> configs = new ArrayList<>();
        configs.add(new SecurityConfig("ROLE_NOT_HAVE"));
        return configs;
//        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    public void setOnlyLoginMap(String... onlyLoginUrls) {
        for (String onlyLoginUrl : onlyLoginUrls) {
            onlyLoginList.add(new AntPathRequestMatcher(onlyLoginUrl));
        }
    }

    /**
     * 更新权限集合
     */
    public void setRequestMap(List<SysAuthorityVo> authorityVoList) {
        Map<RequestMatcher, Collection<ConfigAttribute>> map = new ConcurrentHashMap<>();
        for (SysAuthorityVo sysAuthorityVo : authorityVoList) {
            String authorityName = sysAuthorityVo.getAuthorityName();
            if (StringUtils.isEmpty(sysAuthorityVo.getAuthorityContent())) continue;
            for (String url : sysAuthorityVo.getAuthorityContent().split(",")) {
                Collection<ConfigAttribute> value = map.get(new AntPathRequestMatcher(url));
                if (StringUtils.isEmpty(value)) {
                    ArrayList<ConfigAttribute> configs = new ArrayList<>();
                    configs.add(new SecurityConfig(authorityName));
                    map.put(new AntPathRequestMatcher(url), configs);
                } else {
                    value.add(new SecurityConfig(authorityName));
                }
            }
        }
        this.requestMap = map;
    }
}
