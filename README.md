# Introduction to Spring MVC4

#### Links

#### Notes and Commands

- Annotations
  - `@Controller`

  - `@RestController` - rest services. A convenient annotation replacing `@Controller` and `@ResponseBody`

  - `@Configuration` - instead of xml for creating an application context

  - `@EnableWebMvc` - with `@Configuration` bootstaps configuration of the application. Only used for Spring MVC web apps. Can be extended with `WebMvcConfigurerAdapter`.

  - `@ComponentScan` - location of class which should be scanned 

  - `@SessionAttribute` - bound object to a session

    ```java
    @Controller
    @SessionAttributes("event")
    public class EventController {}
    ```

    ​
- redirect vs forward
  - redirect sends us out of the application and rewire the url. We create a new request.
  - forward continues the response with unchanged url

#### Instructions

###### Convert SpringBoot project to deployable war 

1. Inside pom.xml change packaging to **war**

2. Remove class with a **main** method

3. Add class annotated with: `@Configuration` and `@EnableWebMvc`

4. Inside pom.xml modify spring boot plugin

   ```xml
   <plugin>
   	<groupId>org.springframework.boot</groupId>
   	<artifactId>spring-boot-maven-plugin</artifactId>
   	<executions>
   		<execution>
   			<goals>
   				<goal>repackage</goal>
   			</goals>
   			<configuration>
   				<skip>true</skip>
   			</configuration>
   		</execution>
   	</executions>
   </plugin>
   ```

5. Configure Web app context with web.xml or java class

   - Create src\main\webapp\WEB-INF\web.xml

   ```xml
   <web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   	xmlns="http://xmlns.jcp.org/xml/ns/javaee" xmlns:web="http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
   	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
   	id="WebApp_ID" version="3.1">

   	<display-name>Spring Web MVC Application</display-name>
    	<servlet>
   		<servlet-name>springDispatcherServlet</servlet-name>
   		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
   		<init-param>
   			<param-name>contextClass</param-name>
   			<param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext
   			</param-value>
   		</init-param>
   		<init-param>
   			<param-name>contextConfigLocation</param-name>
   			<param-value>com.pluralsight.WebConfig</param-value>
   		</init-param>
   		<load-on-startup>1</load-on-startup>
   	</servlet>

   	<servlet-mapping>
   		<servlet-name>springDispatcherServlet</servlet-name>
   		<url-pattern>*.html</url-pattern>
   	</servlet-mapping> 

   </web-app>
   ```

   - Create java class instead of web.xml

   ```java
   public class WebAppInitializer implements WebApplicationInitializer {

   	@Override
   	public void onStartup(ServletContext servletContext) throws ServletException {
   		WebApplicationContext context = getContext();
   		servletContext.addListener(new ContextLoaderListener(context));
   		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet", new DispatcherServlet(context));
   		dispatcher.setLoadOnStartup(1);
   		dispatcher.addMapping("*.html");
   /*		request to pdf will go trough dispatcher*/	
   		dispatcher.addMapping("*.pdf");
   	}

   	private AnnotationConfigWebApplicationContext getContext() {
   		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
   		context.register(WebConfig.class);
   		return context;
   	}

   }
   ```


###### I18

1. Define MessageSource bean for loading message files

   ```java
   @Bean
   public MessageSource messageSource(){
   	ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
   	messageSource.setBasename("messages"); //file name prefix
   	return messageSource;
   }
   ```

2. Define a locale resolver assigning current locale to a session

   ```java
   @Bean
   public LocaleResolver localeResolver(){
   	SessionLocaleResolver resolver = new SessionLocaleResolver();
   	resolver.setDefaultLocale(Locale.ENGLISH);		
   	return resolver;		
   }
   ```

3. Define a interceptor detecting when we want to change a locale

   ```java
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
   	LocaleChangeInterceptor changeInterceptor = new LocaleChangeInterceptor();
   	changeInterceptor.setParamName("language");
   	registry.addInterceptor(changeInterceptor);
   }
   ```

4. Create the message files (messages_en.properties and so on) in /scr/main/resources 

5. Import spring tags

   ```jsp
   <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>    
   ```

6. Define links changing the locale

   ```jsp
   <a href="?language=en">English</a>
   <a href="?language=es">Spanish</a>
   ```

7. Insert tags displaying messages

   ```jsp
   <label for="textinput1"><spring:message code="attendee.name"/>:</label>
   ```

###### Validation

1. Add maven dependencies

   ```xml
   <dependency>
   	<groupId>javax.validation</groupId>
   	<artifactId>validation-api</artifactId>
   </dependency>
   <dependency>
   	<groupId>org.hibernate</groupId>
   	<artifactId>hibernate-validator</artifactId>
   </dependency>
   ```

2. Add validation annotation to a model

   ```java
   @Size(min=2, max=30)
   private String name;
   @NotEmpty @Email
   private String emailAddress;
   ```

3. Lunch validation, use BindingResult which contains any problems appeard during processing

   ```java
   @RequestMapping(value="/attendee", method = RequestMethod.POST)
   public String processAttendee(@Valid Attendee attendee, BindingResult result, Model m){	 		if(result.hasErrors()){
   		return "attendee";
   	}
   	return "redirect:index.html";
   }
   ```

4. Ad customized error messages to the message files

   ```properties
   Size.attendee.name=Name must be between {2} and {1} characters
   Email=Not a valid email address
   NotEmpty=Field cannot be left blank
   ```

###### Create a custom validator

1. Create an annotation interface

   ```java
   @Documented
   @Constraint(validatedBy=PhoneConstraintValidator.class)
   @Target({ElementType.METHOD, ElementType.FIELD})
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Phone {
   	String message() default "{Phone}"; //messages.properties
   	Class<?>[] groups() default {};
   	Class<? extends Payload>[] payload() default{};
   }
   ```

2. Create a new validator implementing `ConstraintValidator`

   ```java
   public class PhoneConstraintValidator implements ConstraintValidator<Phone, String> {
   	@Override
   	public void initialize(Phone arg0) {}

   	@Override
   	public boolean isValid(String phoneField, ConstraintValidatorContext ctx) {
   		if (phoneField == null) {
   			return false;
   		}
   		return phoneField.matches("[0-9()-]*");
   	}
   }
   ```

3. Use a new annotation in a model class

   ```java
   @Phone
   private String phone;
   ```

4. Add own message

   ```properties
   Phone=Not a valid phone number
   ```

5. Display message on a page

   ```jsp
   <label for="textinput3"><spring:message code="attendee.phone"/>:</label>
   <form:input path="phone" cssErrorClass="error"/>
   <form:errors path="phone" cssClass="error"/>	
   ```

   ​