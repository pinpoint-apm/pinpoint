package com.navercorp.pinpoint.web.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by yuanxiaozhong on 2018/3/28.
 */
public class TransportEmail {
    private static final Logger logger = LoggerFactory.getLogger(TransportEmail.class);

    private static String defaultSenderName = "";// 默认的发件人用户名，defaultEntity用得到
    private static String defaultSenderPass = "";// 默认的发件人密码，defaultEntity用得到
    private static String defaultSmtpHost = "";// 默认的邮件服务器地址，defaultEntity用得到

    private String smtpHost; // 邮件服务器地址
    private String sendUserName; // 发件人的用户名
    private String sendUserPass; // 发件人密码

    private MimeMessage mimeMsg; // 邮件对象
    private Session session;
    private Properties props;
    private Multipart mp;// 附件添加的组件
    private List<FileDataSource> files = new LinkedList<FileDataSource>();// 存放附件文件

    private void init() {
        if (props == null) {
            props = System.getProperties();
        }
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true"); // 需要身份验证
        session = Session.getDefaultInstance(props, null);
        // 置true可以在控制台（console)上看到发送邮件的过程
        session.setDebug(true);
        // 用session对象来创建并初始化邮件对象
        mimeMsg = new MimeMessage(session);
        // 生成附件组件的实例
        mp = new MimeMultipart();
    }

    private TransportEmail(String smtpHost, String sendUserName, String sendUserPass, String to, String cc, String mailSubject, String mailBody,
                           List<String> attachments) {
        this.smtpHost = smtpHost;
        this.sendUserName = sendUserName;
        this.sendUserPass = sendUserPass;

        init();
        setFrom(sendUserName);
        setTo(to);
        setCC(cc);
        setBody(mailBody);
        setSubject(mailSubject);
        if (attachments != null) {
            for (String attachment : attachments) {
                addFileAffix(attachment);
            }
        }

    }

    /**
     * 邮件实体
     *
     * @param smtpHost
     *            邮件服务器地址
     * @param sendUserName
     *            发件邮件地址
     * @param sendUserPass
     *            发件邮箱密码
     * @param to
     *            收件人，多个邮箱地址以半角逗号分隔
     * @param cc
     *            抄送，多个邮箱地址以半角逗号分隔
     * @param mailSubject
     *            邮件主题
     * @param mailBody
     *            邮件正文
     * @return
     */
    public static TransportEmail entity(String smtpHost, String sendUserName, String sendUserPass, String to, String cc, String mailSubject, String mailBody,
                                        List<String> attachments) {
        return new TransportEmail(smtpHost, sendUserName, sendUserPass, to, cc, mailSubject, mailBody, attachments);
    }

    /**
     * 默认邮件实体，用了默认的发送帐号和邮件服务器
     *
     * @param to
     *            收件人，多个邮箱地址以半角逗号分隔
     * @param cc
     *            抄送，多个邮箱地址以半角逗号分隔
     * @param subject
     *            邮件主题
     * @param body
     *            邮件正文
     * @return
     */
    public static TransportEmail defaultEntity(String to, String cc, String subject, String body, List<String> attachments) {
        return new TransportEmail(defaultSmtpHost, defaultSenderName, defaultSenderPass, to, cc, subject, body, attachments);
    }

    /**
     * 设置邮件主题
     *
     * @param mailSubject
     * @return
     */
    private boolean setSubject(String mailSubject) {
        try {
            mimeMsg.setSubject(mailSubject);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 设置邮件内容,并设置其为文本格式或HTML文件格式，编码方式为UTF-8
     *
     * @param mailBody
     * @return
     */
    private boolean setBody(String mailBody) {
        try {
            BodyPart bp = new MimeBodyPart();
            bp.setContent("<meta http-equiv=Content-Type content=text/html; charset=UTF-8>" + mailBody, "text/html;charset=UTF-8");
            // 在组件上添加邮件文本
            mp.addBodyPart(bp);
        } catch (Exception e) {
            System.err.println("设置邮件正文时发生错误！" + e);
            return false;
        }
        return true;
    }

    /**
     * 添加一个附件
     *
     * @param filename
     *            邮件附件的地址，只能是本机地址而不能是网络地址，否则抛出异常
     * @return
     */
    public boolean addFileAffix(String filename) {
        try {
            if (filename != null && filename.length() > 0) {
                BodyPart bp = new MimeBodyPart();
                FileDataSource fileds = new FileDataSource(filename);
                bp.setDataHandler(new DataHandler(fileds));
                bp.setFileName(MimeUtility.encodeText(fileds.getName(), "utf-8", null)); // 解决附件名称乱码
                mp.addBodyPart(bp);// 添加附件
                files.add(fileds);
            }
        } catch (Exception e) {
            System.err.println("增加邮件附件：" + filename + "发生错误！" + e);
            return false;
        }
        return true;
    }

    /**
     * 删除所有附件
     *
     * @return
     */
    public boolean delFileAffix() {
        try {
            FileDataSource fileds = null;
            for (Iterator<FileDataSource> it = files.iterator(); it.hasNext();) {
                fileds = it.next();
                if (fileds != null && fileds.getFile() != null) {
                    fileds.getFile().delete();
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 设置发件人地址
     *
     * @param from
     *            发件人地址
     * @return
     */
    private boolean setFrom(String from) {
        try {
            mimeMsg.setFrom(new InternetAddress(from));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 设置收件人地址
     *
     * @param to 收件人的地址
     * @return
     */
    private boolean setTo(String to) {
        if (to == null)
            return false;
        try {
            mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 设置抄送
     *
     * @param cc
     * @return
     */
    private boolean setCC(String cc) {
        if (cc == null) {
            return false;
        }
        try {
            mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 发送邮件
     *
     * @return
     */
    public boolean send() throws Exception {
        mimeMsg.setContent(mp);
        mimeMsg.saveChanges();
        logger.info("正在发送邮件....");
        Transport transport = session.getTransport("smtp");
        // 连接邮件服务器并进行身份验证
        transport.connect(smtpHost, sendUserName, sendUserPass);
        // 发送邮件
        transport.sendMessage(mimeMsg, mimeMsg.getRecipients(Message.RecipientType.TO));
        logger.info("发送邮件成功！");
        transport.close();
        return true;
    }
}
