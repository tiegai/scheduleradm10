package com.nike.ncp.scheduler.common.handler.impl;

import com.nike.ncp.scheduler.common.context.XxlJobContext;
import com.nike.ncp.scheduler.common.handler.IJobHandler;
import com.nike.ncp.scheduler.common.log.XxlJobFileAppender;
import com.nike.ncp.scheduler.common.context.XxlJobHelper;
import com.nike.ncp.scheduler.common.glue.GlueTypeEnum;
import com.nike.ncp.scheduler.common.util.ScriptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class ScriptJobHandler extends IJobHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptJobHandler.class);

    private transient int jobId;
    private transient long glueUpdatetime;
    private transient String gluesource;
    private transient GlueTypeEnum glueType;

    public ScriptJobHandler(int jobId, long glueUpdatetime, String gluesource, GlueTypeEnum glueType) {
        this.jobId = jobId;
        this.glueUpdatetime = glueUpdatetime;
        this.gluesource = gluesource;
        this.glueType = glueType;

        // clean old script file
        File glueSrcPath = new File(XxlJobFileAppender.getGlueSrcPath());
        if (glueSrcPath.exists()) {
            File[] glueSrcFileList = glueSrcPath.listFiles();
            if (glueSrcFileList != null && glueSrcFileList.length > 0) {
                for (File glueSrcFileItem : glueSrcFileList) {
                    if (glueSrcFileItem.getName().startsWith(String.valueOf(jobId) + "_")) {
                        try {
                            glueSrcFileItem.delete();
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    @SuppressWarnings("all")
    public void execute() throws Exception {

        if (!glueType.isScript()) {
            XxlJobHelper.handleFail("glueType[" + glueType + "] invalid.");
            return;
        }

        // cmd
        String cmd = glueType.getCmd();

        // make script file
        String scriptFileName = XxlJobFileAppender.getGlueSrcPath().concat(File.separator).concat(String.valueOf(jobId)).concat("_").concat(String.valueOf(glueUpdatetime)).concat(glueType.getSuffix());
        File scriptFile = new File(scriptFileName);
        if (!scriptFile.exists()) {
            ScriptUtil.markScriptFile(scriptFileName, gluesource);
        }

        // log file
        String logFileName = XxlJobContext.getXxlJobContext().getJobLogFileName();

        // script params：0=param、1=分片序号、2=分片总数
        String[] scriptParams = new String[3];
        scriptParams[0] = XxlJobHelper.getJobParam();
        scriptParams[1] = String.valueOf(XxlJobContext.getXxlJobContext().getShardIndex());
        scriptParams[2] = String.valueOf(XxlJobContext.getXxlJobContext().getShardTotal());

        // invoke
        XxlJobHelper.log("----------- script file:" + scriptFileName + " -----------");
        int exitValue = ScriptUtil.execToFile(cmd, scriptFileName, logFileName, scriptParams);

        if (exitValue == 0) {
            XxlJobHelper.handleSuccess();
            return;
        } else {
            XxlJobHelper.handleFail("script exit value(" + exitValue + ") is failed");
            return;
        }

    }

}
