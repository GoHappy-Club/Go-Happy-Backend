package com.startup.goHappy;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.Assert.*;

@SpringBootTest
class GoHappyApplicationTests {

	@Test
	public void HelloWorldControllerTest() throws Exception {
		HelloWorldController hello = new HelloWorldController();
		assertEquals("hello world!", hello.hello());

	}

}
