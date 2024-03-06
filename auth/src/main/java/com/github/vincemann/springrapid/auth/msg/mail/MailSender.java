package com.github.vincemann.springrapid.auth.msg.mail;

/**
 * The mail sender interface for sending mail
 */

public interface MailSender<MailData> {

	void send(MailData mail);
}