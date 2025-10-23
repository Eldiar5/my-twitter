package twitter.servlet.postServlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.PostController;
import twitter.dto.v2.response.PostResponseDto;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.security.JwtHandler;
import twitter.sideComponents.web.ObjectMapperAsComponent;

import java.io.IOException;
import java.util.List;

public class MyPostsCommandServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = ComponentFactory.getComponent(ObjectMapperAsComponent.class).getObjectMapper();
        JwtHandler jwtHandler = ComponentFactory.getComponent(JwtHandler.class);

        String authorization = req.getHeader("Authorization");
        String token = authorization.substring(7);

        try {

            String author = jwtHandler.getUsernameFromToken(token);
            PostController postController = ComponentFactory.getComponent(PostController.class);
            List<PostResponseDto> responseDto = postController.myPosts(author);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.getWriter().write(mapper.writeValueAsString(responseDto));

        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        }
    }
}
