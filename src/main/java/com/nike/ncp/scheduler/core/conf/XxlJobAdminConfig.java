package com.nike.ncp.scheduler.core.conf;

import com.nike.ncp.scheduler.core.scheduler.XxlJobScheduler;
import com.nike.ncp.scheduler.core.alarm.JobAlarmer;
import com.nike.ncp.scheduler.dao.XxlJobGroupDao;
import com.nike.ncp.scheduler.dao.XxlJobInfoDao;
import com.nike.ncp.scheduler.dao.XxlJobLogDao;
import com.nike.ncp.scheduler.dao.XxlJobRegistryDao;
import com.nike.ncp.scheduler.dao.XxlJobLogReportDao;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;

/**
 * xxl-job config
 */

@Component
public class XxlJobAdminConfig implements InitializingBean, DisposableBean {

    private static XxlJobAdminConfig adminConfig = null;
    public static XxlJobAdminConfig getAdminConfig() {
        return adminConfig;
    }


    // ---------------------- XxlJobScheduler ----------------------

    private transient XxlJobScheduler xxlJobScheduler;

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;

        xxlJobScheduler = new XxlJobScheduler();
        xxlJobScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        xxlJobScheduler.destroy();
    }


    // ---------------------- XxlJobScheduler ----------------------

    // conf
    @Value("${xxl.job.i18n}")
    private transient String i18n;

    @Value("${xxl.job.accessToken}")
    private transient String accessToken;

    @Value("${spring.mail.from}")
    private transient String emailFrom;

    @Value("${xxl.job.triggerpool.fast.max}")
    private transient int triggerPoolFastMax;

    @Value("${xxl.job.triggerpool.slow.max}")
    private transient int triggerPoolSlowMax;

    @Value("${xxl.job.logretentiondays}")
    private transient int logretentiondays;

    // dao, service

    @Resource
    private transient XxlJobLogDao xxlJobLogDao;
    @Resource
    private transient XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private transient XxlJobRegistryDao xxlJobRegistryDao;
    @Resource
    private transient XxlJobGroupDao xxlJobGroupDao;
    @Resource
    private transient XxlJobLogReportDao xxlJobLogReportDao;
    @Resource
    private transient JavaMailSender mailSender;
    @Resource
    private transient DataSource dataSource;
    @Resource
    private transient JobAlarmer jobAlarmer;

    private static final int TRIGGER_POOL_FAST_MAX_V = 200;

    private static final int TRIGGER_POOL_SLOW_MAX_V = 100;

    private static final int LOG_RETENTION_DAYS_V = 7;


    public String getI18n() {
        if (!Arrays.asList("zh_CN", "zh_TC", "en").contains(i18n)) {
            return "zh_CN";
        }
        return i18n;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public int getTriggerPoolFastMax() {
        if (triggerPoolFastMax < TRIGGER_POOL_FAST_MAX_V) {
            return 200;
        }
        return triggerPoolFastMax;
    }

    public int getTriggerPoolSlowMax() {
        if (triggerPoolSlowMax < TRIGGER_POOL_SLOW_MAX_V) {
            return 100;
        }
        return triggerPoolSlowMax;
    }

    public int getLogretentiondays() {
        if (logretentiondays < LOG_RETENTION_DAYS_V) {
            return -1;  // Limit greater than or equal to 7, otherwise close
        }
        return logretentiondays;
    }

    public XxlJobLogDao getXxlJobLogDao() {
        return xxlJobLogDao;
    }

    public XxlJobInfoDao getXxlJobInfoDao() {
        return xxlJobInfoDao;
    }

    public XxlJobRegistryDao getXxlJobRegistryDao() {
        return xxlJobRegistryDao;
    }

    public XxlJobGroupDao getXxlJobGroupDao() {
        return xxlJobGroupDao;
    }

    public XxlJobLogReportDao getXxlJobLogReportDao() {
        return xxlJobLogReportDao;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JobAlarmer getJobAlarmer() {
        return jobAlarmer;
    }

}
