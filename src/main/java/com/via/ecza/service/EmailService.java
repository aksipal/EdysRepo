package com.via.ecza.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.File;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class EmailService {


    @Value("${eczaneLink}")
    private String eczaneLink;

    @Autowired
    private JavaMailSender javaMailSender;

    SimpleMailMessage msg = new SimpleMailMessage();

    public void sendSimpleMessagee(String to, String subject, String text) {

        // SimpleMailMessage message = new SimpleMailMessage();
        msg.setTo("viaonur@gmail.com");
        msg.setSubject("ad");
        msg.setText("asdasd");
        javaMailSender.send(msg);

    }


    public void sendMessageWithAttachment(
            String to, String subject, String text, String pathToAttachment) throws MessagingException {


        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        //  helper.setFrom("viaonur@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);

        FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
        helper.addAttachment("Invoice", file);

        javaMailSender.send(message);
        // ...
    }

    public Boolean sendMailWithHtml(String supplierEmail, String note, String mailSubject) throws MessagingException {

        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            String htmlMsg = "<h3>" + note + "</h3>  Eczane Link : <a href='"+eczaneLink+"'> Buraya tıklayarak siparişleri kontrol edin.</a><br/>";



            //  String htmlMsg = "<a href='http://127.0.0.1:3000/pharmacy-index'> Buraya tıklayınız</a><br><img src='http://localhost:8500/show-document/pharma-logo'><br></img><img src='c:/phr.jpg'/>";
            //mimeMessage.setContent(htmlMsg, "text/html"); /** Use this or below line **/
            helper.setText(htmlMsg, true); // Use this or above line.
            helper.setTo(supplierEmail);
            // helper.addCc("");
            helper.setSubject(mailSubject);
            // helper.setFrom("sptestm@gmail.com");
            javaMailSender.send(mimeMessage);

        } catch (MessagingException msgEx) {

            throw new MessagingException("Email Hatası  " + msgEx);
        }


        return true;
    }


}
