package com.example.typing.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * メール送信サービス。
 * Thymeleaf テンプレートを使った HTML メールの送信機能を提供する。
 */
@Service
public class SendMailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    public SendMailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Thymeleaf テンプレートを使って HTML メールを送信する。
     *
     * @param to           送信先メールアドレス
     * @param subject      メールの件名
     * @param templateName {@code templates/mail/} 配下のテンプレートファイル名（拡張子なし）
     * @param variables    テンプレートに渡す変数マップ
     * @throws MessagingException メッセージの構築または送信に失敗した場合
     */
    public void sendTemplateMail(String to, String subject, String templateName, Map<String, Object> variables)
            throws MessagingException {
        Context context = new Context();
        context.setVariables(variables);
        String html = templateEngine.process("mail/" + templateName, context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}
