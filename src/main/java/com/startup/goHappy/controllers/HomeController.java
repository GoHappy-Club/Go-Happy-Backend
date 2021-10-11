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
	String content = "  <table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;background:#ffffff;\">\n"
			+ "    <tr>\n"
			+ "      <td align=\"center\" style=\"padding:0;\">\n"
			+ "        <table role=\"presentation\" style=\"width:602px;border-collapse:collapse;border:1px solid #cccccc;border-spacing:0;text-align:left;\">\n"
			+ "          <tr>\n"
			+ "            <td align=\"center\" style=\"padding:40px 0 30px 0;background:#70bbd9;\">\n"
			+ "              <img src=\"https://assets.codepen.io/210284/h1.png\" alt=\"\" width=\"300\" style=\"height:auto;display:block;\" />\n"
			+ "            </td>\n"
			+ "          </tr>\n"
			+ "          <tr>\n"
			+ "            <td style=\"padding:36px 30px 42px 30px;\">\n"
			+ "              <table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "                <tr>\n"
			+ "                  <td style=\"padding:0 0 36px 0;color:#153643;\">\n"
			+ "                    <h1 style=\"font-size:24px;margin:0 0 20px 0;font-family:Arial,sans-serif;\">Creating Email Magic</h1>\n"
			+ "                    <p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Lorem ipsum dolor sit amet, consectetur adipiscing elit. In tempus adipiscing felis, sit amet blandit ipsum volutpat sed. Morbi porttitor, eget accumsan et dictum, nisi libero ultricies ipsum, posuere neque at erat.</p>\n"
			+ "                    <p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><a href=\"http://www.example.com\" style=\"color:#ee4c50;text-decoration:underline;\">In tempus felis blandit</a></p>\n"
			+ "                  </td>\n"
			+ "                </tr>\n"
			+ "                <tr>\n"
			+ "                  <td style=\"padding:0;\">\n"
			+ "                    <table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "                      <tr>\n"
			+ "                        <td style=\"width:260px;padding:0;vertical-align:top;color:#153643;\">\n"
			+ "                          <p style=\"margin:0 0 25px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><img src=\"https://assets.codepen.io/210284/left.gif\" alt=\"\" width=\"260\" style=\"height:auto;display:block;\" /></p>\n"
			+ "                          <p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Lorem ipsum dolor sit amet, consectetur adipiscing elit. In tempus adipiscing felis, sit amet blandit ipsum volutpat sed. Morbi porttitor, eget accumsan dictum, est nisi libero ultricies ipsum, in posuere mauris neque at erat.</p>\n"
			+ "                          <p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><a href=\"http://www.example.com\" style=\"color:#ee4c50;text-decoration:underline;\">Blandit ipsum volutpat sed</a></p>\n"
			+ "                        </td>\n"
			+ "                        <td style=\"width:20px;padding:0;font-size:0;line-height:0;\">&nbsp;</td>\n"
			+ "                        <td style=\"width:260px;padding:0;vertical-align:top;color:#153643;\">\n"
			+ "                          <p style=\"margin:0 0 25px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><img src=\"https://assets.codepen.io/210284/right.gif\" alt=\"\" width=\"260\" style=\"height:auto;display:block;\" /></p>\n"
			+ "                          <p style=\"margin:0 0 12px 0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\">Morbi porttitor, eget est accumsan dictum, nisi libero ultricies ipsum, in posuere mauris neque at erat. Lorem ipsum dolor sit amet, consectetur adipiscing elit. In tempus adipiscing felis, sit amet blandit ipsum volutpat sed.</p>\n"
			+ "                          <p style=\"margin:0;font-size:16px;line-height:24px;font-family:Arial,sans-serif;\"><a href=\"http://www.example.com\" style=\"color:#ee4c50;text-decoration:underline;\">In tempus felis blandit</a></p>\n"
			+ "                        </td>\n"
			+ "                      </tr>\n"
			+ "                    </table>\n"
			+ "                  </td>\n"
			+ "                </tr>\n"
			+ "              </table>\n"
			+ "            </td>\n"
			+ "          </tr>\n"
			+ "          <tr>\n"
			+ "            <td style=\"padding:30px;background:#ee4c50;\">\n"
			+ "              <table role=\"presentation\" style=\"width:100%;border-collapse:collapse;border:0;border-spacing:0;font-size:9px;font-family:Arial,sans-serif;\">\n"
			+ "                <tr>\n"
			+ "                  <td style=\"padding:0;width:50%;\" align=\"left\">\n"
			+ "                    <p style=\"margin:0;font-size:14px;line-height:16px;font-family:Arial,sans-serif;color:#ffffff;\">\n"
			+ "                      &reg; Someone, Somewhere 2021<br/><a href=\"http://www.example.com\" style=\"color:#ffffff;text-decoration:underline;\">Unsubscribe</a>\n"
			+ "                    </p>\n"
			+ "                  </td>\n"
			+ "                  <td style=\"padding:0;width:50%;\" align=\"right\">\n"
			+ "                    <table role=\"presentation\" style=\"border-collapse:collapse;border:0;border-spacing:0;\">\n"
			+ "                      <tr>\n"
			+ "                        <td style=\"padding:0 0 0 10px;width:38px;\">\n"
			+ "                          <a href=\"http://www.twitter.com/\" style=\"color:#ffffff;\"><img src=\"https://assets.codepen.io/210284/tw_1.png\" alt=\"Twitter\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\n"
			+ "                        </td>\n"
			+ "                        <td style=\"padding:0 0 0 10px;width:38px;\">\n"
			+ "                          <a href=\"http://www.facebook.com/\" style=\"color:#ffffff;\"><img src=\"https://assets.codepen.io/210284/fb_1.png\" alt=\"Facebook\" width=\"38\" style=\"height:auto;display:block;border:0;\" /></a>\n"
			+ "                        </td>\n"
			+ "                      </tr>\n"
			+ "                    </table>\n"
			+ "                  </td>\n"
			+ "                </tr>\n"
			+ "              </table>\n"
			+ "            </td>\n"
			+ "          </tr>\n"
			+ "        </table>\n"
			+ "      </td>\n"
			+ "    </tr>\n"
			+ "  </table>";
	
		@GetMapping("getEvents")
		public void getEvents() throws MessagingException {
			emailService.sendSimpleMessage("rashu.sharma14@gmail.com", "test", content);
		}
}
