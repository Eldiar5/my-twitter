package twitter.servlet.postServlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.PostController;
import twitter.dto.v2.response.PostResponseDto;
import twitter.entity.user.UserType;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.sideComponents.web.ObjectMapperAsComponent;

import java.io.IOException;
import java.util.List;

public class PostsByUserTypeCommandServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = ComponentFactory.getComponent(ObjectMapperAsComponent.class).getObjectMapper();

        String userTypeAsString = req.getParameter("userType");
        UserType userTypeEnum;

        try {

            userTypeEnum = UserType.valueOf(userTypeAsString.trim().toUpperCase());

            PostController postController = ComponentFactory.getComponent(PostController.class);
            List<PostResponseDto> postResponseDto = postController.postsByUserType(userTypeEnum);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");

            resp.getWriter().write(mapper.writeValueAsString(postResponseDto));

        } catch (IllegalArgumentException e) {
            throw new TwitterIllegalArgumentException("Некорректный тип пользователя: " + userTypeAsString);
        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(mapper.writeValueAsString(ex.getMessage()));
        }
    }

}
