package twitter.factory;

import org.springframework.security.crypto.password.PasswordEncoder;
import twitter.configuration.Component;
import twitter.configuration.Injection;
import twitter.controller.v1.PostCommandController;
import twitter.controller.v1.UserCommandController;
import twitter.factory.command.CommandFactoryImpl;
import twitter.mapper.viewMapper.PostViewMapper;
import twitter.security.Security;
import twitter.service.PostService;
import twitter.service.UserService;

import java.io.*;

@Component
public class CommandFactoryBuilder {

    private final Security securityComponent;
    private final PostViewMapper postMapper;
    private final PostService postService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Injection
    public CommandFactoryBuilder(
            Security securityComponent,
            PostViewMapper postMapper,
            PostService postService,
            UserService userService, PasswordEncoder passwordEncoder
    ) {
        this.securityComponent = securityComponent;
        this.postMapper = postMapper;
        this.postService = postService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public CommandFactory buildCommandFactoryForUser(String userIp, BufferedReader reader, BufferedWriter writer) {
        UserCommandController userController = new UserCommandController(userService, passwordEncoder, securityComponent, reader, writer, userIp);
        PostCommandController postController = new PostCommandController(securityComponent, postMapper, postService, userService, reader, writer, userIp);

        return new CommandFactoryImpl(userController, postController);
    }
}
