package com.startup.goHappy.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.repository.ReferralRepository;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.integrations.service.ZoomService;
import com.startup.goHappy.utils.TambolaGenerator;

import io.micrometer.core.instrument.util.StringEscapeUtils;

@RestController
@RequestMapping("event")
public class EventController {
	
	
	private static DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' HH:mm:ss a");

	@Autowired
	EventRepository eventService;

	@Autowired
	ReferralRepository referralService;
	
	@Autowired
	ZoomService zoomService;
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	TambolaGenerator tambolaGenerator;
	
	@Autowired
	UserProfileController userProfileController;
	
	@Autowired
	UserProfileRepository userProfileService;
	
	String content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
			+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n"
			+ "<head>\n"
			+ "<!--[if gte mso 9]>\n"
			+ "<xml>\n"
			+ "  <o:OfficeDocumentSettings>\n"
			+ "    <o:AllowPNG/>\n"
			+ "    <o:PixelsPerInch>96</o:PixelsPerInch>\n"
			+ "  </o:OfficeDocumentSettings>\n"
			+ "</xml>\n"
			+ "<![endif]-->\n"
			+ "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
			+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
			+ "  <meta name=\"x-apple-disable-message-reformatting\">\n"
			+ "  <!--[if !mso]><!--><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><!--<![endif]-->\n"
			+ "  <title></title>\n"
			+ "  \n"
			+ "    <style type=\"text/css\">\n"
			+ "      @media only screen and (min-width: 620px) {\n"
			+ "  .u-row {\n"
			+ "    width: 600px !important;\n"
			+ "  }\n"
			+ "  .u-row .u-col {\n"
			+ "    vertical-align: top;\n"
			+ "  }\n"
			+ "\n"
			+ "  .u-row .u-col-100 {\n"
			+ "    width: 600px !important;\n"
			+ "  }\n"
			+ "\n"
			+ "}\n"
			+ "\n"
			+ "@media (max-width: 620px) {\n"
			+ "  .u-row-container {\n"
			+ "    max-width: 100% !important;\n"
			+ "    padding-left: 0px !important;\n"
			+ "    padding-right: 0px !important;\n"
			+ "  }\n"
			+ "  .u-row .u-col {\n"
			+ "    min-width: 320px !important;\n"
			+ "    max-width: 100% !important;\n"
			+ "    display: block !important;\n"
			+ "  }\n"
			+ "  .u-row {\n"
			+ "    width: calc(100% - 40px) !important;\n"
			+ "  }\n"
			+ "  .u-col {\n"
			+ "    width: 100% !important;\n"
			+ "  }\n"
			+ "  .u-col > div {\n"
			+ "    margin: 0 auto;\n"
			+ "  }\n"
			+ "}\n"
			+ "body {\n"
			+ "  margin: 0;\n"
			+ "  padding: 0;\n"
			+ "}\n"
			+ "\n"
			+ "table,\n"
			+ "tr,\n"
			+ "td {\n"
			+ "  vertical-align: top;\n"
			+ "  border-collapse: collapse;\n"
			+ "}\n"
			+ "\n"
			+ "p {\n"
			+ "  margin: 0;\n"
			+ "}\n"
			+ "\n"
			+ ".ie-container table,\n"
			+ ".mso-container table {\n"
			+ "  table-layout: fixed;\n"
			+ "}\n"
			+ "\n"
			+ "* {\n"
			+ "  line-height: inherit;\n"
			+ "}\n"
			+ "\n"
			+ "a[x-apple-data-detectors='true'] {\n"
			+ "  color: inherit !important;\n"
			+ "  text-decoration: none !important;\n"
			+ "}\n"
			+ "\n"
			+ "table, td { color: #000000; } a { color: #0000ee; text-decoration: underline; } @media (max-width: 480px) { #u_content_image_1 .v-src-width { width: auto !important; } #u_content_image_1 .v-src-max-width { max-width: 48% !important; } #u_content_image_2 .v-container-padding-padding { padding: 7px !important; } #u_content_image_2 .v-src-width { width: auto !important; } #u_content_image_2 .v-src-max-width { max-width: 13% !important; } #u_content_text_15 .v-container-padding-padding { padding: 10px 10px 10px 20px !important; } #u_content_button_1 .v-padding { padding: 9px 35px !important; } #u_content_divider_6 .v-container-padding-padding { padding: 7px !important; } }\n"
			+ "    </style>\n"
			+ "  \n"
			+ "  \n"
			+ "\n"
			+ "<!--[if !mso]><!--><link href=\"https://fonts.googleapis.com/css?family=Cabin:400,700\" rel=\"stylesheet\" type=\"text/css\"><link href=\"https://fonts.googleapis.com/css?family=Lato:400,700\" rel=\"stylesheet\" type=\"text/css\"><link href=\"https://fonts.googleapis.com/css?family=Open+Sans:400,700\" rel=\"stylesheet\" type=\"text/css\"><!--<![endif]-->\n"
			+ "\n"
			+ "</head>\n"
			+ "\n"
			+ "<body class=\"clean-body u_body\" style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #e7e7e7;color: #000000\">\n"
			+ "  <!--[if IE]><div class=\"ie-container\"><![endif]-->\n"
			+ "  <!--[if mso]><div class=\"mso-container\"><![endif]-->\n"
			+ "  <table style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #e7e7e7;width:100%\" cellpadding=\"0\" cellspacing=\"0\">\n"
			+ "  <tbody>\n"
			+ "  <tr style=\"vertical-align: top\">\n"
			+ "    <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
			+ "    <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #e7e7e7;\"><![endif]-->\n"
			+ "    \n"
			+ "\n"
			+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
			+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #e6a501;\">\n"
			+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">\n"
			+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #e6a501;\"><![endif]-->\n"
			+ "      \n"
			+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
			+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
			+ "  <div style=\"width: 100% !important;\">\n"
			+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
			+ "  \n"
			+ "<table id=\"u_content_image_1\" style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:1px 8px 0px 10px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
			+ "  <tr>\n"
			+ "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">\n"
			+ "      \n"
			+ "      <img align=\"center\" border=\"0\" src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-6.png\" alt=\"GoHappy Club\" title=\"GoHappy Club\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 100%;max-width: 176px;\" width=\"176\" class=\"v-src-width v-src-max-width\"/>\n"
			+ "      \n"
			+ "    </td>\n"
			+ "  </tr>\n"
			+ "</table>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
//			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
//			+ "  <tbody>\n"
//			+ "    <tr>\n"
//			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:4px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
//			+ "        \n"
//			+ "  <table height=\"0px\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 1px solid #acacac;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
//			+ "    <tbody>\n"
//			+ "      <tr style=\"vertical-align: top\">\n"
//			+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
//			+ "          <span>&#160;</span>\n"
//			+ "        </td>\n"
//			+ "      </tr>\n"
//			+ "    </tbody>\n"
//			+ "  </table>\n"
//			+ "\n"
//			+ "      </td>\n"
//			+ "    </tr>\n"
//			+ "  </tbody>\n"
//			+ "</table>\n"
			+ "\n"
			+ "<table id=\"u_content_image_2\" style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:9px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n"
			+ "  <tr>\n"
			+ "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">\n"
			+ "      \n"
			+ "      <img align=\"center\" border=\"0\" src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-5.png\" alt=\"Image\" title=\"Image\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 12%;max-width: 69.84px;\" width=\"69.84\" class=\"v-src-width v-src-max-width\"/>\n"
			+ "      \n"
			+ "    </td>\n"
			+ "  </tr>\n"
			+ "</table>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:11px 6px 10px 8px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #ffffff; line-height: 160%; text-align: center; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 160%;\"><span style=\"color: #e67e23; font-size: 18px; line-height: 28.8px; background-color: #ffffff;\">&nbsp; Your <span style=\"line-height: 28.8px; font-size: 18px;\">Registration</span> is Completed!&nbsp; </span></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "<!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
			+ "    </div>\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
			+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ffffff;\">\n"
			+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">\n"
			+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ffffff;\"><![endif]-->\n"
			+ "      \n"
			+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
			+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
			+ "  <div style=\"width: 100% !important;\">\n"
			+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
			+ "  \n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:30px 10px 30px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 130%; text-align: justify; word-wrap: break-word;\">\n"
			+ "    <p style=\"line-height: 130%; font-size: 14px;\"><span style=\"font-size: 16px; line-height: 20.8px;\"><strong>${name}</strong></span></p>\n"
			+ "<p style=\"font-size: 14px; line-height: 130%;\">&nbsp;</p>\n"
			+ "<p style=\"font-size: 14px; line-height: 130%;\"><span style=\"font-size: 16px; line-height: 20.8px; font-family: Lato, sans-serif;\">Thank you for registering for this session,&nbsp; <strong>${title}</strong>.</span></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "<!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
			+ "    </div>\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
			+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #f7f6f4;\">\n"
			+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">\n"
			+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #f7f6f4;\"><![endif]-->\n"
			+ "      \n"
			+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
			+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
			+ "  <div style=\"width: 100% !important;\">\n"
			+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
			+ "  \n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:25px 10px 0px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-family: Lato, sans-serif; font-size: 14px; line-height: 19.6px;\"><strong><span style=\"font-size: 18px; line-height: 25.2px; color: #236fa1;\">SESSION DETAILS</span></strong></span></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:5px 10px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <table height=\"0px\" align=\"left\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"22%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 3px solid #e67e23;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "    <tbody>\n"
			+ "      <tr style=\"vertical-align: top\">\n"
			+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "          <span>&#160;</span>\n"
			+ "        </td>\n"
			+ "      </tr>\n"
			+ "    </tbody>\n"
			+ "  </table>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 10px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><strong>Member Name: </strong>${name}</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:5px 10px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><strong>Meeting ID: </strong>${meetingId}</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:5px 10px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><strong>Meeting Password:</strong>&nbsp;12345</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:5px 10px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><strong>Meeting Link : </strong>${zoomLink}</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:5px 10px 25px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 140%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\"><strong>Session Date : </strong>${date}</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "\n"
			+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "<!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
			+ "    </div>\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "\n"
			+ "\n"
			+ "\n"
			+ "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">\n"
			+ "  <div class=\"u-row\" style=\"Margin: 0 auto;min-width: 320px;max-width: 600px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: #ffffff;\">\n"
			+ "    <div style=\"border-collapse: collapse;display: table;width: 100%;background-color: transparent;\">\n"
			+ "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px;\"><tr style=\"background-color: #ffffff;\"><![endif]-->\n"
			+ "      \n"
			+ "<!--[if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"width: 600px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\" valign=\"top\"><![endif]-->\n"
			+ "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 600px;display: table-cell;vertical-align: top;\">\n"
			+ "  <div style=\"width: 100% !important;\">\n"
			+ "  <!--[if (!mso)&(!IE)]><!--><div style=\"padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;\"><!--<![endif]-->\n"
			+ "  \n"
			+ "<table id=\"u_content_text_15\" style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:20px 20px 15px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 160%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 160%;\">If you have any doubts or questions Whatsapp us at <strong>7888384477</strong> or Email us at <strong>sessions@gohappyclub.in</strong></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:0px 10px 30px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #333333; line-height: 160%; text-align: left; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 160%;\"><span style=\"font-size: 16px; line-height: 25.6px; font-family: Lato, sans-serif;\">We look forward to seeing you at the session.</span></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table id=\"u_content_button_1\" style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px 10px 30px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "<div align=\"center\">\n"
			+ "  <!--[if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-spacing: 0; border-collapse: collapse; mso-table-lspace:0pt; mso-table-rspace:0pt;font-family:'Open Sans',sans-serif;\"><tr><td style=\"font-family:'Open Sans',sans-serif;\" align=\"center\"><v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"\" style=\"height:43px; v-text-anchor:middle; width:176px;\" arcsize=\"4.5%\" stroke=\"f\" fillcolor=\"#e6a501\"><w:anchorlock/><center style=\"color:#FFFFFF;font-family:'Open Sans',sans-serif;\"><![endif]-->\n"
			+ "    <a href=\"${zoomLink}\" target=\"_blank\" style=\"box-sizing: border-box;display: inline-block;font-family:'Open Sans',sans-serif;text-decoration: none;-webkit-text-size-adjust: none;text-align: center;color: #FFFFFF; background-color: #e6a501; border-radius: 2px;-webkit-border-radius: 2px; -moz-border-radius: 2px; width:auto; max-width:100%; overflow-wrap: break-word; word-break: break-word; word-wrap:break-word; mso-border-alt: none;\">\n"
			+ "      <span class=\"v-padding\" style=\"display:block;padding:12px 35px;line-height:120%;\"><span style=\"font-family: Cabin, sans-serif; font-size: 14px; line-height: 16.8px;\"><strong><span style=\"font-size: 16px; line-height: 19.2px;\">Join Session </span></strong></span></span>\n"
			+ "    </a>\n"
			+ "  <!--[if mso]></center></v:roundrect></td></tr></table><![endif]-->\n"
			+ "</div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table id=\"u_content_divider_6\" style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <table height=\"0px\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 2px solid #939391;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "    <tbody>\n"
			+ "      <tr style=\"vertical-align: top\">\n"
			+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "          <span>&#160;</span>\n"
			+ "        </td>\n"
			+ "      </tr>\n"
			+ "    </tbody>\n"
			+ "  </table>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:14px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "<div align=\"center\">\n"
			+ "  <div style=\"display: table; max-width:223px;\">\n"
			+ "  <!--[if (mso)|(IE)]><table width=\"223\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"border-collapse:collapse;\" align=\"center\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse; mso-table-lspace: 0pt;mso-table-rspace: 0pt; width:223px;\"><tr><![endif]-->\n"
			+ "  \n"
			+ "    \n"
			+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 24px;\" valign=\"top\"><![endif]-->\n"
			+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 24px\">\n"
			+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
			+ "        <a href=\"https://www.facebook.com/gohappyclub/\" title=\"Facebook\" target=\"_blank\">\n"
			+ "          <img src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-3.png\" alt=\"Facebook\" title=\"Facebook\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
			+ "        </a>\n"
			+ "      </td></tr>\n"
			+ "    </tbody></table>\n"
			+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "    \n"
			+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 24px;\" valign=\"top\"><![endif]-->\n"
			+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 24px\">\n"
			+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
			+ "        <a href=\"https://www.instagram.com/gohappy_club/\" title=\"Instagram\" target=\"_blank\">\n"
			+ "          <img src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-1.png\" alt=\"Instagram\" title=\"Instagram\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
			+ "        </a>\n"
			+ "      </td></tr>\n"
			+ "    </tbody></table>\n"
			+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "    \n"
			+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 24px;\" valign=\"top\"><![endif]-->\n"
			+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 24px\">\n"
			+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
			+ "        <a href=\"https://www.linkedin.com/company/gohappyclub\" title=\"LinkedIn\" target=\"_blank\">\n"
			+ "          <img src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-2.png\" alt=\"LinkedIn\" title=\"LinkedIn\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
			+ "        </a>\n"
			+ "      </td></tr>\n"
			+ "    </tbody></table>\n"
			+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "    \n"
			+ "    <!--[if (mso)|(IE)]><td width=\"32\" style=\"width:32px; padding-right: 0px;\" valign=\"top\"><![endif]-->\n"
			+ "    <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"32\" height=\"32\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;margin-right: 0px\">\n"
			+ "      <tbody><tr style=\"vertical-align: top\"><td align=\"left\" valign=\"middle\" style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">\n"
			+ "        <a href=\" wa.link/83jogj\" title=\"WhatsApp\" target=\"_blank\">\n"
			+ "          <img src=\"https://storage.googleapis.com/gohappy-main-bucket/website/images/image-4.png\" alt=\"WhatsApp\" title=\"WhatsApp\" width=\"32\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: block !important;border: none;height: auto;float: none;max-width: 32px !important\">\n"
			+ "        </a>\n"
			+ "      </td></tr>\n"
			+ "    </tbody></table>\n"
			+ "    <!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "    \n"
			+ "    \n"
			+ "    <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #828080; line-height: 160%; text-align: center; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 160%;\">306/19, Lane No.5, Golden City,</p>\n"
			+ "<p style=\"font-size: 14px; line-height: 160%;\">Amritsar-143001, India</p>\n"
			+ "<p style=\"font-size: 14px; line-height: 160%;\">&nbsp;</p>\n"
			+ "<p style=\"font-size: 14px; line-height: 160%;\"><strong>GoHappy Club</strong></p>\n"
			+ "<p style=\"font-size: 14px; line-height: 160%;\"><strong><span style=\"font-size: 12px; line-height: 19.2px;\"> INDIA KA SABSE KUSH PARIVAR</span></strong></p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <table height=\"0px\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"64%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 1px solid #BBBBBB;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "    <tbody>\n"
			+ "      <tr style=\"vertical-align: top\">\n"
			+ "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">\n"
			+ "          <span>&#160;</span>\n"
			+ "        </td>\n"
			+ "      </tr>\n"
			+ "    </tbody>\n"
			+ "  </table>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "<table style=\"font-family:'Open Sans',sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">\n"
			+ "  <tbody>\n"
			+ "    <tr>\n"
			+ "      <td class=\"v-container-padding-padding\" style=\"overflow-wrap:break-word;word-break:break-word;padding:0px 10px 20px;font-family:'Open Sans',sans-serif;\" align=\"left\">\n"
			+ "        \n"
			+ "  <div style=\"color: #828080; line-height: 140%; text-align: center; word-wrap: break-word;\">\n"
			+ "    <p style=\"font-size: 14px; line-height: 140%;\">&copy; 2021 GoHappy Club. All Rights Reserved.</p>\n"
			+ "  </div>\n"
			+ "\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </tbody>\n"
			+ "</table>\n"
			+ "\n"
			+ "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "<!--[if (mso)|(IE)]></td><![endif]-->\n"
			+ "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->\n"
			+ "    </div>\n"
			+ "  </div>\n"
			+ "</div>\n"
			+ "\n"
			+ "\n"
			+ "    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->\n"
			+ "    </td>\n"
			+ "  </tr>\n"
			+ "  </tbody>\n"
			+ "  </table>\n"
			+ "  <!--[if mso]></div><![endif]-->\n"
			+ "  <!--[if IE]></div><![endif]-->\n"
			+ "</body>\n"
			+ "\n"
			+ "</html>";

