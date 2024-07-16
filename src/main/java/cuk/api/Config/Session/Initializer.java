package cuk.api.Config.Session;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;

public class Initializer extends AbstractHttpSessionApplicationInitializer {
}