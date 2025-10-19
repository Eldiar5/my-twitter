package twitter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import twitter.configuration.ComponentFactory;
import twitter.controller.v2.InfoController;
import twitter.dto.v2.response.InfoResponseDto;
import twitter.exceptions.TwitterIllegalArgumentException;
import twitter.security.JwtHandler;

import java.io.IOException;

public class InfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String authorization = req.getHeader("Authorization");
        String token = authorization.substring(7);

        JwtHandler jwtHandler = ComponentFactory.getComponent(JwtHandler.class);
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            String userLogin = jwtHandler.getUsernameFromToken(token);
            InfoController infoController = ComponentFactory.getComponent(InfoController.class);
            InfoResponseDto infoResponseDto = infoController.info(userLogin);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(objectMapper.writeValueAsString(infoResponseDto));

        } catch (TwitterIllegalArgumentException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(objectMapper.writeValueAsString(ex.getMessage()));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(objectMapper.writeValueAsString(ex.getMessage()));
        }

    }
}