	@SuppressWarnings("deprecation")
	@PostMapping("create")
	public void createEvent(@RequestBody JSONObject event) throws IOException {
		//eventService.deleteAll();
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ObjectMapper objectMapper = new ObjectMapper();
		Event ev = objectMapper.readValue(event.toJSONString(), Event.class);	
		ev.setId(UUID.randomUUID().toString());
		ev.setParticipantList(new ArrayList<String>());
		ev.setCostType(StringUtils.isEmpty(event.getString("type"))?"free":"paid");
		ev.setType(StringUtils.isEmpty(event.getString("type"))?"session":event.getString("type"));
		if(!StringUtils.isEmpty(ev.getCron())) {
			TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
			CronSequenceGenerator generator = new CronSequenceGenerator(ev.getCron(),tz);
			Date nextExecutionDate = generator.next(Date.from(zonedDateTime.now().toInstant()));
			ev.setIsParent(true);
			ev.setEventDate("");
			eventService.save(ev);
			long duration = (Long.parseLong(ev.getEndTime())- nextExecutionDate.getTime())/(60000);
			int i=0;
			String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
//			Date d = new Date(ev.getStartTime());
			Event tempChild=null;
			while(i<Integer.parseInt(event.getString("occurance")) && nextExecutionDate!=null) {
				Event childEvent = objectMapper.readValue(event.toJSONString(), Event.class);
				childEvent.setId(UUID.randomUUID().toString());
				childEvent.setIsParent(false);
				childEvent.setParentId(ev.getId());
				childEvent.setIsScheduled(false);
				childEvent.setCron("");
				childEvent.setStartTime(""+nextExecutionDate.getTime());
				childEvent.setEventDate(""+nextExecutionDate.getTime());
//				if(childEvent.getEventName().toLowerCase().contains("tambola")) {
//					List<String> tambolaTickets = new ArrayList<>();
//					for(int j=0;j<childEvent.getSeatsLeft();j++) {
//						tambolaTickets.add("\""+tambolaGenerator.generate()+"\"");
//					}
//					childEvent.setTambolaTickets(tambolaTickets);
//				}
				long start = nextExecutionDate.getTime();
				
				Date newEndTime = new Date(nextExecutionDate.getTime()+duration*60000);
				childEvent.setEndTime(""+newEndTime.getTime());				
				
				ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
				obj.setTopic(ev.getEventName());
				obj.setTimezone("Asia/Kolkata");
				obj.setDuration((int)duration);
				obj.setType(2);
				obj.setPassword("12345");
				Date startDate = new Date(start);
				
				DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
				LocalDateTime summerDay = LocalDateTime.of(startDate.getYear()+1900, startDate.getMonth()+1, startDate.getDate(), startDate.getHours(), startDate.getMinutes());
				String finalDateForZoom = newYorkDateFormatter.format(ZonedDateTime.of(summerDay, ZoneId.of("Asia/Kolkata")));
				finalDateForZoom = finalDateForZoom.replace(" ", "T");

				obj.setStart_time(finalDateForZoom);
				ZoomMeetingObjectDTO zoomData = zoomService.createMeeting(obj);
				String zoomLink = zoomData.getJoin_url();
				Long meetingId = zoomData.getId();

				childEvent.setMeetingLink(zoomLink);
				childEvent.setMeetingId(""+meetingId);
				eventService.save(childEvent);
				tempChild=childEvent;
				nextExecutionDate = generator.next(nextExecutionDate);
				i++;
				
			}
		}
		else {
			String newYorkDateTimePattern = "yyyy-MM-dd HH:mm:ssZ";
			long duration = (Long.parseLong(ev.getEndTime())- Long.parseLong(ev.getStartTime()))/(60000);
			ZoomMeetingObjectDTO obj = new ZoomMeetingObjectDTO();
			obj.setTopic(ev.getEventName());
			obj.setTimezone("Asia/Kolkata");
			obj.setDuration((int)duration);
			obj.setType(2);
			obj.setPassword("12345");
			Date startDate = new Date(Long.parseLong(ev.getStartTime()));
			
			DateTimeFormatter newYorkDateFormatter = DateTimeFormatter.ofPattern(newYorkDateTimePattern);
			LocalDateTime summerDay = LocalDateTime.of(startDate.getYear()+1900, startDate.getMonth()+1, startDate.getDate(), startDate.getHours(), startDate.getMinutes());
			String finalDateForZoom = newYorkDateFormatter.format(ZonedDateTime.of(summerDay, ZoneId.of("Asia/Kolkata")));
			finalDateForZoom = finalDateForZoom.replace(" ", "T");

			obj.setStart_time(finalDateForZoom);
			
			ZoomMeetingObjectDTO zoomData = zoomService.createMeeting(obj);
			
			String zoomLink = zoomData.getJoin_url();
			Long meetingId = zoomData.getId();
			
			ev.setMeetingLink(zoomLink);
			ev.setMeetingId(""+meetingId);
			eventService.save(ev);
		}
		
		return;
	}
	@PostMapping("delete")
	public void deleteEvent(@RequestBody JSONObject params) {
		String id = params.getString("id");
		eventService.delete(eventService.get(id).get());
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<Event> events = eventService.retrieveAll();
		List<Event> result = IterableUtils.toList(events);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("getEventsByDate")
	public JSONObject getEventsByDate(@RequestBody JSONObject params){
		System.out.println("date"+params.getString("date"));
		
		Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		zonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		System.out.println(zonedDateTime);

		System.out.println(zonedDateTime.toInstant().toEpochMilli());
		CollectionReference eventsRef = eventService.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime2 = java.time.ZonedDateTime
		                            .ofInstant(instance2,java.time.ZoneId.of("Asia/Kolkata"));
		String newDate = zonedDateTime2.toInstant().toEpochMilli()+"";
		if(params.getString("date").compareTo(newDate)<0) {
			params.put("date", newDate);
		}
//		Query query1 = eventsRef.whereGreaterThan("endTime", params.getString("date"));
//		Query query1New = eventsRef.whereGreaterThan("endTime", params.getString("date")).whereEqualTo("isParent",false);
		Query queryNew = eventsRef.whereGreaterThanOrEqualTo("endTime", params.getString("date")).whereEqualTo("isParent",false);

//		Query query2 = null;
//		Query query2New = null;
		if(params.getString("endDate")!=null) {
//			query2 = eventsRef.whereLessThan("endTime", ""+params.getString("endDate"));
			queryNew = queryNew.whereLessThanOrEqualTo("endTime", ""+params.getString("endDate"));
//			query2New = eventsRef.whereLessThan("endTime", ""+params.getString("endDate")).whereEqualTo("isParent",false);
		}
		else {
//			query2 = eventsRef.whereLessThan("endTime", ""+zonedDateTime.toInstant().toEpochMilli());
			queryNew = queryNew.whereLessThanOrEqualTo("endTime", ""+zonedDateTime.toInstant().toEpochMilli());
//			query2New = eventsRef.whereLessThan("endTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereEqualTo("isParent",false);
		}
//		Query query3 = eventsRef.whereEqualTo("isParent", false);

//		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
//		ApiFuture<QuerySnapshot> querySnapshot1New = query1New.get();
//		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();
//		ApiFuture<QuerySnapshot> querySnapshot2New = query2New.get();
//		ApiFuture<QuerySnapshot> querySnapshot3 = query3.get();
		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();
		
//		Set<Event> events1 = new HashSet<>();
//		Set<Event> events2 = new HashSet<>();
//		Set<Event> events3 = new HashSet<>();
//		Set<Event> events1New = new HashSet<>();
//		Set<Event> events2New = new HashSet<>();
		Set<Event> eventsNew = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				eventsNew.add(document.toObject(Event.class));
			}
//			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
//				events1.add(document.toObject(Event.class));
//			}
//			for (DocumentSnapshot document : querySnapshot1New.get().getDocuments()) {
//				events1New.add(document.toObject(Event.class));
//			}
//			for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
//				events2.add(document.toObject(Event.class));
//			}
//			for (DocumentSnapshot document : querySnapshot2New.get().getDocuments()) {
//				events2New.add(document.toObject(Event.class));
//			}
//			for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
//				events3.add(document.toObject(Event.class));
//			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
//		events1.retainAll(events2);
//		events1.retainAll(events3);
//		events1New.retainAll(events2New);
//		events1New.retainAll(events3);
//		List<Event> events = IterableUtils.toList(events1);
//		List<Event> eventsNewList = IterableUtils.toList(events1New);
		List<Event> eventsNewBest = IterableUtils.toList(eventsNew);
//		Collections.sort(events,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
//		Collections.sort(eventsNewList,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		Collections.sort(eventsNewBest,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
//		System.out.println("Old Approach: "+events.size()+", New Approach: "+eventsNewList.size()+", Another Approach: "+eventsNewBest.size());
		JSONObject output = new JSONObject();
		output.put("events", eventsNewBest);
		return output;
	}
	@PostMapping("bookEvent")
	public String bookEvent(@RequestBody JSONObject params) throws IOException, MessagingException, GeneralSecurityException, InterruptedException, ExecutionException {
		CollectionReference eventRef = eventService.getCollectionReference();
		CollectionReference referrals = referralService.getCollectionReference();

		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.get();
		String ticket = "\""+params.getString("tambolaTicket")+"\"";
		if(event.getSeatsLeft()<=0) {
			return "FAILED:FULL";
		}
		event.setSeatsLeft(event.getSeatsLeft()-1);
		List<String> participants = event.getParticipantList();
		if(participants==null) {
			participants = new ArrayList<String>();
		}
		participants.add(params.getString("phoneNumber"));
		
		event.setParticipantList(participants);
		List<String> tambolaTickets = event.getTambolaTickets();
		if(tambolaTickets==null) {
			tambolaTickets = new ArrayList<String>();
		}
		tambolaTickets.add(ticket);
		
		event.setTambolaTickets(tambolaTickets);
		 
		
		Map<String, Object> map = new HashMap<>();
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		map.put("tambolaTickets",tambolaTickets);
		eventRef.document(params.getString("id")).update(map);
//		content = content.replace("${username}", event.getEventName());
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(event.getStartTime()));
		calendar.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));		
		String currentContent = content;
		currentContent = currentContent.replace("${title}", event.getEventName());
		currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${meetingId}", event.getMeetingId()!=null?event.getMeetingId():"");
		//content = content.replace("${zoomLink}", event.getMeetingLink());
		currentContent = currentContent.replace("${date}", FOMATTER.format(((GregorianCalendar) calendar).toZonedDateTime()));
		UserProfile user = userProfileController.getUserByPhone(params).getObject("user", UserProfile.class);
		if(user!=null) {
			currentContent = currentContent.replace("${name}",user.getName());
			currentContent = currentContent.replace("${name}",user.getName());
			Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
			sessionsAttended++;
			user.setSessionsAttended(""+sessionsAttended);
			userProfileService.save(user);
//			Query query = referrals.whereEqualTo("to", user.getPhone());
//			ApiFuture<QuerySnapshot> querySnapshot = query.get();
//			if(querySnapshot.get().getDocuments().size()!=0){
//				Referral referred = querySnapshot.get().getDocuments().get(0).toObject(Referral.class);
//				referred.setHasAttendedSession(true);
//				referralService.save(referred);
//			}

			if(!StringUtils.isEmpty(user.getEmail()))
				emailService.sendSimpleMessage(user.getEmail(), "GoHappy Club: Session Booked", currentContent);
		}

		return "SUCCESS";
	}
	
  public String sendEmail(String email, String subject, String content)  {
	  try {
		emailService.sendSimpleMessage(email,subject,content);
	} catch (MessagingException | IOException | GeneralSecurityException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
    return "SUCCESS"; 
  }
	
	
	@PostMapping("cancelEvent")
	public String cancelEvent(@RequestBody JSONObject params) throws IOException {
		CollectionReference eventRef = eventService.getCollectionReference();
		Optional<Event> oevent = eventService.findById(params.getString("id"));
		Event event = oevent.get();

		event.setSeatsLeft(event.getSeatsLeft()+1);
		List<String> participants = event.getParticipantList();
		int index = participants.indexOf(params.getString("phoneNumber"));
		participants.remove(params.getString("phoneNumber"));
		event.setParticipantList(participants);
		
		Map<String, Object> map = new HashMap<>();
		if(event.getEventName().contains("Tambola")) {
			List<String> tickets = event.getTambolaTickets();
			if(tickets!=null) {
				tickets.remove(index);
				event.setTambolaTickets(tickets);
				map.put("tambolaTickets",tickets);
			}
		}
		map.put("participantList",participants);
		map.put("seatsLeft",event.getSeatsLeft());
		eventRef.document(params.getString("id")).update(map);
		try {
			JSONObject userJson = userProfileController.getUserByPhone(params);
			UserProfile user = userJson.getObject("user", UserProfile.class);
			Integer sessionsAttended = Integer.parseInt(user.getSessionsAttended());
			sessionsAttended--;
			user.setSessionsAttended(""+sessionsAttended);
			userProfileService.save(user);
		} catch (IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "SUCCESS";
	}
	@PostMapping("mySessions")
	public JSONObject mySessions(@RequestBody JSONObject params) throws IOException {
		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ZonedDateTime endZonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		CollectionReference eventsRef = eventService.getCollectionReference();
//		.whereArrayContains("participantList", params.getString("email")).whereEqualTo("isParent", false)
		Query query1 = eventsRef.whereGreaterThan("startTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("phoneNumber")).whereEqualTo("isParent", false);
//		zonedDateTime.toInstant().toEpochMilli()
		Query query2 = eventsRef.whereLessThan("endTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereArrayContains("participantList", params.getString("phoneNumber")).whereEqualTo("isParent", false);
		
		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();

		
		Set<Event> events1 = new HashSet<>();
		Set<Event> events2 = new HashSet<>();
		Set<Event> events3 = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
				events1.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
				events2.add(document.toObject(Event.class));  
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		JSONObject paramsForEventByDate = new JSONObject();
		paramsForEventByDate.put("date", ""+zonedDateTime.toInstant().toEpochMilli());
		paramsForEventByDate.put("endDate", ""+zonedDateTime.toInstant().toEpochMilli());
		
		JSONObject ongoingJSON = getOngoingEvents(paramsForEventByDate);
		JSONObject output = new JSONObject();
		ObjectMapper objectMapper = new ObjectMapper();
		for(Object obj:ongoingJSON.getJSONArray("events")) {
			Event ev =((Event)obj);
			if(ev.getParticipantList()!=null && ev.getParticipantList().size()>0 &&  ev.getParticipantList().contains(params.getString("phoneNumber"))) {
				events3.add(ev);
			}
		}
		events1.removeAll(events3);
		ArrayList<Event> events1List = new ArrayList<>(events1);
		ArrayList<Event> events2List = new ArrayList<>(events2);
		Collections.sort(events1List,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		Collections.sort(events2List,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		Collections.reverse(events2List);
		output.put("upcomingEvents", events1List);
		output.put("expiredEvents", events2List.subList(0,events2List.size()>12?12:events2List.size()));
		output.put("ongoingEvents", events3);
		
		return output;
	}
	public JSONObject getOngoingEvents(JSONObject params){
		System.out.println(params.getString("date"));
		
		Instant instance = java.time.Instant.ofEpochMilli(Long.parseLong(params.getString("date")));
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
		                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		zonedDateTime = zonedDateTime.with(LocalTime.of ( 23 , 59 ));
		System.out.println(zonedDateTime);

		System.out.println(zonedDateTime.toInstant().toEpochMilli());
		CollectionReference eventsRef = eventService.getCollectionReference();
		Long newStartTime = Long.parseLong(params.getString("date"))+600000;
		Long newEndTime = Long.parseLong(params.getString("endDate"))+600000;
		
		Query query1 = eventsRef.whereLessThan("startTime", ""+newStartTime);
		Query query2 = eventsRef.whereGreaterThan("endTime", ""+newEndTime);
		Query query3 = eventsRef.whereEqualTo("isParent", false);

		ApiFuture<QuerySnapshot> querySnapshot1 = query1.get();
		ApiFuture<QuerySnapshot> querySnapshot2 = query2.get();
		ApiFuture<QuerySnapshot> querySnapshot3 = query3.get();

		
		Set<Event> events1 = new HashSet<>();
		Set<Event> events2 = new HashSet<>();
		Set<Event> events3 = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
				events1.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot2.get().getDocuments()) {
				events2.add(document.toObject(Event.class));  
			}
			for (DocumentSnapshot document : querySnapshot3.get().getDocuments()) {
				events3.add(document.toObject(Event.class));  
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		events1.retainAll(events2);
		events1.retainAll(events3);
		List<Event> events = IterableUtils.toList(events1);
		Collections.sort(events,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		System.out.println(events.size());
		JSONObject output = new JSONObject();
		output.put("events", events);
		return output;
	}
}  

