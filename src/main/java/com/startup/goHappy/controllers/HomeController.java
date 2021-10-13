package com.startup.goHappy.controllers;

import java.util.List;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.services.HomeService;

@RestController
@RequestMapping("home")
public class HomeController {
	
	@Autowired
	HomeService homeService;
	@Autowired
	EventRepository erepo;
	@Autowired
	EmailService emailService;
	String content = "	<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\n"
			+ "		<tr>\n"
			+ "			<td align=\"center\" style=\"padding:0;\">\n"
			+ "				<table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\n"
			+ "					<tr>\n"
			+ "						<td align=\"center\" style=\"padding:40px 0 30px 0;background:#70bbd9;\">\n"
			+ "							<img src=\"https://storage.googleapis.com/gohappy-main-bucket/logo.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "					<tr>\n"
			+ "						<td style=\"padding:36px 30px 42px 30px;\">\n"
			+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "								<tr>\n"
			+ "									<td style=\"padding:0 0 36px 0;color:#153643;\">\n"
			+ "										<h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Thank you for choosing us, ${username}</h1>\n"
			+ "										<p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Below are the details for your session</p>\n"
			+ "                    <p><b>Title: </b>${title}</p>\n"
			+ "                    <p><b>Date: </b>${date}</p>\n"
			+ "                    <p><b>Time: </b>${time}</p>\n"
			+ "                    <p><b>Link to join: </b><a href=\"${zoomLink}\" style=\"text-decoration:underline;\">${zoomLink}</a></p>\n"
			+ "										\n"
			+ "									</td>\n"
			+ "								</tr>\n"
			+ "								\n"
			+ "							</table>\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "					<tr>\n"
			+ "						<td style=\"padding:30px;background:#ee4c50;\">\n"
			+ "							<table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\n"
			+ "								<tr>\n"
			+ "									<td style=\"padding:0;width:50%;\" align=\"left\">\n"
			+ "										<p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\n"
			+ "											&reg; GoHappy Club 2021<br/>\n"
			+ "										</p>\n"
			+ "									</td>\n"
			+ "									<td style=\"padding:0;width:50%;\" align=\"right\">\n"
			+ "										<table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "										</table>\n"
			+ "									</td>\n"
			+ "								</tr>\n"
			+ "							</table>\n"
			+ "						</td>\n"
			+ "					</tr>\n"
			+ "				</table>\n"
			+ "			</td>\n"
			+ "		</tr>\n"
			+ "	</table>";
	
		@GetMapping("getEvents")
		public void getEvents() throws MessagingException {
			emailService.sendSimpleMessage("rashu.sharma14@gmail.com", "test", content);
		}
}
