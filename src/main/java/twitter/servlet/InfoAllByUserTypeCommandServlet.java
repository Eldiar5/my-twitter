package twitter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.InfoController;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.entity.user.UserType;
import twitter.exceptions.TwitterIllegalArgumentException;

import java.io.IOException;
import java.util.List;

public class InfoAllByUserTypeCommandServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String userTypeAsString = req.getParameter("userType");
        UserType userTypeEnum;

        try {

            userTypeEnum = UserType.valueOf(userTypeAsString.trim().toUpperCase());

            InfoController infoController = ComponentFactory.getComponent(InfoController.class);
            List<InfoResponseDto> infoResponseDto = infoController.infoAllByUserType(userTypeEnum);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(objectMapper.writeValueAsString(infoResponseDto));

        } catch (IllegalArgumentException e) {
            throw new TwitterIllegalArgumentException("Некорректный тип пользователя: " + userTypeAsString);
        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(ex.getMessage()));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(ex.getMessage()));
        }
    }
}
