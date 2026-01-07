//package cuk.api.Config.Session;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.orm.hibernate5.HibernateTransactionManager;
//import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
//import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableJdbcHttpSession
//public class JDBCSessionConfig {
//    private final DataSource dataSource;
//    private final HibernateTransactionManager myTransactionManager;
//
//    @Autowired
//    public JDBCSessionConfig(DataSource dataSource, HibernateTransactionManager myTransactionManager) {
//        this.dataSource = dataSource;
//        this.myTransactionManager = myTransactionManager;
//    }
//
//    @Bean
//    public JdbcIndexedSessionRepository jdbcIndexedSessionRepository() {
//        JdbcIndexedSessionRepository repository = new JdbcIndexedSessionRepository(jdbcTemplate(), transactionTemplate());
//        repository.setDefaultMaxInactiveInterval(1800); // 세션 만료 시간 설정
//        return repository;
//    }
//
//    @Bean
//    public TransactionTemplate transactionTemplate() {
//        return new TransactionTemplate(myTransactionManager);
//    }
//
//    // 필요한 경우 JdbcTemplate 등 다른 빈을 설정할 수 있습니다.
//    @Bean
//    public JdbcTemplate jdbcTemplate() {
//        return new JdbcTemplate(dataSource);
//    }
//}
