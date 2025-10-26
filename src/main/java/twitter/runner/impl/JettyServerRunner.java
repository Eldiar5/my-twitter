package twitter.runner.impl;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.configuration.Profile;
import twitter.configuration.Value;
import twitter.filter.TwitterEndpointFilter;
import twitter.runner.ApplicationRunner;
import twitter.servlet.postServlets.*;
import twitter.servlet.userServlets.*;

@Component
@Profile(active = {"default", "prod"})
public class JettyServerRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(JettyServerRunner.class);

    @Injection
    public JettyServerRunner() {}

    @Value(key = "server.port")
    private Integer port;

    @Override
    public void run() {
        try {
            logger.warn("Twitter сервер запустился на порту: {}", port);
            Server server = new Server(port);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            context.addFilter(TwitterEndpointFilter.class, "/*", null);

            context.addServlet(RegisterCommandServlet.class, "/api/register");
            context.addServlet(LoginCommandServlet.class, "/api/login");
            context.addServlet(InfoCommandServlet.class, "/api/info");
            context.addServlet(InfoAllCommandServlet.class, "/api/info-all");
            context.addServlet(InfoByLoginCommandServlet.class, "/api/info-by-login");
            context.addServlet(InfoAllByUserTypeCommandServlet.class, "/api/info-all-by-user-type");

            context.addServlet(AddPostCommandServlet.class, "/api/add-post");
            context.addServlet(MyPostsCommandServlet.class, "/api/my-posts");
            context.addServlet(AllPostsCommandServlet.class, "/api/all-posts");
            context.addServlet(PostsByLoginCommandServlet.class, "/api/posts-by-login");
            context.addServlet(PostsByTagCommandServlet.class, "/api/posts-by-tag");
            context.addServlet(PostsByUserTypeCommandServlet.class, "/api/posts-by-user-type");
            context.addServlet(DeletePostCommandServlet.class, "/api/delete-post");

            server.start();
            server.join();
        } catch (Exception ex) {
            logger.error("Не удалось запустить сервер, ошибка:  {}", ex.getMessage());
        }
    }
}
