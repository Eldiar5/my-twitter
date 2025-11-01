package twitter.servlet.postServlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.PostController;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.security.JwtHandler;
import twitter.sideComponents.web.ObjectMapperAsComponent;

import java.io.IOException;

public class DeletePostCommandServlet extends HttpServlet {

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = ComponentFactory.getComponent(ObjectMapperAsComponent.class).getObjectMapper();
        JwtHandler jwtHandler = ComponentFactory.getComponent(JwtHandler.class);

        Long postId = Long.parseLong(req.getParameter("postId"));

        String authorization = req.getHeader("Authorization");
        String token = authorization.substring(7);

        try {
            String author = jwtHandler.getUsernameFromToken(token);
            PostController postController = ComponentFactory.getComponent(PostController.class);
            postController.deletePost(postId, author);

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.setContentType("application/json");

        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (SecurityException ex) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(mapper.writeValueAsString(e.getMessage()));
        }
    }
}
